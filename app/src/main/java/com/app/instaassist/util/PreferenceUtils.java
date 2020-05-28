package com.app.instaassist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.instaassist.base.Base;

public class PreferenceUtils {


    public static final String ACTION_CHANGE_LOCALE = "action_change_locale";
    public static final String EXTRAS = "extras";
    public static final boolean TEST_FOR_GP  = false;

    public static final String ACTION_NOTIFY_DATA_CHANGED = "action_data_changed";
    public static final String KEY_BEAN_PAGE_URL = "KEY_BEAN_PAGE_URL";

    private static SharedPreferences mMainSharedPreference;

    public static final String PREFERNCE_FILE_NAME = "shared_pfs";
    public static final String FIRST_LAUNCH = "first_launch";

    public static final String LAST_LOAD_FULL_AD = "last_load_full_ad";

    private static SharedPreferences getSharedPreferences() {
        if (mMainSharedPreference == null) {
            Context context = Base.getInstance();
            mMainSharedPreference = context.getSharedPreferences(PREFERNCE_FILE_NAME, Context.MODE_MULTI_PROCESS);
        }
        return mMainSharedPreference;
    }

    public static boolean isShowedHowToInfo() {
        getSharedPreferences();
        boolean result = mMainSharedPreference.getBoolean(FIRST_LAUNCH, false);
        return result;
    }

    public static void showedHowToInfo() {
        getSharedPreferences();
        mMainSharedPreference.edit().putBoolean(FIRST_LAUNCH,true).commit();
    }


    public static void setLoadFullScreenAd() {
        getSharedPreferences();
        mMainSharedPreference.edit().putLong(LAST_LOAD_FULL_AD,System.currentTimeMillis()).commit();
    }

    public static long getLastLoadFullScreenAD() {
        getSharedPreferences();
        return mMainSharedPreference.getLong(LAST_LOAD_FULL_AD,0);
    }

}
