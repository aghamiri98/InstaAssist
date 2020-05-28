package com.app.instaassist.ui.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.app.instaassist.base.Constant;
import com.bumptech.glide.Glide;
import com.app.instaassist.R;

public class ImageGalleryActivity extends Activity implements View.OnClickListener {


    private Uri mImageUri;
    private String mImagePath;

    private ImageView mImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.image_play);

        mImageView = (ImageView) findViewById(R.id.image);

        mImageUri = getIntent().getData();
        mImagePath = getIntent().getStringExtra(Constant.EXTRAS);

        Glide.with(this).load(mImageUri).into(mImageView);

        findViewById(R.id.more_vert).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.more_vert) {
        }
    }
}
