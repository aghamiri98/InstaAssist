package com.app.instaassist.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.app.instaassist.R;
import com.app.instaassist.data.model.ItemViewHolder;
import com.app.instaassist.ui.adapter.MainListRecyclerAdapter;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.downloader.DownloadingTaskList;
import com.app.instaassist.base.Constant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class VideoHistoryFragment extends Fragment {

    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private List<DownloadContentItem> mDataList;
    private MainListRecyclerAdapter mAdapter;

    private MainListRecyclerAdapter.ISelectChangedListener mListener;
    private boolean mInsertFacebookAdStatus = false;
    private int mLastAdInsertedPosition = 0;
    private HashMap<Integer, DownloadContentItem> mBeanMap = new HashMap<>();
    private Handler mMainLooperHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                DownloadContentItem downloadContentItem = (DownloadContentItem) msg.obj;
                if (mListener != null) {
                    mListener.onDeleteDownloadItem(downloadContentItem);
                }
                final int index = mDataList.indexOf(downloadContentItem);
                if (index > -1) {
                    mAdapter.notifyItemRemoved(index);
                    mDataList.remove(index);
                }
            } else if (msg.what == 1) {
                mAdapter.clearSelectedList();
                if (mDataList.size() == 0) {
                    mAdapter.quitSelectMode();
                    if (mListener != null) {
                        mListener.onQuitSelectMode();
                    }
                }
            }
        }
    };

    private BroadcastReceiver mUpdateDataReceiver;

    public static VideoHistoryFragment newInstance() {
        VideoHistoryFragment fragment = new VideoHistoryFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_page, container, false);
        return view;
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (RecyclerView) findViewById(R.id.video_history_list);
        mListView.setHasFixedSize(true);
        mListView.setItemAnimator(new DefaultItemAnimator());
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mListView.setLayoutManager(mLayoutManager);

        mListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (!mInsertFacebookAdStatus) {
                            return;
                        }
                        int visibleItemCount = mLayoutManager.getChildCount();
                        int totalItemCount = mLayoutManager.getItemCount();
                        int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
                        Log.v("fan", visibleItemCount + ":" + totalItemCount + ":" + firstVisibleItemPosition + ":" + lastVisibleItemPosition);
                        if (firstVisibleItemPosition < 2) {
                            return;
                        }
                        View view = null;
                        boolean haveFacebookAdInScreen = false;
                        for (int index = 0; index < visibleItemCount; index++) {
                            view = mListView.getChildAt(index);
                            if (view != null) {
                                DownloadContentItem downloadContentItem = (DownloadContentItem) view.getTag();
                                if (downloadContentItem != null) {
                                    if (downloadContentItem.itemType == DownloadContentItem.TYPE_FACEBOOK_AD) {
                                        haveFacebookAdInScreen = true;
                                    }
                                }
                            }
                        }

                        if (!haveFacebookAdInScreen) {
                            DownloadContentItem downloadContentItem = mBeanMap.get(mLastAdInsertedPosition % 2);
                            if (downloadContentItem != null) {
                                int adPosition = lastVisibleItemPosition - 1;
                                mDataList.add(adPosition, downloadContentItem);
                                mAdapter.notifyItemInserted(adPosition);
                                mLastAdInsertedPosition += 1;
                            }
                        }

                        break;

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {

                    mInsertFacebookAdStatus = true;
                } else {
                    mInsertFacebookAdStatus = false;
                }
            }
        });
        initData();
    }

    private void initData() {
        registerLocalBroadcast();
        DownloadingTaskList.SINGLETON.getExecutorService().execute(() -> {
            mDataList = DownloaderDBHelper.SINGLETON.getDownloadedTask();

            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    mAdapter = new MainListRecyclerAdapter(mDataList, false);
                    mAdapter.setLayoutManager(mLayoutManager);
                    mListView.setAdapter(mAdapter);
                });
            }
        });


    }

    public void onAddNewDownloadedFile(String pageURL) {
        if (mDataList != null) {
            DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
            if (videoBean != null) {
                final int index = mDataList.indexOf(videoBean);
                if (index < 0) {
                    mDataList.add(0, videoBean);
                    mAdapter.notifyItemInserted(0);
                    mListView.smoothScrollToPosition(0);
                } else {
                    RecyclerView.ViewHolder viewHolder = mListView.findViewHolderForAdapterPosition(index);
                    if (viewHolder != null && viewHolder instanceof ItemViewHolder) {
                        ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;
                        itemHolder.circleProgress.setVisibility(View.GONE);
                        DownloadContentItem downloadContentItem = mDataList.get(index);
                        downloadContentItem.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED;
                    }
                }
            }
        }
    }


    private void registerLocalBroadcast() {
        if (isAdded()) {
            mUpdateDataReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String pageURL = intent.getStringExtra(Constant.KEY_BEAN_PAGE_URL);
                    mMainLooperHandler.post(() -> {
                        if (TextUtils.isEmpty(pageURL)) {
                            return;
                        }
                        DownloadContentItem bean = new DownloadContentItem();
                        bean.pageURL = pageURL;
                        int index = mDataList.indexOf(bean);
                        if (index > -1) {
                            mAdapter.notifyItemRemoved(mDataList.indexOf(bean));
                            mDataList.remove(bean);
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
                            itemHolder.circleProgress.setVisibility(View.VISIBLE);
                            int count = downloadContentItem.fileCount * 100;
                            int position = filePosition;
                            int totalProgress = position * 100 + progress;
                            int newProgrees = totalProgress * 100 / count;
                            if (newProgrees >= itemHolder.circleProgress.getProgress() && newProgrees <= 100) {
                                itemHolder.circleProgress.setProgress(newProgrees);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        unRegisterLocalBroadcast();
        super.onDestroy();
    }


    public void setISelectChangedListener(MainListRecyclerAdapter.ISelectChangedListener listener) {
        mListener = listener;
        if (mAdapter != null) {
            mAdapter.setISelectChangedListener(listener);
        }
    }

    public void selectAll() {
        if (mAdapter != null) {
            mAdapter.selectAll();
        }
    }

    public void quitSelectMode() {
        if (mAdapter != null) {
            mAdapter.quitSelectMode();
        }
    }

    public boolean isSelectMode() {
        if (mAdapter != null) {
            return mAdapter.isSelectMode();
        }

        return false;
    }

    public void deleteSelectItems() {
        final HashSet<DownloadContentItem> selectDataList = mAdapter.getSelectList();
        final DownloaderDBHelper dbHelper = DownloaderDBHelper.SINGLETON;

        DownloadingTaskList.SINGLETON.getExecutorService().execute(() -> {
            Iterator<DownloadContentItem> itemIterator = selectDataList.iterator();
            while (itemIterator.hasNext()) {
                DownloadContentItem downloadContentItem = itemIterator.next();
                dbHelper.deleteDownloadContentItem(downloadContentItem);
                mMainLooperHandler.obtainMessage(0, downloadContentItem).sendToTarget();
            }
            mMainLooperHandler.sendEmptyMessage(1);
        });

    }
}
