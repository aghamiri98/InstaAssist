package com.app.instaassist.ui.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import com.app.instaassist.R;
import com.app.instaassist.base.Constant;

public class VideoPlayerActivity extends Activity  implements View.OnClickListener{




    private VideoView mVideoView;


    private Uri mVideoURI;
    private String mVideoPath;

    private boolean mErrorHappened = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_videoplayer);

        mVideoView = (VideoView) findViewById(R.id.videoView);

        mVideoURI  = getIntent().getData();
        mVideoPath = getIntent().getStringExtra(Constant.EXTRAS);
        android.widget.MediaController mediaController = new android.widget.MediaController(this);

        mVideoView.setMediaController(mediaController);
        mVideoView.setVideoURI(mVideoURI);
        mVideoView.start();
        mVideoView.requestFocus();

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mErrorHappened = true;
                return false;
            }
        });


        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(mErrorHappened) {
                    return;
                }
                mVideoView.setVideoURI(mVideoURI);
                mVideoView.start();
            }
        });


        findViewById(R.id.more_vert).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        if(v.getId() == R.id.more_vert) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }


    @Override
    protected void onDestroy() {
        mVideoView.destroyDrawingCache();
        super.onDestroy();
    }
}
