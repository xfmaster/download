package com.xf.download.lib;

public interface DownLoadListener {
    void onStart(long totalSize);

    void onError(String error);

    void onProgress(long progress);

    void onFinish(String filePath);
}
