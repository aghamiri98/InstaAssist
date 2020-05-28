package com.app.instaassist.services.downloader;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.instaassist.R;
import com.app.instaassist.base.Base;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.service.DownloadService;
import com.app.instaassist.services.service.LearningDownloader;
import com.app.instaassist.services.service.PowerfulDownloader;
import com.app.instaassist.util.DownloadUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadingTaskList {


    public static final DownloadingTaskList SINGLETON = new DownloadingTaskList();

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();


    private List<String> mFuturedTaskList = new LinkedList<>();
    private HashMap<String, DownloadContentItem> mFutureTaskDetailMap = new HashMap<>();

    public boolean isPendingDownloadTask(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return false;
        }

        return mFuturedTaskList.indexOf(pageURL) > -1;
    }

    public List<String> getFutureTask() {
        return mFuturedTaskList;
    }

    private DownloadingTaskList() {

    }

    public boolean isEmpty() {
        return mFuturedTaskList.isEmpty();
    }

    public void addNewDownloadTask(String taskId) {
        if (mFuturedTaskList.size() > 0) {
            if (mFuturedTaskList.contains(taskId)) {
                return;
            }
            mFuturedTaskList.add(taskId);
            return;
        }


        mFuturedTaskList.add(taskId);
        executeNextTask();
    }

    public void addNewDownloadTask(String taskId, DownloadContentItem data) {
        if (mFuturedTaskList.size() > 0) {
            if (mFuturedTaskList.contains(taskId)) {
                return;
            }
            mFuturedTaskList.add(taskId);
            mFutureTaskDetailMap.put(taskId, data);
            return;
        }


        mFuturedTaskList.add(taskId);
        mFutureTaskDetailMap.put(taskId, data);
        executeNextTask();
    }


    private Handler mHandler;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }


    public void intrupted(String taskId) {
        if (TextUtils.isEmpty(taskId)) {
            return;
        }

        if (taskId.equals(LearningDownloader.getDefault().getCurrentDownloadingTaskId())) {
            LearningDownloader.getDefault().interupted();
        }

        if (taskId.equals(PowerfulDownloader.getDefault().getCurrentDownloadingTaskId())) {
            PowerfulDownloader.getDefault().interupted();
        }
        mFuturedTaskList.remove(taskId);
    }


    private void downloadItemContent(final DownloadContentItem item) {
        if (item != null) {
            List<String> futureDownloadedList = item.getDownloadContentList();
            downloadItem(item);
            if (item.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FAILED) {
                mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_ERROR, 0, 0, item.pageURL).sendToTarget();
            } else {
                DownloaderDBHelper.SINGLETON.finishDownloadTask(item.pageURL);
                mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_SUCCESS, 0, 0, item.pageURL).sendToTarget();
            }
        }
    }



    private void downloadItem(final DownloadContentItem item) {
        PowerfulDownloader.getDefault().startDownload(item.homeDirectory,item,null);
    }

    private void downloadItem(final List<String> totalDownloadedList, final DownloadContentItem item) {
        if (item.getVideoCount() > 0) {
            final String fileURL = item.getVideoList().remove(0);
            final int filePositon = totalDownloadedList.indexOf(fileURL);

            final String pageURL = item.pageURL;
            LearningDownloader.getDefault().startDownload(filePositon, item.pageURL, fileURL, item.getTargetDirectory(item.pageURL, fileURL), new LearningDownloader.IPowerfulDownloadCallback() {
                @Override
                public void onStart(String path) {

                }

                @Override
                public void onFinish(int code, String pageURL, int filePosition, String path) {
                    if (code == PowerfulDownloader.CODE_OK) {
                        mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_SUCCESS, filePosition, 0, pageURL).sendToTarget();
                        Message msg = mHandler.obtainMessage();
                        msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                        msg.arg1 = 100;
                        msg.arg2 = filePosition;
                        msg.obj = pageURL;
                        mHandler.sendMessage(msg);
                    } else if (code == PowerfulDownloader.CODE_DOWNLOAD_FAILED) {
                        DownloaderDBHelper.SINGLETON.setDownloadingTaskFailed(pageURL);
                        item.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FAILED;

                    } else if (code == PowerfulDownloader.CODE_DOWNLOAD_CANCELED) {
                        DownloaderDBHelper.SINGLETON.deleteDownloadTask(pageURL);
                    }
                    downloadItem(totalDownloadedList, item);
                }

                @Override
                public void onError(int errorCode) {
                }

                @Override
                public void onProgress(String pageURL, int filePosition, String path, int progress) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                    msg.arg1 = progress;
                    msg.arg2 = filePosition;
                    msg.obj = pageURL;
                    mHandler.sendMessage(msg);
                }
            });
        } else if (item.getImageCount() > 0) {
            final String fileURL = item.getImageList().remove(0);
            final int filePositon = totalDownloadedList.indexOf(fileURL);
            PowerfulDownloader.getDefault().startDownload(item.pageURL, item, new PowerfulDownloader.IPowerfulDownloadCallback() {
                @Override
                public void onStart(String path) {

                }

                @Override
                public void onFinish(int statusCode, String pageURL, int filePosition, String path) {
                    if (statusCode == PowerfulDownloader.CODE_OK) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                        msg.arg1 = 100;
                        msg.arg2 = filePosition;
                        msg.obj = pageURL;
                        mHandler.sendMessage(msg);
                    } else if (statusCode == PowerfulDownloader.CODE_DOWNLOAD_CANCELED) {
                        DownloaderDBHelper.SINGLETON.deleteDownloadTask(pageURL);
                    } else if (statusCode == PowerfulDownloader.CODE_DOWNLOAD_FAILED) {
                        item.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FAILED;
                        DownloaderDBHelper.SINGLETON.setDownloadingTaskFailed(pageURL);
                    }
                    downloadItem(totalDownloadedList, item);
                }

                @Override
                public void onError(int errorCode) {

                }

                @Override
                public void onProgress(String pageURL, int filePosition, String path,
                                       int progress) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = DownloadService.MSG_UPDATE_PROGRESS;
                    msg.arg1 = progress;
                    msg.arg2 = filePosition;
                    msg.obj = pageURL;
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

    public void finishTask(String taskId) {
        mFuturedTaskList.remove(taskId);
        mFutureTaskDetailMap.remove(taskId);
    }

    public void executeNextTask() {
        if (mFuturedTaskList.size() > 0) {
            final String taskId = mFuturedTaskList.get(0);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    DownloadContentItem cacheData = mFutureTaskDetailMap.get(taskId);
                    if (cacheData != null) {
                        downloadItemContent(cacheData);
                    } else {
                        DownloadContentItem downloadContentItem = VideoDownloadFactory.getInstance().request(taskId);
                        if (downloadContentItem != null) {
                            if (downloadContentItem != null) {
                                mHandler.post(() -> DownloadUtil.showFloatView());
                                DownloaderDBHelper.SINGLETON.saveNewDownloadTask(downloadContentItem);
                                mHandler.obtainMessage(DownloadService.MSG_DOWNLOAD_START, 0, 0, downloadContentItem.pageURL).sendToTarget();
                                downloadItemContent(downloadContentItem);
                            }
                        } else {
                            mHandler.post(() -> Toast.makeText(Base.getInstance().getApplicationContext(), R.string.spider_request_error, Toast.LENGTH_SHORT).show());
                        }
                    }

                    finishTask(taskId);
                    executeNextTask();
                }
            });
        }
    }


    public ExecutorService getExecutorService() {
        return mExecutorService;
    }
}
