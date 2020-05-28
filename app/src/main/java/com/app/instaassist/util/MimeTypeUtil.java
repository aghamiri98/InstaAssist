package com.app.instaassist.util;

import java.util.HashMap;
import java.util.HashSet;

public class MimeTypeUtil {

    public static HashMap<String, String> mimeTypeCache;

    public static HashSet<String> mVideoFormat;

    static {

        mimeTypeCache = new HashMap<>();
        mimeTypeCache.put("mp4", "video/mp4");
        mimeTypeCache.put("mov","video/mov");
        mimeTypeCache.put("jpg", "image/*");
        mimeTypeCache.put("png", "image/*");
        mimeTypeCache.put("jpeg", "image/*");
        mimeTypeCache.put("gif", "image/*");

        mVideoFormat = new HashSet<>();
        mVideoFormat.add("mp4");
        mVideoFormat.add("mov");

    }

    public static String getMimeTypeBySuffixName(String fileName) {
        return mimeTypeCache.get(fileName);
    }

    public static String getSuffixByName(String name) {
        String array[] = name.split("\\.");
        return array[array.length - 1];
    }

    public static String getMimeTypeByFileName(String fileName) {
        return getMimeTypeBySuffixName(getSuffixByName(fileName));
    }

    public static boolean isVideoType(String fileName) {
        String array[] = fileName.split("\\.");
        String suffixName = array[array.length - 1];
        return mVideoFormat.contains(suffixName);
    }
}
