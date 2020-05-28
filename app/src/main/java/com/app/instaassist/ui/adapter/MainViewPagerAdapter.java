package com.app.instaassist.ui.adapter;

import android.content.res.Resources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.instaassist.base.Base;
import com.app.instaassist.R;
import com.app.instaassist.ui.fragment.DownloadingFragment;
import com.app.instaassist.ui.fragment.VideoHistoryFragment;



public class MainViewPagerAdapter extends FragmentPagerAdapter {


    public static final CharSequence[] TITLE_ARRAY = new CharSequence[]{"", ""};

    private DownloadingFragment mDownloadingFragment;
    private VideoHistoryFragment mVideoHistoryFragment;

    private Resources mRes;
    private String mParams;

    public MainViewPagerAdapter(FragmentManager fm,String params) {
        super(fm);
        mRes = Base.getInstance().getApplicationContext().getResources();
        mParams = params;
    }

    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mDownloadingFragment = DownloadingFragment.newInstance(mParams);
            case 1:
                return mVideoHistoryFragment = VideoHistoryFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mRes.getString(R.string.tab_title_downloading);
            case 1:
                return mRes.getString(R.string.tab_title_history);
            default:

                return null;
        }
    }

    public DownloadingFragment getDownloadingFragment() {
        return mDownloadingFragment;
    }

    public VideoHistoryFragment getVideoHistoryFragment() {
        return mVideoHistoryFragment;
    }
}
