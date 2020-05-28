package com.app.instaassist.ui.views;

import android.content.Context;

import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.app.instaassist.R;
import com.app.instaassist.ui.activity.GalleryPagerActivity;
import com.app.instaassist.util.MimeTypeUtil;


public class MobMediaView extends FrameLayout {

    private View mContentView;
    private String mMediaSource;

    private PinchImageView mImageView;
    private VideoView mVideoView;
    private View mVideoIcon;

    private RequestManager mImageLoader;

    private boolean mIsVideoMimeType = false;

    public MobMediaView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MobMediaView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        init(context);
    }


    private void init(Context context) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.media_view_content, null);
        addView(mContentView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mImageLoader = Glide.with(context);

        mImageView = (PinchImageView) mContentView.findViewById(R.id.imageView);
        mVideoIcon = mContentView.findViewById(R.id.video_flag);

    }

    public void setMediaSource(String source) {
        mMediaSource = source;
        initSelfByMimeType();
    }


    private void initSelfByMimeType() {
        mIsVideoMimeType = MimeTypeUtil.isVideoType(mMediaSource);
        if (mIsVideoMimeType) {
            if (mVideoView == null) {
                mVideoView = (VideoView) mContentView.findViewById(R.id.videoView);

                android.widget.MediaController mediaController = new android.widget.MediaController(getContext());
                mVideoView.setMediaController(mediaController);
            }
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mVideoIcon.setVisibility(View.VISIBLE);
            mImageLoader.load(mMediaSource).into(mImageView);
            final String videoPath = mMediaSource;
            mVideoIcon.setOnClickListener(v -> playVideo(videoPath));
            if (getTag().equals(0)) {
                playVideo(videoPath);
            }
        } else {
            mVideoIcon.setVisibility(View.GONE);
            if (mVideoView != null && mVideoView.getVisibility() == View.VISIBLE) {
                mVideoView.setVisibility(View.GONE);
            }
            if (mImageView != null) {
                mImageView.reset();
                try {
                    mImageLoader.load(mMediaSource).diskCacheStrategy(DiskCacheStrategy.DATA).into(mImageView);
                } catch (OutOfMemoryError error) {
                    System.gc();
                    System.gc();
                    System.gc();
                }
            }
        }
    }

    private void playVideo(String videoPath) {
        mVideoView.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);
        mVideoIcon.setVisibility(View.GONE);


        mVideoView.setVideoPath(videoPath);
        mVideoView.start();

        mVideoView.requestFocus();

        mVideoView.setOnCompletionListener(mp -> {
            stop();
            if (mCacheVideoAdBean != null) {
            }
        });
        mCacheVideoAdBean = null;

    }


    private GalleryPagerActivity.PagerBean mCacheVideoAdBean;




    public void play() {
        if (mIsVideoMimeType) {
            playVideo(mMediaSource);
        }
    }

    public void stop() {
        if (mIsVideoMimeType) {
            if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }
            mVideoIcon.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    public void resume() {
        if (mVideoView != null && mVideoView.getVisibility() == View.VISIBLE) {
            mVideoView.resume();
        }
    }

    public String getMediaSource() {
        return mMediaSource;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
