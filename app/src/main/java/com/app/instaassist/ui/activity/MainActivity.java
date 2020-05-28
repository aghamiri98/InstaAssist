package com.app.instaassist.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.app.instaassist.R;
import com.app.instaassist.base.Constant;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.service.DownloadService;
import com.app.instaassist.services.service.IDownloadBinder;
import com.app.instaassist.services.service.IDownloadCallback;
import com.app.instaassist.services.service.TLRequestParserService;
import com.app.instaassist.ui.adapter.MainListRecyclerAdapter;
import com.app.instaassist.ui.adapter.MainViewPagerAdapter;
import com.app.instaassist.ui.views.FloatViewManager;
import com.app.instaassist.util.URLMatcher;
import com.app.instaassist.util.Utils;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, MainListRecyclerAdapter.ISelectChangedListener {

    private static final int PERMISSION_REQUEST_CODE = 200;

    private ViewPager mMainViewPager;
    private MainViewPagerAdapter mViewPagerAdapter;
    private TabLayout mTabLayout;

    private int mCurrentPagePosition = 0;

    private View mInstagramIcon;
    private View mSelectedContainer;

    private void init() {
        Intent intent = new Intent(this, TLRequestParserService.class);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscribeDownloadService();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
        cancelAllNotification();

        if (checkPermission()) {


        } else {
            requestPermission();

        }


        FloatViewManager.getDefault().dismissFloatView();
        mInstagramIcon = findViewById(R.id.ins_icon);
        mInstagramIcon.setOnClickListener(this);
        mSelectedContainer = findViewById(R.id.select_container);
        findViewById(R.id.ic_delete).setOnClickListener(this);
        findViewById(R.id.ic_select).setOnClickListener(this);
        findViewById(R.id.ic_undo).setOnClickListener(this);
        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(2);
        FragmentManager fm = getSupportFragmentManager();

        String params = null;
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            params = URLMatcher.getHttpURL(sharedText);
        }
        mViewPagerAdapter = new MainViewPagerAdapter(fm, params);
        mMainViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.slidindg_tabs);
        mTabLayout.setupWithViewPager(mMainViewPager);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPagePosition = position;
                if (position == 1) {
                    if (mViewPagerAdapter.getVideoHistoryFragment() != null) {
                        mViewPagerAdapter.getVideoHistoryFragment().setISelectChangedListener(MainActivity.this);

                        if (mViewPagerAdapter.getVideoHistoryFragment().isSelectMode()) {
                            mInstagramIcon.setVisibility(View.GONE);
                            mSelectedContainer.setVisibility(View.VISIBLE);
                        }
                    }


                } else {
                    mInstagramIcon.setVisibility(View.VISIBLE);
                    mSelectedContainer.setVisibility(View.GONE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);


    }

    private void handleSendIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                String url = URLMatcher.getHttpURL(sharedText);
                if (mViewPagerAdapter.getDownloadingFragment() != null) {
                    mViewPagerAdapter.getDownloadingFragment().receiveSendAction(url);
                }
            }
        }
    }


    private void cancelAllNotification() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.ACTION_CANCEL_ALL_NOTIFICATION);
        startService(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSendIntent();
    }

    @Override
    public void onBackPressed() {
            if (mViewPagerAdapter != null && mViewPagerAdapter.getVideoHistoryFragment() != null && mViewPagerAdapter.getVideoHistoryFragment().isSelectMode()) {
                mViewPagerAdapter.getVideoHistoryFragment().quitSelectMode();
                return;
            }
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            if (Constant.TEST_FOR_GP) {

            }
            Utils.openInstagram();
        } else if (id == R.id.nav_gallery) {
            if (mViewPagerAdapter.getDownloadingFragment() != null) {
                mViewPagerAdapter.getDownloadingFragment().showHotToInfo();
            }
        }

        return true;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ins_icon) {
            Utils.openInstagram();
        } else if (v.getId() == R.id.ic_select) {
            if (mViewPagerAdapter.getVideoHistoryFragment() != null) {
                mViewPagerAdapter.getVideoHistoryFragment().selectAll();
            }
        } else if (v.getId() == R.id.ic_undo) {
            mSelectedContainer.setVisibility(View.GONE);
            mInstagramIcon.setVisibility(View.VISIBLE);
            if (mViewPagerAdapter.getVideoHistoryFragment() != null) {
                mViewPagerAdapter.getVideoHistoryFragment().quitSelectMode();
            }
        } else if (v.getId() == R.id.ic_delete) {
            if (mViewPagerAdapter.getVideoHistoryFragment() != null) {
                mViewPagerAdapter.getVideoHistoryFragment().deleteSelectItems();
            }
        }
    }


    private void subscribeDownloadService() {
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private IDownloadBinder mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IDownloadBinder.Stub.asInterface(service);

            try {
                mService.registerCallback(mRemoteCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mService.unregisterCallback(mRemoteCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
        }
    };

    private IDownloadCallback.Stub mRemoteCallback = new IDownloadCallback.Stub() {
        @Override
        public void onPublishProgress(final String pageURL, final int filePostion, final int progress) throws RemoteException {

            if (isFinishing()) {
                return;
            }

            if (mViewPagerAdapter != null && mViewPagerAdapter.getDownloadingFragment() != null) {
                mViewPagerAdapter.getDownloadingFragment().publishProgress(pageURL, filePostion, progress);
            }
            if (mViewPagerAdapter != null && mViewPagerAdapter.getVideoHistoryFragment() != null) {
                mViewPagerAdapter.getVideoHistoryFragment().publishProgress(pageURL, filePostion, progress);
            }

        }

        @Override
        public void onReceiveNewTask(final String pageURL) throws RemoteException {
            if (isFinishing()) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPagerAdapter.getDownloadingFragment() != null) {
                        mViewPagerAdapter.getDownloadingFragment().onReceiveNewTask(pageURL);
                    }
                }
            });
        }

        @Override
        public void onStartDownload(final String path) throws RemoteException {
            if (isFinishing()) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPagerAdapter.getDownloadingFragment() != null) {
                        mViewPagerAdapter.getDownloadingFragment().onStartDownload(path);
                    }
                }
            });

        }

        @Override
        public void onDownloadSuccess(final String path) throws RemoteException {

            if (isFinishing()) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPagerAdapter != null) {
                        if (mViewPagerAdapter.getDownloadingFragment() != null) {
                            mViewPagerAdapter.getDownloadingFragment().downloadFinished(path);
                        }

                        if (mViewPagerAdapter.getVideoHistoryFragment() != null) {
                            mViewPagerAdapter.getVideoHistoryFragment().onAddNewDownloadedFile(path);
                        }

                        if (DownloaderDBHelper.SINGLETON.getDownloadedTaskCount() > 1) {

                            //     showRatingDialog();
                        }
                    }
                }
            });
        }

        @Override
        public void onDownloadFailed(String path) throws RemoteException {

        }
    };

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        Glide.get(this).clearMemory();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mViewPagerAdapter.getDownloadingFragment() != null) {
            mViewPagerAdapter.getDownloadingFragment().hideHowToInfoCard();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onEnterSelectMode() {
        mInstagramIcon.setVisibility(View.GONE);
        mSelectedContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onQuitSelectMode() {
        mInstagramIcon.setVisibility(View.VISIBLE);
        mSelectedContainer.setVisibility(View.GONE);
    }

    @Override
    public void onDeleteDownloadItem(DownloadContentItem downloadContentItem) {
        if (mViewPagerAdapter.getDownloadingFragment() != null) {
            mViewPagerAdapter.getDownloadingFragment().deleteDownloadFinishedItem(downloadContentItem);
        }
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted)
                        Toast.makeText(this, "Permission Granted, Now you can access this app.", Toast.LENGTH_LONG).show();

                    else {
                        Toast.makeText(this, "Permission Denied, You cannot use this app.", Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
