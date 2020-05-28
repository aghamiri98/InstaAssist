package com.app.instaassist.util;

import android.util.Log;

import com.app.instaassist.BuildConfig;

import java.io.File;

public class FileUtils {
    public static final String SUFFIX_FILES[] = new String[]{"mp4", "jpg", "jpeg"};

    public static String getFileNameByPath(String path) {
        String[] separtArray = path.split(File.separator);
        if (separtArray != null && separtArray.length > 1) {
            return separtArray[separtArray.length - 1];
        }
        return path;
    }


    public static String getFileName(String fileUrl) {
        String targetFileName = String.valueOf(System.currentTimeMillis()) + ".mp4";
        if (fileUrl.contains("facebook.com")) {

            if (fileUrl.contains(".mp4")) {
                String mp4Array[] = fileUrl.split(".mp4");
                if (mp4Array.length > 0) {
                    targetFileName = mp4Array[0].substring(mp4Array[0].lastIndexOf("/")) + ".mp4";
                }
            }

            if (fileUrl.contains(".jpg")) {
                String mp4Array[] = fileUrl.split(".jpg");
                if (mp4Array.length > 0) {
                    targetFileName = mp4Array[0].substring(mp4Array[0].lastIndexOf("/")) + ".jpg";
                }
            }
            return targetFileName;
        } else {
            if (fileUrl.contains(".mp4")) {
                targetFileName = String.valueOf(System.currentTimeMillis()) + ".mp4";
            } else {
                if (fileUrl.contains(".jpg")) {
                    String mp4Array[] = fileUrl.split(".jpg");
                    if (mp4Array.length > 0) {
                        targetFileName = mp4Array[0].substring(mp4Array[0].lastIndexOf("/")) + ".jpg";
                    }

                } else {
                }
            }
        }
        if (BuildConfig.DEBUG) {
        }
        return targetFileName;
    }


    public static String getFileNameByURL(String fileUrl) {
        String fileName = fileUrl;
        for (String suffix : SUFFIX_FILES) {
            if (fileUrl.contains(suffix)) {
                String regx = "\\." + suffix;
                String array[] = fileUrl.split(regx);
                Log.e("file","array[0]" + array[0]);
                Log.e("file","array[1]" + array[1]);
                fileName = array[0].substring(array[0].lastIndexOf("/")+1) + "." +   suffix;
                Log.e("file","fileName:" + fileName);
            }
        }

        return fileName;
    }

    public static String getFilePath(String homeDir,String fileName) {

        return new File(homeDir, fileName).getAbsolutePath();
    }
}

