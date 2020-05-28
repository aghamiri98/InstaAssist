package com.app.instaassist.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.app.instaassist.BuildConfig;
import com.app.instaassist.util.DownloadUtil;

import java.util.ArrayList;
import java.util.List;


public class DownloadContentItem implements BaseColumns {

    public static String TABLE_NAME = "download_content";


    public static final int PAGE_MIME_TYPE_IMAGE = 0;
    public static final int PAGE_MIME_TYPE_VIDEO = 1;
    public static final int TYPE_NORMAL_ITEM = 0;
    public static final int TYPE_HEADER_ITEM = 1;
    public static final int TYPE_HOWTO_ITEM = 2;
    public static final int TYPE_FACEBOOK_AD = 3;

    public static final int PAGE_STATUS_DOWNLOADING = 0;
    public static final int PAGE_STATUS_DOWNLOAD_FAILED = 1;
    public static final int PAGE_STATUS_DOWNLOAD_FINISHED = 2;


    public static Uri CONTENT_URI = Uri.parse("content://" + DownloaderContentProvider.AUTHORITY + "/" + TABLE_NAME);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.imob.videodownloader.content";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.imob.videodownloader.content";

    public static final String PAGE_URL = "page_url";
    public static final String PAGE_HOME = "page_home";
    public static final String PAGE_THUMBNAIL = "page_thumbnail";
    public static final String PAGE_TITLE = "page_title";
    public static final String PAGE_DESCRIPTION = "page_desc";
    public static final String HASH_TAGS = "hash_tags";
    public static final String PAGE_MIME_TYPE = "mime_type";
    public static final String PAGE_DOWNLOAD_FILE_COUNT = "count";
    public static final String PAGE_STATUS = "page_status";

    public static final String DEFAULT_ORDERBY = _ID + "  DESC";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY,"
            + PAGE_URL + " VARCHAR(255)," + PAGE_HOME + " VARCHAR(255)," + PAGE_THUMBNAIL + " VARCHAR(255)," + PAGE_TITLE + " VARCHAR(255) ," + PAGE_DESCRIPTION + " VARCHAR(255) ," + HASH_TAGS + " VARCHAR(255)," + PAGE_MIME_TYPE + " int default 0," + PAGE_DOWNLOAD_FILE_COUNT + " int," + PAGE_STATUS + " int default 0);";


    public int pageId;
    public String pageURL;
    public String pageHOME;
    public String pageThumb;
    public String pageTitle;
    public String pageDesc;
    public String pageTags;
    public int mimeType;
    public int fileCount;
    public int pageStatus;

    public String homeDirectory;
    public long createdTime;
    public int itemType = TYPE_NORMAL_ITEM;

    private ContentValues mContentValues;

    public DownloadContentItem() {

    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
    }

    public void setPageThumb(String thumb) {
        this.pageThumb = thumb;
    }


    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void setPageDesc(String pageDesc) {
        this.pageDesc = pageDesc;
    }

    public void setPageTags(String pageTags) {
        this.pageTags = pageTags;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public void setPageStatus(int status) {
        this.pageStatus = status;
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }


    public int getMimeType() {
        return getVideoCount() > 0 ? PAGE_MIME_TYPE_VIDEO : PAGE_MIME_TYPE_IMAGE;
    }


    public static DownloadContentItem fromCusor(Cursor cursor) {
        DownloadContentItem item = new DownloadContentItem();
        item.pageId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
        item.pageURL = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_URL));
        item.pageHOME = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_HOME));
        item.pageThumb = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_THUMBNAIL));
        item.pageTitle = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_TITLE));
        item.pageDesc = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_DESCRIPTION));
        item.pageTags = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.HASH_TAGS));
        item.mimeType = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_MIME_TYPE));
        item.fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_DOWNLOAD_FILE_COUNT));
        item.pageStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_STATUS));
        item.itemType = TYPE_NORMAL_ITEM;
        return item;
    }

    public static ContentValues from(DownloadContentItem item) {
        ContentValues cv = new ContentValues();
        cv.put(PAGE_URL, item.pageURL);
        cv.put(PAGE_THUMBNAIL, item.pageThumb);
        cv.put(PAGE_TITLE, item.pageTitle);
        cv.put(PAGE_DESCRIPTION, item.pageDesc);
        cv.put(HASH_TAGS, item.pageTags);
        cv.put(PAGE_MIME_TYPE, item.getMimeType());
        cv.put(PAGE_DOWNLOAD_FILE_COUNT, item.getFileCount());
        cv.put(PAGE_HOME, item.getPageHomeAndCreateHome());
        cv.put(PAGE_STATUS, item.pageStatus);
        return cv;
    }


    public List<String> futureVideoList;
    public List<String> futureImageList;

    private String getPageHomeAndCreateHome() {
        if (TextUtils.isEmpty(pageHOME)) {
            pageHOME = DownloadUtil.getDownloadItemDirectory(homeDirectory);
        }
        return pageHOME;
    }

    public String getPageHome() {
        if (TextUtils.isEmpty(pageHOME)) {
            pageHOME = DownloadUtil.getDownloadItemDirectory(homeDirectory);
        }
        return pageHOME;
    }


    public void addVideo(String path) {
        if (futureVideoList == null) {
            futureVideoList = new ArrayList<>();
        }

        if (!futureVideoList.contains(path)) {
            futureVideoList.add(path);
        }
    }

    public void addImage(String path) {
        if (futureImageList == null) {
            futureImageList = new ArrayList<>();
        }

        if (!futureImageList.contains(path)) {
            futureImageList.add(path);
        }
    }

    public List<String> getVideoList() {
        return futureVideoList;
    }

    public List<String> getImageList() {
        return futureImageList;
    }


    public int getVideoCount() {
        return futureVideoList == null ? 0 : futureVideoList.size();
    }

    public int getImageCount() {
        return futureImageList == null ? 0 : futureImageList.size();
    }

    public int getFileCount() {
        return getVideoCount() + getImageCount();
    }

    public List<String> getDownloadContentList() {
        List<String> dataList = new ArrayList<>();
        if (getVideoCount() > 0) {
            dataList.addAll(futureVideoList);
        }
        if (getImageCount() > 0) {
            dataList.addAll(futureImageList);
        }
        return dataList;
    }


    public boolean haveVideos() {
        return futureVideoList != null && futureVideoList.size() > 0;
    }

    public boolean haveImages() {
        return futureImageList != null && futureImageList.size() > 0;
    }


    public String getTargetDirectory(String fileUrl) {
        if (BuildConfig.DEBUG) {
            Log.e("download", "getTargetDirectory2=" + fileUrl);
        }
        String childDirectory = "";
        if (fileUrl.contains("facebook.com")) {
            String targetFileName = String.valueOf(System.currentTimeMillis()) + ".mp4";
            if (fileUrl.contains(".mp4")) {
                String mp4Array[] = fileUrl.split(".mp4");
                if (mp4Array.length > 0) {
                    targetFileName = mp4Array[0].substring(mp4Array[0].lastIndexOf("/")) + ".mp4";
                }
            }

            if (fileUrl.contains(".jpg") && !fileUrl.endsWith(".jpg")) {
                String mp4Array[] = fileUrl.split(".jpg");
                if (mp4Array.length > 0) {
                    targetFileName = mp4Array[0].substring(mp4Array[0].lastIndexOf("/")) + ".jpg";
                }
            }
            childDirectory = targetFileName;
        } else {
            if (fileUrl.contains(".mp4")) {
                String targetFileName = String.valueOf(System.currentTimeMillis()) + ".mp4";
                childDirectory = targetFileName;
            } else {
                if (fileUrl.contains(".jpg") && !fileUrl.endsWith(".jpg")) {
                    String targetFileName = "";
                    String mp4Array[] = fileUrl.split(".jpg");
                    if (mp4Array.length > 0) {
                        targetFileName = mp4Array[0].substring(mp4Array[0].lastIndexOf("/")) + ".jpg";
                    }

                    childDirectory = targetFileName;
                } else {
                    childDirectory = DownloadUtil.getFileNameByUrl(fileUrl);
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.e("download", "childDirectory=" + childDirectory);
        }
        return DownloadUtil.getDownloadTargetDir(getPageHome(), childDirectory);
    }

    public String getTargetDirectory(String pageURL, String fileURL) {
        String childDirectory = "";
        if (BuildConfig.DEBUG) {
            Log.e("download", "getTargetDirectroy:" + pageURL + ":" + fileURL);
        }
        if (!TextUtils.isEmpty(pageURL) && !TextUtils.isEmpty(fileURL)) {
            String targetFileName = null;
            if (fileURL.contains(".mp4")) {
                targetFileName = String.valueOf(System.currentTimeMillis()) + ".mp4";
            } else if (fileURL.contains(".jpg")) {
                targetFileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
            }
            childDirectory = targetFileName;
        } else {
            if (TextUtils.isEmpty(childDirectory)) {
                if (!TextUtils.isEmpty(fileURL)) {
                    childDirectory = DownloadUtil.getFileNameByUrl(fileURL);
                }
            }
        }
        return DownloadUtil.getDownloadTargetDir(
                getPageHome(), childDirectory);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloadContentItem) {
            DownloadContentItem bean = (DownloadContentItem) obj;

            if (itemType == bean.itemType) {
                if (itemType == TYPE_NORMAL_ITEM) {
                    if (bean.itemType == TYPE_NORMAL_ITEM) {
                        if (TextUtils.isEmpty(pageURL)) {
                            return false;
                        }
                        return pageURL.equals(bean.pageURL);
                    } else {
                        return false;
                    }
                } else {
                    if (itemType == DownloadContentItem.TYPE_FACEBOOK_AD) {
                        if (itemType == bean.itemType) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    if (itemType == DownloadContentItem.TYPE_HOWTO_ITEM) {
                        if (itemType == bean.itemType) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

}
