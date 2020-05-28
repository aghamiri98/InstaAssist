package com.app.instaassist.services.downloader;

import android.os.Looper;
import android.util.Log;

import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.util.URLMatcher;
import com.app.instaassist.util.Utils;


public final class VideoDownloadFactory {

    private BaseDownloader mDownloader;


    private static VideoDownloadFactory sInstance = new VideoDownloadFactory();


    public static VideoDownloadFactory getInstance() {
        return sInstance;
    }

    public DownloadContentItem request(String url) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("video download cannt start from main thread");
        }
        Log.e("fan","request:" + url);
        String handledUrl = URLMatcher.getHttpURL(url);

        BaseDownloader downloader = getSpecDownloader(handledUrl);

        if (downloader == null) {
        }

        if (downloader != null) {
            return downloader.startSpideThePage(url);
        }

        return null;
    }


    private BaseDownloader getSpecDownloader(String url) {
        BaseDownloader downloader;

        if (url.contains("www.instagram.com")) {
            return new InstagramDownloader();
        }

        return null;
    }

    public boolean isSupportWeb(String url) {
        if (url.contains("www.instagram.com")) {
            return true;
        }


        for (String hostKey : Utils.EXPIRE_SUFFIX_ARRAY) {
            if (url.contains(hostKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean needShowPasteBtn() {
        String normalURL = Utils.getTextFromClipboard();
        if (DownloaderDBHelper.SINGLETON.isExistPageURL(normalURL)) {
            return false;
        }
        return VideoDownloadFactory.getInstance().isSupportWeb(normalURL);
    }


}
