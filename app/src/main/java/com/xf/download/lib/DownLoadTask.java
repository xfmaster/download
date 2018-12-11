package com.xf.download.lib;


import com.xf.utils.Lg;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoadTask implements Runnable {
    private boolean pause;
    private DownLoadBean bean;
    private long total;
    private long currentTotal = 0;
    private DownloadTaskObserver observer;

    public DownLoadTask(DownLoadBean bean, DownloadTaskObserver observer) {
        this.bean = bean;
        this.observer = observer;
    }

    @Override
    public void run() {
        HttpURLConnection urlConnection;
        RandomAccessFile randomFile;
        InputStream inputStream;
        try {
            URL url = new URL(bean.getUrl());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);      //设置连接超时事件为5秒
            urlConnection.setRequestMethod("GET");      //设置请求方式为GET
            //设置用户端可以接收的媒体类型
            urlConnection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, " +
                    "image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                    "application/vnd.ms-xpsdocument, application/x-ms-xbap," +
                    " application/x-ms-application, application/vnd.ms-excel," +
                    " application/vnd.ms-powerpoint, application/msword, */*");

            urlConnection.setRequestProperty("Accept-Language", "zh-CN");  //设置用户语言
            urlConnection.setRequestProperty("Charset", "UTF-8");    //设置客户端编码
            //设置用户代理
            urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727;" +
                    " .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            //设置下载位置
            urlConnection.setRequestProperty("Range", "bytes=" + bean.getStart() + "-" + bean.getEnd());
            //设置文件写入位置
            File file = new File(bean.getPath(), bean.getFileName());
            randomFile = new RandomAccessFile(file, "rwd");
            randomFile.seek(bean.getStart());
            urlConnection.connect();
            if (observer != null) {
                observer.onStart(bean, bean.getTaskId());
            }
            Lg.e(bean.getStart() + "start------" + bean.getEnd() + ">>>>" + bean.getTaskId());
            //获得文件流
            inputStream = urlConnection.getInputStream();
            byte[] buffer = new byte[1024];
            int len;
            long time = System.currentTimeMillis();
            while ( (len = inputStream.read(buffer)) != -1) {
                //写入文件
                randomFile.write(buffer, 0, len);
                total += len;
                currentTotal += len;
                //时间间隔大于500ms再发
                if (System.currentTimeMillis() - time > 500) {
                    time = System.currentTimeMillis();
                    observer.onProgress(bean, bean.getTaskId(), total);
                    total = 0;
                }
                //判断是否是暂停状态.保存断点
                if (observer != null && pause) {
                    bean.setStart(bean.getStart()+currentTotal);
                    bean.setCurrentProgress(bean.getCurrentProgress()+currentTotal);
                    observer.onPause(bean, bean.getTaskId(), total);
                    total = 0;
                    currentTotal=0;
                    randomFile.close();
                    inputStream.close();
                    break;
                }
                Lg.d(bean.getTaskId() + ">>>>currentTotal=" + currentTotal);
            }
            Lg.d(bean.getTaskId() + "------pause" + pause + ">>>>currentTotal=" + currentTotal);
            if (observer != null && !pause) {
                observer.onFinish(bean, bean.getTaskId(), total);
                Lg.d(bean.getTaskId() + "------" + bean.getEnd() + ">>>>currentTotal=" + total);
            }
        } catch (Exception e) {
            if (observer != null) {
                observer.onError(bean, bean.getTaskId(), e.getMessage());
            }
        }
    }

    public DownLoadBean getBean() {
        return bean;
    }

    public void setBean(DownLoadBean bean) {
        this.bean = bean;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }


    protected static abstract class DownloadTaskObserver {

        protected abstract void onStart(DownLoadBean bean, long taskId);

        protected abstract void onProgress(DownLoadBean bean, long taskId, long progress);

        protected abstract void onFinish(DownLoadBean bean, long taskId, long lastProgress);

        protected abstract void onPause(DownLoadBean bean, long taskId, long currentProgress);

        protected abstract void onError(DownLoadBean bean, long taskId, String erroMsg);
    }
}
