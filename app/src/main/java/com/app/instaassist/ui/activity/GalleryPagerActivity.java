package com.app.instaassist.ui.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.app.instaassist.R;
import com.app.instaassist.base.Base;
import com.app.instaassist.ui.adapter.ImageGalleryPagerAdapter;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.downloader.DownloadingTaskList;
import com.app.instaassist.util.PopWindowUtils;
import com.app.instaassist.util.PreferenceUtils;
import com.app.instaassist.util.ShareActionUtil;
import com.app.instaassist.util.Utils;
import com.app.instaassist.ui.views.MobMediaView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GalleryPagerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MAX_COUNT_THREHOLD = 4;

    private ViewPager mMainViewPager;
    private TextView mCountInfoView;

    private ImageGalleryPagerAdapter mAdapter;
    private List<PagerBean> mDataList;
    private MobMediaView mSelectedMobView;

    private String mPageHome;


    private int mSelectedPosition = 0;

    private PagerBean mAdBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //    EventUtil.getDefault().onEvent("UI", "GalleryPageActivity.onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery_pager);

        String baseHome = getIntent().getStringExtra(PreferenceUtils.EXTRAS);

        mPageHome = baseHome;
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.more_vert).setOnClickListener(this);
        mCountInfoView = (TextView) findViewById(R.id.count_info);
        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(2);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mSelectedPosition = position;
                MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(position);
                mSelectedMobView = itemView;
                if (itemView != null) {
                    itemView.play();
                }
                mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1 + position, mDataList.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:
                        if (mSelectedMobView != null) {
                            mSelectedMobView.stop();
                        }
                        break;
                }
            }
        });

        final String home = baseHome;
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File targetFile = new File(home);
                        if (targetFile == null) {
                            return;
                        }
                        mDataList = new ArrayList<PagerBean>();
                        if (targetFile.listFiles() != null && targetFile.listFiles().length > 0) {
                            for (File file : targetFile.listFiles()) {
                                PagerBean bean = new PagerBean();
                                bean.file = file;
                                mDataList.add(bean);
                            }

                            Collections.sort(mDataList, new PagerBeanComparator());
                            if (mDataList.size() > 1 && mDataList.size() < 4) {
                             

                            } else {

                            }
                            mAdapter = new ImageGalleryPagerAdapter(GalleryPagerActivity.this, mDataList);
                            mMainViewPager.setAdapter(mAdapter);
                            if (mDataList.size() == 1) {
                                mCountInfoView.setVisibility(View.GONE);
                            }
                            mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1, mDataList.size()));
                            if (mDataList.size() >= MAX_COUNT_THREHOLD) {
                               
                            }
                        }
                    }
                });
            }
        });
    }




    @Override
    protected void onResume() {
        super.onResume();
        if (mDataList != null && mStopCurrentPosition > -1) {
            MobMediaView mobMediaView = (MobMediaView) mMainViewPager.findViewWithTag(mStopCurrentPosition);
            mobMediaView.resume();
        }
    }

    private int mStopCurrentPosition = -1;

    @Override
    protected void onStop() {
        super.onStop();
        if (mDataList != null) {
            mStopCurrentPosition = mSelectedPosition;
            MobMediaView mobMediaView = (MobMediaView) mMainViewPager.findViewWithTag(mStopCurrentPosition);
            if (mobMediaView != null) {
                mobMediaView.stop();
            }
        }

    }




    public static class PagerBean {
        public File file;
       // public NativeAd facebookNativeAd;


        @Override
        public boolean equals(Object obj) {

            if (obj instanceof PagerBean) {
                PagerBean right = (PagerBean) obj;
                if (file != null && right.file != null) {
                    return file.getAbsolutePath().equals(right.file.getAbsolutePath());
                }

                if ((file != null && right.file == null) || (file == null && right.file != null)) {
                    return false;
                }

            }
            return false;
        }
    }

    public class PagerBeanComparator implements Comparator<PagerBean> {


        @Override
        public int compare(PagerBean o1, PagerBean o2) {
            return (int) (o2.file.lastModified() - o1.file.lastModified());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.more_vert) {
            PopWindowUtils.showPlayVideoMorePopWindow(v, new PopWindowUtils.IPopWindowCallback() {


                @Override
                public void onDelete() {
                    MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(mSelectedPosition);
                    String filePath = itemView.getMediaSource();
                    if (!TextUtils.isEmpty(filePath)) {
                        PagerBean bean = new PagerBean();
                        bean.file = new File(filePath);
                        mAdapter.deleteItem(bean, itemView);
                        mMainViewPager.setAdapter(mAdapter);

                        if (mDataList.size() == 0) {
                            GalleryPagerActivity.this.finish();
                            return;
                        }
                        if (mDataList.size() == 1) {
                            mCountInfoView.setVisibility(View.GONE);
                        }
                        mCountInfoView.setText(getResources().getString(R.string.file_count_format, 1, mDataList.size()));
                    }
                }

                @Override
                public void onRepost() {
                    MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(mSelectedPosition);
                    ShareActionUtil.startInstagramShare(Base.getInstance().getApplicationContext(), itemView.getMediaSource());
                }

                @Override
                public void onShare() {
                    MobMediaView itemView = (MobMediaView) mMainViewPager.findViewWithTag(mSelectedPosition);
                    //  Utils.startShareIntent(itemView.getMediaSource());

                    Utils.originalShareImage(GalleryPagerActivity.this, itemView.getMediaSource());
                }

                @Override
                public void launchInstagram() {
                    DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageHome(mPageHome);
                    if (videoBean != null) {
                        Utils.openInstagramByUrl(videoBean.pageURL);
                    }
                }

                @Override
                public void onPastePageUrl() {
                    DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageHome(mPageHome);
                    if (videoBean != null) {
                        Utils.copyText2Clipboard(videoBean.pageHOME);
                    }
                }
            });
        }
    }
}
