package com.app.instaassist.data.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.app.instaassist.BuildConfig;

import java.util.HashMap;

public class DownloaderContentProvider extends ContentProvider {
    public static final String TAG = "download";
    public static final String DATABASE_NAME = "imob_downloader.db";
    public static final int DATABASE_VERSION = 1;
    public static String AUTHORITY = BuildConfig.APPLICATION_ID;

    private static UriMatcher sUriMatcher;
    private static final int DOWNLOAD_LIST = 1;
    private static final int DOWNLOAD_LIST_ITEM = 2;


    private DatabaseHelper mDBHelper;


    private static HashMap<String, String> sDownloaderProjectioNMap;

    static {
        sDownloaderProjectioNMap = new HashMap<String, String>();
        sDownloaderProjectioNMap.put(DownloadContentItem._ID, DownloadContentItem._ID);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_URL, DownloadContentItem.PAGE_URL);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_HOME, DownloadContentItem.PAGE_HOME);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_THUMBNAIL, DownloadContentItem.PAGE_THUMBNAIL);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_TITLE, DownloadContentItem.PAGE_TITLE);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_DESCRIPTION, DownloadContentItem.PAGE_DESCRIPTION);
        sDownloaderProjectioNMap.put(DownloadContentItem.HASH_TAGS, DownloadContentItem.HASH_TAGS);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_DOWNLOAD_FILE_COUNT, DownloadContentItem.PAGE_DOWNLOAD_FILE_COUNT);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_MIME_TYPE, DownloadContentItem.PAGE_MIME_TYPE);
        sDownloaderProjectioNMap.put(DownloadContentItem.PAGE_STATUS, DownloadContentItem.PAGE_STATUS);

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, DownloadContentItem.TABLE_NAME, DOWNLOAD_LIST);
        sUriMatcher.addURI(AUTHORITY, DownloadContentItem.TABLE_NAME + "/#", DOWNLOAD_LIST_ITEM);

    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DatabaseHelper(getContext());
        return mDBHelper == null ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case DOWNLOAD_LIST:
                queryBuilder.setTables(DownloadContentItem.TABLE_NAME);
                queryBuilder.setProjectionMap(sDownloaderProjectioNMap);
                break;
            case DOWNLOAD_LIST_ITEM:
                queryBuilder.setTables(DownloadContentItem.TABLE_NAME);
                queryBuilder.setProjectionMap(sDownloaderProjectioNMap);
                queryBuilder.appendWhere(DownloadContentItem._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }

        String orderBy = null;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DownloadContentItem.DEFAULT_ORDERBY;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case DOWNLOAD_LIST:
                return DownloadContentItem.CONTENT_TYPE;
            case DOWNLOAD_LIST_ITEM:
                return DownloadContentItem.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case DOWNLOAD_LIST:
                long rowID = database.insert(DownloadContentItem.TABLE_NAME, null, values);

                if (rowID > 0) {
                    Uri rowUri = ContentUris.withAppendedId(DownloadContentItem.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                    return rowUri;
                }

                break;
            default:
                throw new IllegalArgumentException("Unknow URI: " + uri);

        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count = -1;
        switch (sUriMatcher.match(uri)) {
            case DOWNLOAD_LIST:
                count = db.delete(DownloadContentItem.TABLE_NAME, selection, selectionArgs);
                break;

            case DOWNLOAD_LIST_ITEM:
                String rowID = uri.getPathSegments().get(1);
                count = db.delete(DownloadContentItem.TABLE_NAME, DownloadContentItem._ID + "=" + rowID, null);
                break;

            default:
                throw new IllegalArgumentException("Unknow URI :" + uri);

        }
        this.getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count = -1;
        switch (sUriMatcher.match(uri)) {
            case DOWNLOAD_LIST:
                count = db.update(DownloadContentItem.TABLE_NAME, values, selection, null);
                break;
            case DOWNLOAD_LIST_ITEM:
                String rowID = uri.getPathSegments().get(1);
                count = db.update(DownloadContentItem.TABLE_NAME, values,DownloadContentItem._ID + "=" + rowID, null);
                break;
            default:
                throw new IllegalArgumentException("Unknow URI : " + uri);

        }
        this.getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {


        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DownloadContentItem.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
