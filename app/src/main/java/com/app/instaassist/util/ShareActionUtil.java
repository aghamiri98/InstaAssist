package com.app.instaassist.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.app.instaassist.BuildConfig;

import java.io.File;


public class ShareActionUtil {

    public static final String PKG_INSTAGRAM = "com.instagram.android";
    static String type = "image/*";


    public static void startInstagramShare(Context activity, String imageUrl) {
        ShareActionUtil util = new ShareActionUtil();
        util.createInstagramIntent(activity, type, imageUrl);
    }

    private void createInstagramIntent(Context activity, String type, String mediaPath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage(PKG_INSTAGRAM);
        String mimeType = MimeTypeUtil.getMimeTypeByFileName(mediaPath);
        if(mimeType == null) {
            mimeType = type;
        }
        share.setType(mimeType);
        File media = new File(mediaPath);
        Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider",media);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        share.putExtra(Intent.EXTRA_STREAM, uri);

        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(share);
    }



}
