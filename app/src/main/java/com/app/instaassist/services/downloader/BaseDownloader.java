package com.app.instaassist.services.downloader;

import com.app.instaassist.data.db.DownloadContentItem;
import com.app.instaassist.base.HttpRequestSpider;

public abstract class BaseDownloader {

    protected String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

    public abstract String getVideoUrl(String content);

    public abstract DownloadContentItem startSpideThePage(String htmlUrl);
}

