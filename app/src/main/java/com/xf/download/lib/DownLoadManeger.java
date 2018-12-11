package com.xf.download.lib;

import android.text.TextUtils;
import android.util.ArrayMap;


import com.xf.download.dao.DaoManager;
import com.xf.utils.AppExecutors;
import com.xf.utils.Lg;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownLoadManeger {
    private static DownLoadManeger instance;
    private final static int TIME_OUT = 10000;
    private final static long MIN_SIZE = 20;//小于20M一个线程下载
    private final static long MIDUM_SIZE = 80;//超过80M开三个线程下载
    private ArrayMap<String, Long> progressCache = new ArrayMap<>();//进度控制缓存
    private Map<String, List<DownLoadBean>> downloadTaskCache = new HashMap<>();//每个url对应可能是多个downloadbean对象（多个任务）
    //创建fixed线程池
    private ExecutorService downloadThreadCache = Executors.newFixedThreadPool(5);//这里是写死最多5个线程
    private Map<String, Boolean> downloacStatus = new HashMap<>();//保存该url对应的下载是否是在下载判断
    private boolean isAuto = false;

    public static DownLoadManeger getInstance() {
        if (instance == null)
            instance = new DownLoadManeger();
        return instance;
    }

    public DownLoadManeger addTask(DownloadBuilder builder) {//添加任务用builder构建
        if (!isExistTask(builder.url))//如果缓存中存在该下载那就直接去取缓存的
            getFileSize(builder);
        else
            addLocalTask(builder);
        return instance;
    }

    private void addLocalTask(DownloadBuilder builder) {//遍历缓存下载任务，并将任务的总进度缓存到progressCache中
        List<DownLoadBean> list = downloadTaskCache.get(builder.url);
        if (list == null)
            return;
        for (int i = 0; i < list.size(); i++) {
            DownLoadBean downLoadBean = list.get(i);
            downLoadBean.setListener(builder.listener);
            if (progressCache.get(builder.url) == null)
                progressCache.put(builder.url, downLoadBean.getCurrentProgress());
            else {
                progressCache.put(builder.url, downLoadBean.getCurrentProgress() + progressCache.get(builder.url));
            }
            DownLoadTask task = new DownLoadTask(downLoadBean, observer);
            TaskQueue.getInstance().add(new DownLoadTask(downLoadBean, observer));//添加到下载队列中
            if (isAuto && downLoadBean.getStatus() == 0) {//判断是否自动下载和当前taskbean是否已经下载并未下载完成
                downloadThreadCache.execute(task);
            }
        }
    }

    public boolean isExistTask(String url) {//判断是否存在下载任务
        return downloadTaskCache.containsKey(url);
    }

    private void getLoacalTask() {//获取本地下载任务缓存，当退出app是会缓存到数据库，所以这里取出缓存
        List<DownLoadBean> list = DaoManager.getInstance().queryList(DownLoadBean.class);
        if (list == null | list.size() == 0)
            return;
        for (DownLoadBean downLoadBean : list) {//遍历缓存，因为下载一个任务其实是对应了几个task线程的，所以这里根据url遍历分类
            if (downloadTaskCache.containsKey(downLoadBean.getUrl())) {
                downloadTaskCache.get(downLoadBean.getUrl()).add(downLoadBean);
            } else {
                List<DownLoadBean> beanList = new ArrayList<>();
                beanList.add(downLoadBean);
                downloadTaskCache.put(downLoadBean.getUrl(), beanList);
            }
        }


    }


    private void getFileSize(DownloadBuilder builder) {//当添加任务时，如果缓存中没有，则通过HttpURLConnection获取下载文件大小
        if (TextUtils.isEmpty(builder.url))
            return;
        AppExecutors.getAppExecutors().networkIO().execute(() -> {
            URL url;
            try {
                url = new URL(builder.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(TIME_OUT);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    builder.total = conn.getContentLength();
                    createTask(builder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void createTask(DownloadBuilder builder) {//根据文件大小计算任务个数
        int taskCount;
        long total = builder.total;
        long mSize = total / 1024 / 1024;//多少MB
        if (mSize < MIN_SIZE) {
            taskCount = 1;
        } else if (mSize >= MIN_SIZE && mSize < MIDUM_SIZE) {
            taskCount = 2;
        } else {
            taskCount = 3;
        }
        long ex = builder.total % taskCount;
        List<DownLoadBean> list = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            long start;
            if (i == 0) {
                start = i * builder.total / taskCount;
            } else {
                start = i * builder.total / taskCount + 1;
            }
            long end;
            if (i == taskCount - 1) {
                end = (builder.total / taskCount) * (i + 1) + ex;
            } else {
                end = (builder.total / taskCount) * (i + 1);
            }
            createTaskBean(builder, start, end, list);
            downloadTaskCache.put(builder.url, list);
        }
    }

    private void createTaskBean(DownloadBuilder builder, long start, long end, List<DownLoadBean> list) {//创建下载任务bean和创建下载任务
        DownLoadBean bean = new DownLoadBean();
        bean.setFileName(builder.name);
        bean.setPath(builder.path);
        bean.setUrl(builder.url);
        bean.setStart(start);
        bean.setEnd(end);
        bean.setTaskId(System.currentTimeMillis());
        bean.setListener(builder.listener);
        bean.setTotal(builder.total);
        DownLoadTask task = new DownLoadTask(bean, observer);
        progressCache.put(bean.getUrl(), 0l);
        TaskQueue.getInstance().add(task);//添加到任务队列
        list.add(bean);
        DaoManager.getInstance().insert(DownLoadBean.class, bean);//保存到数据库
    }


    private DownLoadManeger() {
        getLoacalTask();
    }

    //自动下载
    public void setAutoDownLoad(boolean isAuto) {
        this.isAuto = isAuto;
    }

    public void pauseAll() {//全部暂停
        TaskQueue.getInstance().pause();
        for (DownLoadTask task : TaskQueue.getInstance().getAllTask()) {
            task.setPause(true);
            if (downloacStatus.containsKey(task.getBean().getUrl()))
                downloacStatus.put(task.getBean().getUrl(), false);
        }
    }

    public void pause(String url) {//根据url暂停对应的下载
        if (downloacStatus.containsKey(url) && !downloacStatus.get(url))
            return;
        TaskQueue.getInstance().pause(url);
        downloacStatus.put(url, false);
    }

    public void start(String url) {//根据url开始对应的下载
        if (downloacStatus.containsKey(url) && downloacStatus.get(url))
            return;
        TaskQueue.getInstance().start(url);
        List<DownLoadTask> downLoadTasks = TaskQueue.getInstance().poll(url);
        if (downLoadTasks != null) {
            for (DownLoadTask downLoadTask : downLoadTasks) {
                if (downLoadTask != null) {
                    downLoadTask.setPause(false);
                    downloadThreadCache.execute(downLoadTask);
                }
            }
        }
        downloacStatus.put(url, true);
    }


    public void onDestroy() {//放在activity对应生命周期里，全部暂停
        TaskQueue.getInstance().pause();
    }

    public void startAll() {//全部开始
        List<DownLoadTask> downLoadTask = TaskQueue.getInstance().getAllTask();
        if (downLoadTask != null)
            for (DownLoadTask task : downLoadTask) {
                task.setPause(false);
                downloadThreadCache.execute(task);
                downloacStatus.put(task.getBean().getUrl(), true);
            }
    }


    public static class DownloadBuilder {//构造器
        protected String url;//下载地址
        protected String path;//下载路径
        protected String name;//文件名字
        protected long total;//现在总进度
        protected DownLoadBean.DownLoadListener listener;//下载监听

        public DownloadBuilder url(String url) {
            this.url = url;
            return this;
        }

        public DownloadBuilder filePath(String path) {
            this.path = path;
            return this;
        }

        public DownloadBuilder listener(DownLoadBean.DownLoadListener listener) {
            this.listener = listener;
            return this;
        }

        public DownloadBuilder fileName(String name) {
            this.name = name;
            return this;
        }
    }

    //下载线程的观察者
    private DownLoadTask.DownloadTaskObserver observer = new DownLoadTask.DownloadTaskObserver() {
        @Override
        protected void onStart(DownLoadBean bean, long taskId) {//下载开始，回调总进度给监听，方便设置进度大小
            Lg.e("taskId=" + taskId + ">>>>>" + bean.getTotal());
            bean.setStatus(0);
            AppExecutors.getAppExecutors().mainThread().execute(() -> {//切换到主线程
                if (bean.getListener() != null) {
                    bean.getListener().onStart(bean.getTotal());
                }
            });
            Lg.d("url=" + bean.getUrl() + "--->onStart>>>>>total=" + bean.getTotal());
        }

        @Override
        protected void onProgress(DownLoadBean bean, long taskId, long progress) {//进度调用
            synchronized (this) {//这里必须加同步代码块，因为这共用进度等变量
                long currentProgress = progressCache.get(bean.getUrl()) + progress;//当前线程进度加上缓存进度等于这个下载的总进度
                AppExecutors.getAppExecutors().mainThread().execute(() -> {
                    Lg.d("onProgress>>>>>currentProgress=" + currentProgress + ">>>taskId>>>" + taskId);
                    if (bean.getListener() != null)
                        bean.getListener().onProgress(currentProgress);
                });
                progressCache.put(bean.getUrl(), currentProgress);//缓存下载进度
            }
        }

        @Override
        protected void onFinish(DownLoadBean bean, long taskId, long lastProgress) {
            synchronized (this) {
                long total = progressCache.get(bean.getUrl()) + lastProgress;//这里其实也是当前线程进度加上缓存进度等于这个下载的总进度，
                // 因为不确定是该任务是彻底完成了，所以必须缓存当前线程最后下载的多少
                progressCache.put(bean.getUrl(), total);
                Lg.e(progressCache.get(bean.getUrl()) + "++++++" + bean.getTotal());
                TaskQueue.getInstance().remove(bean);//单个任务完成删除
                DaoManager.getInstance().delete(DownLoadBean.class, bean);
                if (total == bean.getTotal() && bean.getListener() != null) {//多线程下载，所以要判断整体下载完成
                    AppExecutors.getAppExecutors().mainThread().execute(() -> bean.getListener().onFinish(bean.getPath() + "/" + bean.getFileName()));
                    progressCache.remove(bean.getUrl());
                    Lg.d("url=" + bean.getUrl() + ">>>>>onFinish=" + bean.getPath());
                    downloadTaskCache.remove(bean.getUrl());
                    downloacStatus.remove(bean.getUrl());
//                    DownLoadTask downLoadTask = TaskQueue.getInstance().poll();
//                    if (downLoadTask != null)
//                        downloadThreadCache.execute(downLoadTask);
                }
            }
        }

        @Override
        protected void onPause(DownLoadBean bean, long taskId, long currentProgress) {
            synchronized (this) {//暂停这里必须加上同步，不然会出现实际进度跟显示进度不一致问题，其实也就是缓存没存进去，第二个线程又来了
                long progress = currentProgress + progressCache.get(bean.getUrl());
                progressCache.put(bean.getUrl(), progress);
                List<DownLoadBean> downLoadBeans = downloadTaskCache.get(bean.getUrl());
                for (int i = 0; i < downLoadBeans.size(); i++) {
                    if (downLoadBeans.get(i).getTaskId() == taskId) {//根据下载任务id判断
                        downloadTaskCache.get(bean.getUrl()).set(i, bean);//更新内存中下载任务
                    }
                }
                Lg.e(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + progress);
                DaoManager.getInstance().update(DownLoadBean.class, bean);//更新数据库，退出app再进来就不会从头下载
            }
        }

        @Override
        protected void onError(DownLoadBean bean, long taskId, String erroMsg) {//错误提示
            if (bean.getListener() != null) {
                progressCache.remove(bean.getUrl());//删除该进度
                List<DownLoadBean> downLoadBeans = downloadTaskCache.get(bean.getUrl());
                if (downLoadBeans != null)
                    for (int i = 0; i < downLoadBeans.size(); i++) {
                        if (bean.getUrl().equals(downLoadBeans.get(i).getUrl()))
                            downloadTaskCache.get(bean.getUrl()).remove(i);
                    }
                TaskQueue.getInstance().remove(bean);
                AppExecutors.getAppExecutors().mainThread().execute(() -> {
                    bean.getListener().onError(erroMsg);
                    DaoManager.getInstance().delete(DownLoadBean.class, bean);//删除数据库缓存
                });
            }
        }
    };
}
