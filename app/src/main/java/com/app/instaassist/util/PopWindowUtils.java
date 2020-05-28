package com.app.instaassist.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.app.instaassist.base.Base;
import com.app.instaassist.R;
import com.app.instaassist.ui.adapter.MainListRecyclerAdapter;

public class PopWindowUtils {


    public static void showVideoMoreOptionWindow(View trigerView, boolean showDelete, final MainListRecyclerAdapter.IPopWindowClickCallback callback) {
        Context context = Base.getInstance().getApplicationContext();
        View contentView = LayoutInflater.from(context).inflate(R.layout.more_option, null);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                dip2px(100), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor((v, event) -> false);

        contentView.findViewById(R.id.copy_all).setOnClickListener(v -> {
            if (callback != null) {
                callback.onCopyAll();
            }

            popupWindow.dismiss();
        });

        contentView.findViewById(R.id.copy_hashtags).setOnClickListener(v -> {
            if (callback != null) {
                callback.onCopyHashTags();
            }

            popupWindow.dismiss();
        });
        contentView.findViewById(R.id.launch).setOnClickListener(v -> {
            if (callback != null) {
                callback.launchAppByUrl();
            }

            popupWindow.dismiss();
        });

        contentView.findViewById(R.id.copy).setOnClickListener(v -> {
            if (callback != null) {
                callback.onPasteSharedUrl();
            }

            popupWindow.dismiss();
        });
        if (showDelete) {
            TextView deleteTv = (TextView) contentView.findViewById(R.id.redownload);
            deleteTv.setText(R.string.menu_delete);
        }

        contentView.findViewById(R.id.redownload).setOnClickListener(v -> {
            if (callback != null) {
                callback.onStartDownload();
            }

            popupWindow.dismiss();
        });
        int windowPos[] = calculatePopWindowPos(trigerView, contentView);
        popupWindow.showAsDropDown(trigerView, -dip2px(100) + trigerView.getWidth() / 2, windowPos[1]);
    }


    public static int dip2px(float dipValue) {
        final float scale = Base.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    public static void showPlayVideoMorePopWindow(View trigerView, final IPopWindowCallback callback) {
        Context context = Base.getInstance().getApplicationContext();
        View contentView = LayoutInflater.from(context).inflate(R.layout.videoplay_more_option, null);
        View deleteView = contentView.findViewById(R.id.share);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                dip2px(100), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor((v, event) -> false);
        contentView.findViewById(R.id.repost).setOnClickListener(v -> {
            if (callback != null) {
                callback.onRepost();
            }

            popupWindow.dismiss();

        });
        deleteView.setOnClickListener(v -> {
            if (callback != null) {
                callback.onShare();
            }

            popupWindow.dismiss();

        });

        contentView.findViewById(R.id.launch).setOnClickListener(v -> {
            if (callback != null) {
                callback.launchInstagram();
            }

            popupWindow.dismiss();
        });

        contentView.findViewById(R.id.copy).setOnClickListener(v -> {
            if (callback != null) {
                callback.onPastePageUrl();
            }

            popupWindow.dismiss();
        });

        contentView.findViewById(R.id.delete).setOnClickListener(v -> {
            if (callback != null) {
                callback.onDelete();
            }

            popupWindow.dismiss();
        });
        popupWindow.showAsDropDown(trigerView, -dip2px(100) + trigerView.getWidth() / 2, -dip2px(15));
    }

    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        anchorView.getLocationOnScreen(anchorLoc);

        final int screenHeight = DeviceUtil.getScreenHeight();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        final boolean isNeedShowUp = (anchorLoc[1] + windowHeight > screenHeight);
        if (isNeedShowUp) {
            windowPos[1] = (int) anchorView.getY() - windowHeight - anchorView.getHeight();
        } else {
            windowPos[1] = -dip2px(10);
        }
        windowPos[0] = (int) anchorView.getX() - windowWidth - windowWidth;
        return windowPos;
    }


    public interface IPopWindowCallback {
        void onRepost();

        void onShare();

        void launchInstagram();

        void onPastePageUrl();

        void onDelete();
    }
}
