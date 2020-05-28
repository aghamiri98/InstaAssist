package com.app.instaassist.ui.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.app.instaassist.base.Constant;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.app.instaassist.R;
import com.app.instaassist.data.model.ItemViewHolder;
import com.app.instaassist.ui.adapter.MainDownloadingRecyclerAdapter;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.downloader.DownloadingTaskList;
import com.app.instaassist.services.downloader.VideoDownloadFactory;
import com.app.instaassist.services.service.DownloadService;
import com.app.instaassist.util.DeviceUtil;
import com.app.instaassist.util.PreferenceUtils;
import com.app.instaassist.util.URLMatcher;

import java.util.List;

public class DownloadingFragment extends Fragment implements View.OnClickListener, MainDownloadingRecyclerAdapter.IBtnCallback {

    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private MainDownloadingRecyclerAdapter mAdapter;
    private List<DownloadContentItem> mDataList;
    private ProgressDialog mProgressDialog;

    public String mReceiveUrlParams;

    private DownloadContentItem mHowToBean = null;

    private boolean isShowHowToPage;

    private String mFormatLeftFileString;
    private RequestManager mRequestMangager;

    private boolean mIsPasteInMain = false;
    private Handler mHandler = new Handler() {};

    private BroadcastReceiver mUpdateDataReceiver;

    public static DownloadingFragment newInstance(String params) {
        DownloadingFragment fragment = new DownloadingFragment();
        fragment.mReceiveUrlParams = params;
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.downloading_page, container, false);

        return view;
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerLocalBroadcast();
        mRequestMangager = Glide.with(getActivity());
        mListView = (RecyclerView) findViewById(R.id.downloading_list);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setItemAnimator(new DefaultItemAnimator());

        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                mDataList = DownloaderDBHelper.SINGLETON.getDownloadingTask();
                DownloadContentItem headerBean = new DownloadContentItem();
                headerBean.itemType = DownloadContentItem.TYPE_HEADER_ITEM;
                mDataList.add(0, headerBean);
                if (!PreferenceUtils.isShowedHowToInfo()) {
                    isShowHowToPage = true;
                    PreferenceUtils.showedHowToInfo();
                    mHowToBean = new DownloadContentItem();
                    mHowToBean.itemType = DownloadContentItem.TYPE_HOWTO_ITEM;
                    mDataList.add(mHowToBean);
                } else {

                }
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        mAdapter = new MainDownloadingRecyclerAdapter(mRequestMangager, mDataList, true, DownloadingFragment.this);
                        mListView.setAdapter(mAdapter);

                    });
                }
            }
        });


        if (!TextUtils.isEmpty(mReceiveUrlParams)) {
            receiveSendAction(mReceiveUrlParams);
        }

        mFormatLeftFileString = getResources().getString(R.string.downloading_left_task_count);

    }

    public void receiveSendAction(String url) {
        startDownload(url);
    }

    private void startDownload(final String url) {
        if (isAdded()) {
            String pageURL = URLMatcher.getHttpURL(url);
            if (VideoDownloadFactory.getInstance().isSupportWeb(pageURL)) {
                showCheckURLProgressDialog();
                Intent intent = new Intent(getActivity(), DownloadService.class);
                intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
                intent.putExtra(DownloadService.EXTRAS_FLOAT_VIEW, false);
                intent.putExtra(Constant.EXTRAS, url);
                getActivity().startService(intent);
            } else {
                Toast.makeText(getActivity(), R.string.not_support_url, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showCheckURLProgressDialog() {
        if (isAdded()) {
            getActivity().runOnUiThread(() -> {
                mProgressDialog = ProgressDialog.show(getActivity(), getActivity().getString(R.string.eheck_url_dialgo_title), getActivity().getString(R.string.check_url), true, true);
                mProgressDialog.show();
            });
        }
    }


    private void registerLocalBroadcast() {
        if (isAdded()) {
            mUpdateDataReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String pageURL = intent.getStringExtra(Constant.KEY_BEAN_PAGE_URL);
                    mHandler.post(() -> {
                        if (TextUtils.isEmpty(pageURL)) {
                            return;
                        }
                        DownloadContentItem bean = new DownloadContentItem();
                        bean.pageURL = pageURL;
                        int index = mDataList.indexOf(bean);
                        if (index > -1) {
                            mAdapter.notifyItemRemoved(mDataList.indexOf(bean));
                            mDataList.remove(bean);
                        } else {
                            mAdapter.notifyItemChanged(index);
                        }
                    });
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constant.ACTION_NOTIFY_DATA_CHANGED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateDataReceiver, intentFilter);
        }
    }

    private void unRegisterLocalBroadcast() {
        if (isAdded()) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateDataReceiver);
        }
    }

    public void publishProgress(final String pageURL, final int filePosition, final int progress) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                DownloadContentItem bean = new DownloadContentItem();
                bean.pageURL = pageURL;
                if (mDataList != null) {
                    int index = mDataList.indexOf(bean);
                    if (index > -1) {
                        DownloadContentItem downloadContentItem = mDataList.get(index);
                        RecyclerView.ViewHolder viewHolder = mListView.findViewHolderForAdapterPosition(index);
                        if (viewHolder != null && viewHolder instanceof ItemViewHolder) {
                            ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
                            itemHolder.progressBar.setVisibility(View.VISIBLE);
                            int leftFileCount = downloadContentItem.fileCount - filePosition;

                            itemHolder.taskCountView.setText(String.format(mFormatLeftFileString, leftFileCount));
                            int count = downloadContentItem.fileCount * 100;
                            int position = filePosition;
                            int totalProgress = position * 100 + progress;
                            int newProgrees = totalProgress * 100 / count;
                            if (newProgrees >= itemHolder.progressBar.getProgress() && newProgrees <= 100) {
                                itemHolder.progressBar.setProgress(newProgrees);
                            }
                        }
                    }
                }
            });
        }
    }

    public void onReceiveNewTask(String pageURL) {
        if (isAdded()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if (TextUtils.isEmpty(pageURL)) {
                return;
            }

            if (pageURL.equals(getString(R.string.toast_downlaoded_video))) {
                return;
            }
            DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
            if (videoBean != null) {
                mDataList.add(1, videoBean);
                mAdapter.notifyItemInserted(1);
            }
        }
    }

    public void onStartDownload(String pageURL) {
        if (isAdded()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if (TextUtils.isEmpty(pageURL)) {
                Toast.makeText(getActivity(), R.string.spider_request_error, Toast.LENGTH_SHORT).show();
                return;
            }

            DownloadContentItem bean = new DownloadContentItem();
            bean.pageURL = pageURL;
            if (!mDataList.contains(bean)) {
                DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
                if (videoBean != null) {
                    mDataList.add(1, videoBean);
                    mAdapter.notifyItemInserted(1);
                }

            }
            if (mIsPasteInMain) {
                mIsPasteInMain = false;
                if (false) {
                    if (System.currentTimeMillis() - PreferenceUtils.getLastLoadFullScreenAD() >= 2 * 60 * 60 * 1000) {
                        mHandler.post(() -> PreferenceUtils.setLoadFullScreenAd());
                    }
                }
            }
        }
    }

    public void downloadFinished(String pageURL) {
        if (TextUtils.isEmpty(pageURL)) {
            return;
        }
        DownloadContentItem bean = new DownloadContentItem();
        bean.pageURL = pageURL;
        int index = mDataList.indexOf(bean);
        if (index > -1) {
            DownloadContentItem downloadContentItem = mDataList.get(index);
            downloadContentItem.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
            mAdapter.notifyItemChanged(index);
        }

        if (false && DeviceUtil.isBeyondTime(2 * 60 * 60 * 1000)) {
            if (isVisible()) {
                if (DownloaderDBHelper.SINGLETON.getDownloadedTaskCount() > 0 && DownloaderDBHelper.SINGLETON.getDownloadingTaskCount() == 0) {
                    mHandler.post(() -> {



                    });
                }
            }
        }
    }



    @Override
    public void onClick(View v) {
    }


    public void showHotToInfo() {
        if (isShowHowToPage) {
            isShowHowToPage = false;
            mDataList.remove(mHowToBean);
            mAdapter.notifyDataSetChanged();
        } else {
            isShowHowToPage = true;
            if (mHowToBean == null) {
                mHowToBean = new DownloadContentItem();
                mHowToBean.itemType = DownloadContentItem.TYPE_HOWTO_ITEM;
            }
            mDataList.add(1, mHowToBean);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void hideHowToInfoCard() {
        if (isShowHowToPage) {
            showHotToInfo();
            PreferenceUtils.showedHowToInfo();
        }
    }





    @Override
    public void showHowTo() {
        showHotToInfo();
    }

    @Override
    public void onDownloadFromClipboard(View view, String inputURL) {
        if (isAdded()) {
            hideInputMethod(view, getActivity());
            String handledUrl = URLMatcher.getHttpURL(inputURL);
            if (TextUtils.isEmpty(handledUrl)) {
                Toast.makeText(getActivity(), R.string.not_support_url, Toast.LENGTH_SHORT).show();
            } else {
                mIsPasteInMain = true;
                startDownload(handledUrl);
            }
        }
    }

    private void hideInputMethod(View view, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void onDestroy() {
        unRegisterLocalBroadcast();
        super.onDestroy();
    }

    public void deleteDownloadFinishedItem(DownloadContentItem downloadContentItem) {
        final int index = mDataList.indexOf(downloadContentItem);
        if (index > -1) {
            mAdapter.notifyItemRemoved(index);
            mDataList.remove(index);

        }
    }

}
