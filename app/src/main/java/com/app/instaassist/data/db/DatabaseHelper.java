package com.app.instaassist.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.app.instaassist.base.Base;

public class DatabaseHelper {


    private static final String DATABASE_NAME = "assistDownloader.db";
    SQLiteDatabase db;
    Context context;

    private static volatile DatabaseHelper sDefault;


    public static DatabaseHelper getDefault() {
        if (sDefault == null) {
            sDefault = new DatabaseHelper(Base.getInstance().getApplicationContext());
        }

        return sDefault;
    }

    public DatabaseHelper(Context _context) {
        context = _context;
        db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        createTable();
    }

    public void createTable() {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS downloading_table (" +
                    "_ID INTEGER PRIMARY KEY autoincrement,"
                    + "video_title TEXT, page_url varchar(512),thumbnail_url varchar(512),video_url varchar(512),app_page_url varchar(512),video_path varchar(512),video_status int default 0"
                    + ");");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
