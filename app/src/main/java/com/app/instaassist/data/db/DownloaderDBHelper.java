package com.app.instaassist.data.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.app.instaassist.base.Base;
import com.app.instaassist.services.downloader.DownloadingTaskList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloaderDBHelper {


    public static final DownloaderDBHelper SINGLETON = new DownloaderDBHelper();

    private Context mContext;
    private ContentResolver mContentResolver;

    private DownloaderDBHelper() {
        mContext = Base.getInstance().getApplicationContext();
        mContentResolver = mContext.getContentResolver();
    }


    public void saveNewDownloadTask(DownloadContentItem item) {
        if (item != null && !TextUtils.isEmpty(item.pageURL)) {
            int pageId = getPageIdByPageURL(item.pageURL);
            if (pageId > -1) {
                updateDownloadTaskStatus(pageId,DownloadContentItem.PAGE_STATUS_DOWNLOADING);
                return;
            }
            Uri id = mContentResolver.insert(DownloadContentItem.CONTENT_URI, DownloadContentItem.from(item));
        }
    }


    public List<DownloadContentItem> getDownloadingTask() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " != ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        List<DownloadContentItem> itemList = new ArrayList<>();
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    itemList.add(item);
                }
                return itemList;
            }

            return itemList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<DownloadContentItem> getDownloadedTask() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " = ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        try {
            if (cursor != null) {
                List<DownloadContentItem> itemList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    itemList.add(item);
                }
                return itemList;
            }

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getDownloadedTaskCount() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " = ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        try {
            if (cursor != null) {
                return cursor.getCount();
            }

            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getDownloadingTaskCount() {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_STATUS + " = ?", new String[]{String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOADING)}, null);
        try {
            if (cursor != null) {
                return cursor.getCount();
            }

            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public DownloadContentItem getDownloadItemByPageURL(String pageURL) {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    return item;
                }
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public DownloadContentItem getDownloadItemByPageHome(String pageHome) {
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_HOME + " = ? ", new String[]{pageHome}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    DownloadContentItem item = DownloadContentItem.fromCusor(cursor);
                    return item;
                }
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getPageIdByPageURL(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return -1;
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
                }
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getDownloadingPageIdByPageURL(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return -1;
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ?", new String[]{pageURL}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
                }
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getPageHomeByPageURL(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return "";
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_HOME));
                }
            }
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getDownloadedPageIdByURL(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return -1;
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? and " + DownloadContentItem.PAGE_STATUS + " = ?", new String[]{pageURL, String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
                }
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getDownloadedPageHomeByURL(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return null;
        }
        Cursor cursor = mContentResolver.query(DownloadContentItem.CONTENT_URI, null, DownloadContentItem.PAGE_URL + " = ? and " + DownloadContentItem.PAGE_STATUS + " = ?", new String[]{pageURL, String.valueOf(DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED)}, null);
        try {
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_HOME));
                }
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public void finishDownloadTask(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return;
        }
        int pageId = getDownloadingPageIdByPageURL(pageURL);
        if (pageId > -1) {
            updateDownloadTaskStatus(pageId, DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED);
        }
    }

    public void setDownloadingTaskFailed(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return;
        }
        int pageId = getDownloadingPageIdByPageURL(pageURL);
        if (pageId > -1) {
            updateDownloadTaskStatus(pageId, DownloadContentItem.PAGE_STATUS_DOWNLOAD_FAILED);
        }
    }

    public int updateDownloadTaskStatus(int pageId, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContentItem.PAGE_STATUS, status);
        Uri uri = ContentUris.withAppendedId(DownloadContentItem.CONTENT_URI, pageId);
        int count = mContentResolver.update(uri, values, null, null);
        return count;
    }

    public int deleteDownloadTask(String pageURL) {
        DownloadContentItem item = getDownloadItemByPageURL(pageURL);
        if (item != null) {
            String dir = item.pageHOME;
            if (!TextUtils.isEmpty(dir)) {
                File dirFile = new File(dir);
                Context context = Base.getInstance().getApplicationContext();
                if (dirFile.isDirectory() && dirFile.listFiles() != null) {
                    for (File meidaFile : dirFile.listFiles()) {
                        meidaFile.delete();
                        deleteMediaDB(context, meidaFile.getAbsolutePath());
                    }
                    dirFile.delete();
                } else {
                    dirFile.delete();
                }
            }
        }

        return mContentResolver.delete(DownloadContentItem.CONTENT_URI, DownloadContentItem.PAGE_URL + " = ? ", new String[]{pageURL});
    }

    public int deleteDownloadContentItem(DownloadContentItem downloadContentItem) {
        if (downloadContentItem != null) {
            String dir = downloadContentItem.pageHOME;
            if (!TextUtils.isEmpty(dir)) {
                File dirFile = new File(dir);
                Context context = Base.getInstance().getApplicationContext();
                if (dirFile != null) {
                    if (dirFile.isDirectory() && dirFile.listFiles() != null) {
                        for (File meidaFile : dirFile.listFiles()) {
                            meidaFile.delete();
                            deleteMediaDB(context, meidaFile.getAbsolutePath());
                        }
                        dirFile.delete();
                    } else {
                        dirFile.delete();
                    }
                }
            }
        }

        return mContentResolver.delete(DownloadContentItem.CONTENT_URI, DownloadContentItem._ID + " = ? ", new String[]{String.valueOf(downloadContentItem.pageId)});
    }


    private void deleteMediaDB(Context context, String path) {
        try {
            int id = context.getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + " = '" + path + "'", null);
            id = context.getContentResolver().delete(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.DATA + " = '" + path + "'", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isExistDownloadedPageURL(String pageURL) {
        return getDownloadedPageIdByURL(pageURL) > -1;
    }

    public boolean isExistPageURL(String pageURL) {
        return getPageIdByPageURL(pageURL) > -1;
    }

    public void deleteDownloadTaskAsync(final String pageURL) {
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                deleteDownloadTask(pageURL);
            }
        });
    }


}
