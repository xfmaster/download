package com.xf.download.lib;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

@Entity
public class DownLoadBean {
    private String url;
    private long start;
    private long end;
    private long total;
    @Id
    private long taskId;
    private String fileName;
    private String path;
    private long currentProgress;
    private int status = -1;//-1、未下载；0、下载中；1、下载完成
    //@Transient：表明这个字段不会被写入数据库，只是作为一个普通的java类字段，用来临时存储数据的，不会被持久化
    @Transient
    private DownLoadListener listener;


    @Generated(hash = 4512285)
    public DownLoadBean(String url, long start, long end, long total, long taskId,
            String fileName, String path, long currentProgress, int status) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.total = total;
        this.taskId = taskId;
        this.fileName = fileName;
        this.path = path;
        this.currentProgress = currentProgress;
        this.status = status;
    }

    @Generated(hash = 600345743)
    public DownLoadBean() {
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(long currentProgress) {
        this.currentProgress = currentProgress;
    }

    public DownLoadListener getListener() {
        return listener;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public void setListener(DownLoadListener listener) {
        this.listener = listener;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public interface DownLoadListener {
        void onStart(long totalSize);

        void onError(String error);

        void onProgress(long progress);

        void onFinish(String filePath);
    }
}
