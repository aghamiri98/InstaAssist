package com.app.instaassist.ui.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.instaassist.data.model.ItemHeaderHolder;
import com.app.instaassist.data.model.ItemHowToHolder;
import com.app.instaassist.data.model.ItemViewHolder;
import com.app.instaassist.data.model.NativeAdItemHolder;
import com.app.instaassist.util.PreferenceUtils;
import com.bcgdv.asia.lib.fanmenu.FanMenuButtons;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.app.instaassist.R;
import com.app.instaassist.base.Base;
import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.data.db.DownloaderDBHelper;
import com.app.instaassist.services.downloader.DownloadingTaskList;
import com.app.instaassist.util.DownloadUtil;
import com.app.instaassist.util.PopWindowUtils;
import com.app.instaassist.util.Utils;

import java.util.List;



public class MainDownloadingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DownloadContentItem> mDataList;
    private RequestManager imageLoader;
    private boolean mFullImageState = false;
    private Context mContext;
    private IBtnCallback callback;
    private boolean mClickedPasteBtn = false;
    private Resources mResources;
    private String mLeftDownloadFileString;

    public MainDownloadingRecyclerAdapter(RequestManager requestManager, List<DownloadContentItem> dataList, boolean isFullImage, IBtnCallback callback) {
        mDataList = dataList;
        imageLoader = requestManager;
        mFullImageState = isFullImage;
        mContext = Base.getInstance().getApplicationContext();
        this.callback = callback;
        mClickedPasteBtn = false;
        mResources = mContext.getResources();
        mLeftDownloadFileString = mResources.getString(R.string.downloading_left_task_count);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == DownloadContentItem.TYPE_FACEBOOK_AD) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.facebook_native_item, parent, false);

            return new NativeAdItemHolder(itemView);
        } else if (viewType == DownloadContentItem.TYPE_HEADER_ITEM) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new ItemHeaderHolder(itemView);
        } else if (viewType == DownloadContentItem.TYPE_HOWTO_ITEM) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_how_to, parent, false);
            return new ItemHowToHolder(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout_second, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder baseHolder, final int position) {
        final DownloadContentItem bean = mDataList.get(position);

        if (baseHolder instanceof ItemViewHolder) {
            final ItemViewHolder holder = (ItemViewHolder) baseHolder;
            if (holder.fanMenuButtons.getVisibility() == View.VISIBLE) {
                holder.fanMenuButtons.toggleShow();
            }
            holder.operationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.fanMenuButtons.toggleShow();
                }
            });
            if (DownloadingTaskList.SINGLETON.isPendingDownloadTask(bean.pageURL)) {
                holder.progressBar.setProgress(0);
                holder.progressBar.setVisibility(View.VISIBLE);
            } else {
                holder.progressBar.setVisibility(View.GONE);
            }
            holder.fanMenuButtons.setOnFanButtonClickListener(new FanMenuButtons.OnFanClickListener() {
                @Override
                public void onFanButtonClicked(int index) {
                    holder.fanMenuButtons.toggleShow();
                    if (index == 0) {
                       // EventUtil.getDefault().onEvent("downloading", "startDownload");
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                        DownloadUtil.startResumeDownload(bean.pageURL);
                    } else if (index == 1) {
                        DownloadUtil.downloadThumbnail(bean.pageURL, bean.pageThumb);
                    } else if (index == 2) {
                        pauseDownloadingVideo(bean);
                    } else if (index == 3) {
                        deleteDownloadingVideo(bean);
                        sendDeleteVideoBroadcast(bean.pageURL);
                    }
                }
            });

            holder.thumbnailView.setOnClickListener(v -> {
                if (bean.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FAILED) {
                    bean.pageStatus = DownloadContentItem.PAGE_STATUS_DOWNLOADING;
                    holder.progressBar.setProgress(0);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    DownloadUtil.startResumeDownload(bean.pageURL);
                } else if (bean.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED) {
                    DownloadUtil.openFileList(bean.pageHOME);
                }
            });
            if (TextUtils.isEmpty(bean.pageTags)) {
                holder.hashTagView.setVisibility(View.GONE);
            } else {
                holder.hashTagView.setVisibility(View.VISIBLE);
                holder.hashTagView.setText(bean.pageTags);
            }
            holder.taskCountView.setText(String.format(mLeftDownloadFileString, bean.fileCount));
            holder.playView.setVisibility(bean.mimeType == bean.PAGE_MIME_TYPE_VIDEO ? View.VISIBLE : View.GONE);
            try {
                imageLoader.load(bean.pageThumb).centerCrop().priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.thumbnailView);
            } catch (OutOfMemoryError error) {
                System.gc();
                System.gc();
                System.gc();
            }
            if (TextUtils.isEmpty(bean.pageTitle)) {
                holder.titleTv.setVisibility(View.GONE);
            } else {
                holder.titleTv.setText(bean.pageTitle);
                holder.titleTv.setVisibility(View.VISIBLE);
            }

            if (bean.pageStatus == DownloadContentItem.PAGE_STATUS_DOWNLOAD_FINISHED) {
                holder.moreIv.setVisibility(View.VISIBLE);
                holder.taskCountView.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                holder.operationBtn.setVisibility(View.GONE);
                holder.moreIv.setOnClickListener(v -> PopWindowUtils.showVideoMoreOptionWindow(v, true, new MainListRecyclerAdapter.IPopWindowClickCallback() {
                    @Override
                    public void onCopyAll() {
                        String title = bean.pageTitle;
                        String hashTags = bean.pageTags;
                        StringBuilder sb = new StringBuilder(bean.pageURL);
                        if (!TextUtils.isEmpty(title)) {
                            sb.append(title);
                        }

                        if (!TextUtils.isEmpty(hashTags)) {
                            sb.append(hashTags);
                        }

                        Utils.copyText2Clipboard(sb.toString());

                    }

                    @Override
                    public void onCopyHashTags() {
                        String hashTags = bean.pageTags;
                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(hashTags)) {
                            sb.append(hashTags);
                            Utils.copyText2Clipboard(sb.toString());
                        }
                    }

                    @Override
                    public void launchAppByUrl() {
                        if (bean != null && !TextUtils.isEmpty(bean.pageURL)) {
                            Utils.openInstagramByUrl(bean.pageURL);
                        }
                    }

                    @Override
                    public void onPasteSharedUrl() {
                        if (bean != null && !TextUtils.isEmpty(bean.pageURL)) {
                            Utils.copyText2Clipboard(bean.pageURL);
                        }

                    }

                    @Override
                    public void onShare() {
                    }

                    @Override
                    public void onStartDownload() {
                        deleteDownloadingVideo(bean);
                        sendDeleteVideoBroadcast(bean.pageURL);
                    }
                }));
            } else {
                holder.moreIv.setVisibility(View.GONE);
                holder.operationBtn.setVisibility(View.VISIBLE);
                holder.taskCountView.setVisibility(View.VISIBLE);
            }
        } else if (baseHolder instanceof NativeAdItemHolder) {
            final NativeAdItemHolder holder = (NativeAdItemHolder) baseHolder;

        } else if (baseHolder instanceof ItemHeaderHolder) {
            final ItemHeaderHolder holder = (ItemHeaderHolder) baseHolder;
            holder.showHowToBtn.setOnClickListener(v -> {
                if (callback != null) {
                    callback.showHowTo();
                }
            });
            holder.inputUrl.setText(bean.pageURL);
            if (!TextUtils.isEmpty(bean.pageURL)) {
                if (callback != null) {
                    callback.onDownloadFromClipboard(holder.inputUrl, holder.inputUrl.getText().toString());
                }
            }


            holder.downloadBtn.setOnClickListener(v -> {
                mClickedPasteBtn = true;
                if (callback != null) {
                    callback.onDownloadFromClipboard(holder.inputUrl, holder.inputUrl.getText().toString());
                }

                holder.inputUrl.setText("");
            });
        }

    }


    private void sendDeleteVideoBroadcast(String pageURL) {
        Intent intent = new Intent(PreferenceUtils.ACTION_NOTIFY_DATA_CHANGED);
        intent.putExtra(PreferenceUtils.KEY_BEAN_PAGE_URL, pageURL);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void deleteDownloadingVideo(final DownloadContentItem bean) {
        int index = mDataList.indexOf(bean);
        if (index > -1) {
            notifyItemRemoved(index);
            mDataList.remove(index);
        }
        DownloadingTaskList.SINGLETON.intrupted(bean.pageURL);
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                DownloaderDBHelper.SINGLETON.deleteDownloadTask(bean.pageURL);
            }
        });
    }

    private void pauseDownloadingVideo(final DownloadContentItem bean) {
        DownloadingTaskList.SINGLETON.intrupted(bean.pageURL);
        int index = mDataList.indexOf(bean);
        if (index > -1) {
            notifyItemChanged(index);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).itemType;
    }


    public interface IBtnCallback {
        void showHowTo();

        void onDownloadFromClipboard(View view, String httpURL);

    }


    private String getPrimaryContent() {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        ClipData.Item item = data.getItemAt(0);
        String content = item.getText().toString();
        return content;
    }

}