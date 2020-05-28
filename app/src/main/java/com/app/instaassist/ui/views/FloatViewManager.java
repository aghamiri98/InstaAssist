package com.app.instaassist.ui.views;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.app.instaassist.base.Base;
import com.app.instaassist.util.DeviceUtil;

public class FloatViewManager {


    private FloatNotificationView mFloatView;
    private Context mContext;

    private WindowManager mWindowManager;

    private static final FloatViewManager sInstance = new FloatViewManager();

    public static FloatViewManager getDefault() {
        return sInstance;
    }

    private FloatViewManager() {
        mContext = Base.getInstance().getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mFloatView = new FloatNotificationView(mContext);
    }


    public void showFloatView() {
        if (mFloatView != null && mFloatView.getParent() != null) {
            return;
        }
        try {
            boolean canShowFloatView = true;
            if (Build.VERSION.SDK_INT >= 23) {
                canShowFloatView = false;
                if (Settings.canDrawOverlays(mContext)) {
                    canShowFloatView = true;
                }
            }

            if (canShowFloatView) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT >= 26) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else if (Build.VERSION.SDK_INT >= 24) {
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                } else if (Build.VERSION.SDK_INT >= 19) {
                    params.type = WindowManager.LayoutParams.TYPE_TOAST;
                    try {
                        String obj = Build.MODEL;
                        if (!TextUtils.isEmpty(obj) && obj.toLowerCase().contains("vivo") && Build.VERSION.SDK_INT > 19 && Build.VERSION.SDK_INT < 23) {
                            params.type = WindowManager.LayoutParams.TYPE_PHONE;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                params.format = PixelFormat.RGBA_8888;
                params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                params.dimAmount = 0.5f;
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.x = DeviceUtil.getScreenWidth() - dip2px(100);
                params.y = DeviceUtil.getScreenHeight() - dip2px(200);
                mFloatView.setWindowManager(mWindowManager);
                mFloatView.setLayoutParams(params);
                mWindowManager.addView(mFloatView, params);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static int dip2px(float dipValue) {
        final float scale = Base.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void dismissFloatView() {
        if (mFloatView != null && mFloatView.getParent() != null) {
            mWindowManager.removeViewImmediate(mFloatView);
        }
    }
}
