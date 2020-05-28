package com.app.instaassist.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.app.instaassist.ui.activity.ImageGalleryActivity;
import com.app.instaassist.ui.activity.VideoPlayerActivity;
import com.app.instaassist.base.Base;
import com.app.instaassist.ui.views.FloatViewManager;
import com.app.instaassist.ui.activity.GalleryPagerActivity;
import com.app.instaassist.services.service.DownloadService;

import java.io.File;


public class DownloadUtil {


    public static void startDownload(String url) {
        final Context context = Base.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_DOWNLOAD_VIDEO_ACTION);
        intent.putExtra(PreferenceUtils.EXTRAS, url);
        context.startService(intent);
    }

    public static void startResumeDownload(String url) {
        final Context context = Base.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, false);

        intent.putExtra(DownloadService.EXTRAS_FORCE_DOWNLOAD, true);
        intent.putExtra(PreferenceUtils.EXTRAS, url);
        context.startService(intent);
    }

    public static void startForceDownload(String pageURL) {
        final Context context = Base.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, false);
        intent.putExtra(DownloadService.EXTRAS_FORCE_DOWNLOAD, true);
        intent.putExtra(PreferenceUtils.EXTRAS, pageURL);
        context.startService(intent);
    }

    public static void startRequest(String pageUrl) {
        final Context context = Base.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, true);
        intent.putExtra(PreferenceUtils.EXTRAS, pageUrl);
        context.startService(intent);
    }

    public static void downloadThumbnail(String pageURL, String downloadUrl) {
        final Context context = Base.getInstance().getApplicationContext();
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.DOWNLOAD_ACTION);
        intent.putExtra(DownloadService.DOWNLOAD_PAGE_URL, pageURL);
        intent.putExtra(PreferenceUtils.EXTRAS, downloadUrl);
        context.startService(intent);
    }


    public static File getHomeDirectory() {
        File targetDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DownloadService.DIR);
        return targetDir;
    }

    public static void openVideo(String filePath) {
        Intent intent = new Intent();
        if (filePath.endsWith("mp4") || filePath.endsWith("mov")) {
            intent.setClass(Base.getInstance().getApplicationContext(), VideoPlayerActivity.class);
            intent.putExtra(PreferenceUtils.EXTRAS, filePath);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), MimeTypeUtil.getMimeTypeByFileName(filePath));
        } else {
            intent.setClass(Base.getInstance().getApplicationContext(), ImageGalleryActivity.class);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
        }
        intent.putExtra(PreferenceUtils.EXTRAS, filePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Base.getInstance().startActivity(intent);
    }

    public static void openFileList(String fileDirectory) {
        Intent intent = new Intent(Base.getInstance().getApplicationContext(), GalleryPagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PreferenceUtils.EXTRAS, fileDirectory);
        Base.getInstance().getApplicationContext().startActivity(intent);
    }


    public static String getDownloadTargetInfo(String url) {
        File targetDir = DownloadUtil.getHomeDirectory();

        if (targetDir.exists()) {
            return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
        }
        targetDir.mkdir();
        return targetDir.getAbsolutePath() + File.separator + getFileNameByUrl(url);
    }

    public static void checkDownloadBaseHomeDirectory() {
        if (!DownloadUtil.getHomeDirectory().exists()) {
            DownloadUtil.getHomeDirectory().mkdir();
        }
    }

    public static String getDownloadTargetDir(String parent, String fileName) {
        File targetDir = new File(parent, fileName);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        return targetDir.getAbsolutePath();
    }

    public static String getDownloadItemDirectory(String tag) {
        File homeDirectory = DownloadUtil.getHomeDirectory();
        if (!homeDirectory.exists()) {
            homeDirectory.mkdir();
        }

        File tagDidrectory = new File(homeDirectory, tag);
        if (!tagDidrectory.exists()) {
            tagDidrectory.mkdirs();
        }

        File itemDirectory = new File(tagDidrectory, String.valueOf(System.currentTimeMillis()));
        if (!itemDirectory.exists()) {
            itemDirectory.mkdir();
        }
        return itemDirectory.getAbsolutePath();
    }

    public static String getFileNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String array[] = url.split("/");
        return array[array.length - 1];
    }

    public static void showFloatView() {
        FloatViewManager manager = FloatViewManager.getDefault();
        manager.showFloatView();
    }

}
