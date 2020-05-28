package com.app.instaassist.ui.adapter;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.app.instaassist.ui.activity.GalleryPagerActivity;
import com.app.instaassist.ui.views.MobMediaView;

import java.util.LinkedList;
import java.util.List;

public class ImageGalleryPagerAdapter extends PagerAdapter {

    private List<GalleryPagerActivity.PagerBean> mDataList;
    private LinkedList<MobMediaView> mPageViewList;
    private RequestManager mImageLoader;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public ImageGalleryPagerAdapter(Context context, List<GalleryPagerActivity.PagerBean> dataList) {
        mDataList = dataList;
        mImageLoader = Glide.with(context);
        mPageViewList = new LinkedList<>();
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;

    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        MobMediaView view = (MobMediaView) object;
        container.removeView(view);
        mPageViewList.addLast(view);
    }

    public void deleteItem(GalleryPagerActivity.PagerBean pagerBean,MobMediaView mobMediaView) {
        mDataList.remove(pagerBean);
        pagerBean.file.delete();
        mPageViewList.remove(mobMediaView);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GalleryPagerActivity.PagerBean bean = mDataList.get(position);
        MobMediaView convertView = null;
        if (mPageViewList.size() > 0) {
            convertView = mPageViewList.removeFirst();
        } else {
            convertView = new MobMediaView(mContext);
        }

        convertView.setTag(position);
        if (bean.file != null) {
            convertView.setMediaSource(bean.file.getAbsolutePath());
        } else {
         
          
        }
        container.addView(convertView);
        return convertView;
    }
}
