package com.xf.download.lib;


import java.util.ArrayList;
import java.util.List;

public class TaskQueue {
    private static TaskQueue instance;
    private List<DownLoadTask> mTaskQueue;
    private int currentTask = 0;

    private TaskQueue() {
        mTaskQueue = new ArrayList<>();
    }

    public int size() {
        return mTaskQueue.size();
    }

    public static TaskQueue getInstance() {
        if (instance == null)
            instance = new TaskQueue();
        return instance;
    }

    public List<DownLoadTask> getAllTask() {
        return mTaskQueue;
    }

    public void add(DownLoadTask task) {
        mTaskQueue.add(task);
    }

    public void remove(DownLoadBean p) {
        for (int i = 0; i < mTaskQueue.size(); i++) {
            DownLoadTask task = mTaskQueue.get(i);
            if (task.getBean().getUrl().equals(p.getUrl())) {
                if (i <= currentTask)
                    currentTask--;
                mTaskQueue.remove(i);
            }
        }
    }

    public boolean isExistTask(DownLoadBean p) {
        for (DownLoadTask task : mTaskQueue) {
            if (task.getBean().getUrl().equals(p.getUrl()))
                return true;
        }
        return false;
    }

    public DownLoadTask poll() {
        if (currentTask >= 0 && currentTask < mTaskQueue.size()) {
            DownLoadTask task = mTaskQueue.get(currentTask);
            currentTask++;
            return task;
        }
        return null;
    }

    public List<DownLoadTask> poll(String url) {
        List<DownLoadTask> list = new ArrayList<>();
        for (DownLoadTask task : mTaskQueue) {
            if (task.getBean().getUrl().equals(url)) {
                list.add(task);
                currentTask++;
            }
        }
        return list;
    }

    public void pause(String url) {
        for (DownLoadTask task : mTaskQueue) {
            if (task.getBean().getUrl().equals(url)) {
                task.setPause(true);
                currentTask--;
            }
        }
    }

    public void pause() {
        currentTask = 0;
        for (DownLoadTask task : mTaskQueue) {
            task.setPause(true);
        }
    }

    public void start(String url) {
        for (DownLoadTask task : mTaskQueue) {
            if (task.getBean().getUrl().equals(url)) {
                task.setPause(false);
            }
        }
    }
}