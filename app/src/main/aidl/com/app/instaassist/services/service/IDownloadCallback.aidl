// IDownloadCallback.aidl
package com.app.instaassist.services.service;
// Declare any non-default types here with import statements

interface IDownloadCallback {


    void onReceiveNewTask(String pageUrl);
    void onStartDownload(String pageURL);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onPublishProgress(String pageURL,int filePositon,int progress);
    void onDownloadSuccess(String pageURL);
    void onDownloadFailed(String pageURL);
}
