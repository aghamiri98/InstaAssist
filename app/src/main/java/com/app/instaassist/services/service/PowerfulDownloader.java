package com.app.instaassist.services.service;

import android.util.Log;

import com.app.instaassist.base.HttpRequestSpider;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.util.FileUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
public class PowerfulDownloader {


    public static final int CODE_OK = 0;
    public static final int CODE_DOWNLOAD_FAILED = -1;
    public static final int CODE_DOWNLOAD_CANCELED = 100;
    public volatile static PowerfulDownloader sInstance;

    public int THREAD_COUNT = 1;

    private volatile int mReadBytesCount = 0;

    private AtomicBoolean mInternalErrorInterupted = new AtomicBoolean(false);

    private String mCurrentTaskId;
    private int mFilePosition;

    private PowerfulDownloader() {
        THREAD_COUNT = 1;
    }


    public static PowerfulDownloader getDefault() {
        synchronized (PowerfulDownloader.class) {
            if (sInstance == null) {
                sInstance = new PowerfulDownloader();
            }
        }
        return sInstance;
    }


    public void interupted() {
        mInternalErrorInterupted.set(true);
    }


    private class CustomFileDownloadListener extends FileDownloadListener {


        private IPowerfulDownloadCallback callback;
        private List<String> fileList;
        private DownloadContentItem downloadContentItem;

        public CustomFileDownloadListener(List<String> fileList, IPowerfulDownloadCallback callback, DownloadContentItem downloadContentItem) {
            this.callback = callback;
            this.fileList = fileList;
            this.downloadContentItem = downloadContentItem;

        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Log.e("ok", "pending:" + soFarBytes + ":" + totalBytes);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (callback != null) {
                int position = fileList.indexOf(task.getPath());
                callback.onProgress(downloadContentItem.pageURL, position, task.getPath(), (int) (100 * soFarBytes / (totalBytes * 1.0)));
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            if (callback != null) {
                int position = fileList.indexOf(task.getPath());
                callback.onFinish(CODE_OK, downloadContentItem.pageURL, position, task.getPath());
            }
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (callback != null) {
                int position = fileList.indexOf(task.getPath());
                callback.onFinish(CODE_DOWNLOAD_CANCELED, downloadContentItem.pageURL, position, task.getPath());
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            if (callback != null) {
                int position = fileList.indexOf(task.getPath());
                callback.onError(CODE_DOWNLOAD_FAILED);
            }
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            Log.e("ok", "warn:" + task.getFilename());
        }

    }

    public void startDownload(String home, DownloadContentItem downloadContentItem, final IPowerfulDownloadCallback callback) {

        final List<BaseDownloadTask> tasks = new ArrayList<>();
        int imageCount = (downloadContentItem == null || downloadContentItem.futureImageList == null) ? 0 : downloadContentItem.futureImageList.size();
        List<String> downloadFileList = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            String url = downloadContentItem.futureImageList.get(i);
            url = url.replace("\\u0026", "&");
            String path = FileUtils.getFilePath(downloadContentItem.getPageHome(), FileUtils.getFileNameByURL(url));
            downloadFileList.add(path);
            tasks.add(FileDownloader.getImpl().create(url).setTag(i + 1).setPath(path).addHeader("User-Agent", HttpRequestSpider.UA_1));
        }

        int videoCount = (downloadContentItem == null || downloadContentItem.futureVideoList == null) ? 0 : downloadContentItem.futureVideoList.size();
        for (int index = 0; index < videoCount; index++) {
            String url = downloadContentItem.futureVideoList.get(index);
            url = url.replace("\\u0026", "&");
            String path = FileUtils.getFilePath(downloadContentItem.getPageHome(), FileUtils.getFileNameByURL(url));
            downloadFileList.add(path);
            tasks.add(FileDownloader.getImpl().create(url).setTag(imageCount + index + 1).setPath(path).addHeader("User-Agent", HttpRequestSpider.USER_AGENT));
        }


        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(new CustomFileDownloadListener(downloadFileList, callback, downloadContentItem));

        if (tasks.size() > 1) {
            queueSet.disableCallbackProgressTimes();
        }
        queueSet.setAutoRetryTimes(1);
        queueSet.downloadTogether(tasks);
        queueSet.start();
    }

    public String getCurrentDownloadingTaskId() {
        return mCurrentTaskId;
    }

    public interface IPowerfulDownloadCallback {
        void onStart(String path);

        void onFinish(int statusCode, String pageURL, int filePositon, String path);

        void onError(int errorCode);

        void onProgress(String pageURL, int filePositon, String path, final int progress);
    }

}