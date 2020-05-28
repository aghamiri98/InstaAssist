package com.app.instaassist.util;

import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.instaassist.ui.activity.MainActivity;
import com.app.instaassist.base.Base;
import com.app.instaassist.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Utils {

    public static final String EXPIRE_SUFFIX_ARRAY[] = new String[]{};
    public static void openInstagramByUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.instagram.android");
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Base.getInstance().getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static boolean openAppByPackageName(String packageName) {
        try {
            Intent intent = Base.getInstance().getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Base.getInstance().getApplicationContext().startActivity(intent);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void openInstagram() {
        boolean result = openAppByPackageName("com.instagram.android");
        if (!result) {
            goToGpByPackageName(Base.getInstance().getApplicationContext(), "com.instagram.android");
        }
    }

    public static void launchMySelf() {
        Intent intent = new Intent(Base.getInstance().getApplicationContext(), MainActivity.class);
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Base.getInstance().getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static void copyText2Clipboard(String content) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(Base.getInstance().getApplicationContext(), R.string.clipboard_copy_text, Toast.LENGTH_SHORT).show();
        }
        final Context context = Base.getInstance().getApplicationContext();
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public static String getTextFromClipboard() {
        final Context context = Base.getInstance().getApplicationContext();
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String pastContent = cmb.getText().toString();
        if (!TextUtils.isEmpty(pastContent)) {
            String handledUrl = URLMatcher.getHttpURL(pastContent);
            return handledUrl;
        }
        return "";
    }




    public static void goToGpByPackageName(Context context, String packageName) {
        final String appPackageName = packageName;

        try {
            Intent launchIntent = new Intent();
            launchIntent.setPackage("com.android.vending");
            launchIntent.setData(Uri.parse("market://details?id=" + appPackageName));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        } catch (android.content.ActivityNotFoundException anfe) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void rateUs5Star() {
        Context context = Base.getInstance().getApplicationContext();
        goToGpByPackageName(context, context.getPackageName());
    }


    public static void originalShareImage(Context context, String path) {
        Intent share_intent = new Intent();
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri imageContentUri =
                    FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(path));
            imageUris.add(imageContentUri);
        } else {
            imageUris.add(Uri.fromFile(new File(path)));
        }
        share_intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        share_intent.setType(MimeTypeUtil.getMimeTypeByFileName(path));
        share_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        share_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        context.startActivity(Intent.createChooser(share_intent, context.getString(R.string.str_share_this_video)));
    }






    public static void writeFile(String content) {
        File writename = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.txt");
        try {
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            out.write(content);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String replaceEscapteSequence(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return rawUrl;
        }
        rawUrl = rawUrl.replace("&amp;", "&");
        rawUrl = rawUrl.replace("\\/", "/");
        return rawUrl;
    }
}
