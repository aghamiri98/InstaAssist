package com.app.instaassist.services.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.instaassist.R;
import com.app.instaassist.ui.activity.MainActivity;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.downloader.DownloadingTaskList;
import com.app.instaassist.services.downloader.VideoDownloadFactory;
import com.app.instaassist.util.PreferenceUtils;
import com.app.instaassist.util.URLMatcher;
import com.app.instaassist.ui.views.FloatViewManager;
import com.app.instaassist.util.DownloadUtil;

import java.io.File;

public class DownloadService extends Service {


    public static final String DIR = "instaAssist";
    public static final String DOWNLOAD_ACTION = "download_action";
    public static final String DOWNLOAD_PAGE_URL = "page_url";
    public static final String ACTION_CANCEL_ALL_NOTIFICATION = "cancel_all_notification";
    public static final String REQUEST_VIDEO_URL_ACTION = "request_video_url_action";
    public static final String REQUEST_DOWNLOAD_VIDEO_ACTION = "request_download_video_action";
    public static final String EXTRAS_FLOAT_VIEW = "extras_float_view";
    public static final String EXTRAS_FORCE_DOWNLOAD = "extras_force_download";
    public static final String DOWNLOAD_URL = "download_url";

    public static final int MSG_DOWNLOAD_SUCCESS = 0;
    public static final int MSG_DOWNLOAD_ERROR = 1;
    public static final int MSG_DOWNLOAD_START = 2;
    public static final int MSG_UPDATE_PROGRESS = 3;
    public static final int MSG_NOTIFY_DOWNLOADED = 4;
    public static final int MSG_HANDLE_SEND_ACTION = 5;
    public static final int MSG_REQUSET_URL_ERROR = 6;

    private NotificationManager mNotifyManager;

    private static final String NOTIFICATION_CHANNEL_SERVICE = "download-notification";

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    public static final int NOTIFICATION_ID_DOWNLOAD = 1000009;


    final RemoteCallbackList<IDownloadCallback> mCallbacks = new RemoteCallbackList<IDownloadCallback>();

    @Override
    public void onCreate() {
        super.onCreate();
        final ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cb.addPrimaryClipChangedListener(() -> {

            String pasteContent = cb.getText().toString();
            if (TextUtils.isEmpty(pasteContent)) {
                return;
            }
            String handledUrl = URLMatcher.getHttpURL(pasteContent);
            if (VideoDownloadFactory.getInstance().isSupportWeb(handledUrl)) {
                cb.setPrimaryClip(ClipData.newPlainText("", ""));
                boolean  isExistURL = DownloaderDBHelper.SINGLETON.isExistPageURL(handledUrl);
                if(isExistURL) {
                    Toast.makeText(DownloadService.this, R.string.existed_download, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
                intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, true);
                intent.putExtra(PreferenceUtils.EXTRAS, handledUrl);
                processRequestDownload(intent);
            }
        });
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DOWNLOAD_SUCCESS) {
                if (msg.obj != null) {
                    String pageURL = (String) msg.obj;
                    boolean isExistURL = DownloaderDBHelper.SINGLETON.isExistPageURL(pageURL);
                    if (isExistURL) {
                        Toast.makeText(DownloadService.this, R.string.download_result_success, Toast.LENGTH_SHORT).show();
                        DownloadService.this.notifyDownloadFinished((String) msg.obj);
                    }
                }
            } else if (msg.what == MSG_DOWNLOAD_ERROR) {
                Toast.makeText(DownloadService.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            } else if (msg.what == MSG_DOWNLOAD_START) {
                String pageURL = (String) msg.obj;
                DownloadService.this.notifyStartDownload((String) msg.obj);
            } else if (msg.what == MSG_UPDATE_PROGRESS) {
                DownloadService.this.notifyDownloadProgress((String) msg.obj, msg.arg2, msg.arg1);
            } else if (msg.what == MSG_NOTIFY_DOWNLOADED) {
                Toast.makeText(DownloadService.this, R.string.toast_downlaoded_video, Toast.LENGTH_SHORT).show();
                DownloadService.this.notifyReceiveNewTask(getString(R.string.toast_downlaoded_video));
            } else if (msg.what == MSG_HANDLE_SEND_ACTION) {
                if (msg.obj == null) {
                    Toast.makeText(DownloadService.this, R.string.spider_request_error, Toast.LENGTH_SHORT).show();
                }
                DownloadService.this.notifyReceiveNewTask((String) msg.obj);
            }
        }
    };

    private void showFloatView() {
        mHandler.post(() -> {
            if (DownloadingTaskList.SINGLETON.getFutureTask().size() == 0) {
                FloatViewManager manager = FloatViewManager.getDefault();
                manager.showFloatView();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            DownloadUtil.checkDownloadBaseHomeDirectory();

            if (DOWNLOAD_ACTION.equals(intent.getAction())) {
                final String url = intent.getStringExtra(PreferenceUtils.EXTRAS);
                final String pageURL = intent.getStringExtra(DOWNLOAD_PAGE_URL);
                final boolean forceDownload = intent.getBooleanExtra(EXTRAS_FORCE_DOWNLOAD,false);
                if (TextUtils.isEmpty(url)) {
                    return super.onStartCommand(intent, flags, startId);
                }
                boolean isExistURL = DownloaderDBHelper.SINGLETON.isExistPageURL(pageURL);
                if(!forceDownload) {

                    if (isExistURL) {
                        Toast.makeText(DownloadService.this, R.string.existed_download, Toast.LENGTH_SHORT).show();
                        return START_STICKY;
                    }
                }
                final DownloadContentItem item = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
                final String homeDir = item.getTargetDirectory(url);
                DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        PowerfulDownloader.getDefault().startDownload(pageURL, item, new PowerfulDownloader.IPowerfulDownloadCallback() {
                            @Override
                            public void onStart(String path) {

                            }

                            @Override
                            public void onFinish(int statusCode, String pageURL, int filePositon, String path) {
                                mHandler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS);
                            }

                            @Override
                            public void onError(int errorCode) {

                            }

                            @Override
                            public void onProgress(String pageURL, int filePositon, String path, int progress) {

                            }
                        });
                    }
                });

            } else if (REQUEST_VIDEO_URL_ACTION.equals(intent.getAction())) {
                processRequestDownload(intent);

            } else if (REQUEST_DOWNLOAD_VIDEO_ACTION.equals(intent.getAction())) {
                String url = intent.getStringExtra(PreferenceUtils.EXTRAS);
                if (DownloaderDBHelper.SINGLETON.isExistDownloadedPageURL(url)) {
                    mHandler.sendEmptyMessage(MSG_NOTIFY_DOWNLOADED);
                    return super.onStartCommand(intent, flags, startId);
                }
                DownloadingTaskList.SINGLETON.setHandler(mHandler);
                DownloadingTaskList.SINGLETON.addNewDownloadTask(url);
            }
        }
        return Service.START_STICKY;
    }


    private void processRequestDownload(Intent intent) {
        final String url = intent.getStringExtra(PreferenceUtils.EXTRAS);
        if (TextUtils.isEmpty(url)) {
            return ;
        }
        final boolean showFloatView = intent.getBooleanExtra(DownloadService.EXTRAS_FLOAT_VIEW, true);
        String pageHome = DownloaderDBHelper.SINGLETON.getDownloadedPageHomeByURL(url);
        final boolean forceDownload = intent.getBooleanExtra(DownloadService.EXTRAS_FORCE_DOWNLOAD, false);
        if (!forceDownload) {
            if (pageHome != null && new File(pageHome).exists()) {
                mHandler.sendEmptyMessage(MSG_NOTIFY_DOWNLOADED);
            }

            boolean isExistURL = DownloaderDBHelper.SINGLETON.isExistPageURL(url);
            if (isExistURL) {
                Toast.makeText(DownloadService.this, R.string.existed_download, Toast.LENGTH_SHORT).show();
                return ;
            }
        } else {

        }


        DownloadUtil.checkDownloadBaseHomeDirectory();
        DownloadingTaskList.SINGLETON.setHandler(mHandler);
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                DownloadContentItem downloadContentItem = null;
                downloadContentItem = VideoDownloadFactory.getInstance().request(url);

                if (downloadContentItem != null && downloadContentItem.getFileCount() > 0) {
                    if (showFloatView) {
                        showFloatView();
                    }
                    String pageHome = DownloaderDBHelper.SINGLETON.getPageHomeByPageURL(url);
                    if (!TextUtils.isEmpty(pageHome)) {
                        downloadContentItem.pageHOME = pageHome;
                        File targetHome = new File(pageHome);
                        if (!targetHome.exists()) {
                            targetHome.mkdir();
                        }
                        downloadContentItem.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOADING;
                    }
                    DownloaderDBHelper.SINGLETON.saveNewDownloadTask(downloadContentItem);
                    if (!forceDownload) {
                        mHandler.obtainMessage(MSG_DOWNLOAD_START, downloadContentItem.pageURL).sendToTarget();
                    }
                    DownloadingTaskList.SINGLETON.addNewDownloadTask(url, downloadContentItem);
                } else {
                    mHandler.sendEmptyMessage(MSG_HANDLE_SEND_ACTION);
                }
            }
        });

    }

    private void initNotification() {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.setComponent(new ComponentName(this, MainActivity.class));
        PendingIntent disconnectPendingIntent = PendingIntent.getActivity(this, 0, mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);
        mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SERVICE);
        mBuilder.setWhen(0);
        mBuilder.setColor(ContextCompat.getColor(this, R.color.black)).setContentIntent(disconnectPendingIntent)
                .setSmallIcon(R.drawable.ins_icon);
        mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }



    final static int PROGRESS_MAX = 100;
    static int PROGRESS_CURRENT = 0;
    private int mProgress = PROGRESS_CURRENT;

    private void updateNotificationProgress(String pageURL, int progress) {
        if (PROGRESS_CURRENT != progress) {
            if (progress == 100) {
                mBuilder.setContentText("Download complete")
                        .setProgress(0, 0, false);
                mNotifyManager.notify(pageURL.hashCode(), mBuilder.build());
            } else {
                mBuilder.setProgress(100, progress, false);
                mNotifyManager.notify(pageURL.hashCode(), mBuilder.build());
                PROGRESS_CURRENT = progress;
            }
        }

    }

    private void cancelNotification(String pageURL) {
        mNotifyManager.cancel(pageURL.hashCode());
        if (DownloadingTaskList.SINGLETON.isEmpty()) {
            stopForeground(true);
        }
    }


    private void cancelAllNotification() {
        //mNotifyManager.cancelAll();
    }
    public void publishProgress(String pageURL, int filePosition, int progress) {
        notifyDownloadProgress(pageURL, filePosition, progress);
    }

    private Object sCallbackLock = new Object();

    private void notifyDownloadProgress(String pageUrl, int filePosition, int progress) {
        try {
            synchronized (sCallbackLock) {
                if (mCallbacks.getRegisteredCallbackCount() > 0) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).onPublishProgress(pageUrl, filePosition, progress);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyReceiveNewTask(String pageURL) {
        try {
            synchronized (sCallbackLock) {
                if (mCallbacks.getRegisteredCallbackCount() > 0) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).onReceiveNewTask(pageURL);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void notifyStartDownload(String filePath) {
        try {
            if (mCallbacks.getRegisteredCallbackCount() > 0) {
                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onStartDownload(filePath);
                    } catch (RemoteException e) {
                    }
                }
                mCallbacks.finishBroadcast();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyDownloadFinished(String filePath) {

        try {
            if (mCallbacks.getRegisteredCallbackCount() > 0) {
                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onDownloadSuccess(filePath);
                    } catch (RemoteException e) {
                    }
                }
                mCallbacks.finishBroadcast();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private final IDownloadBinder.Stub mBinder = new IDownloadBinder.Stub() {
        @Override
        public void registerCallback(IDownloadCallback callback) throws RemoteException {
            mCallbacks.register(callback);
        }

        @Override
        public void unregisterCallback(IDownloadCallback callback) throws RemoteException {
            mCallbacks.unregister(callback);
        }
    };
}
