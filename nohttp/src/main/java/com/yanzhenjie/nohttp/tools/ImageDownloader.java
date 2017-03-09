/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.nohttp.tools;

import android.text.TextUtils;

import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.HandlerDelivery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created in Nov 4, 2015 3:10:58 PM.
 *
 * @author Yan Zhenjie.
 */
public class ImageDownloader {

    private static ImageDownloader instance;
    private ExecutorService mExecutorService;

    /**
     * CacheStore path.
     */
    private String mCachePath;

    private ImageDownloader() {
        setCachePath(NoHttp.getContext().getCacheDir().getAbsolutePath());
        mExecutorService = Executors.newFixedThreadPool(3);
    }

    /**
     * Singleton mode to create download object.
     *
     * @return {@link ImageDownloader}.
     */
    public static ImageDownloader getInstance() {
        if (instance == null) {
            instance = new ImageDownloader();
        }
        return instance;
    }

    /**
     * Set cache path.
     *
     * @param cachePath path.
     */
    public void setCachePath(String cachePath) {
        if (TextUtils.isEmpty(cachePath))
            throw new NullPointerException("cachePath can't null");
        this.mCachePath = cachePath;
        File file = new File(cachePath);
        if (file.exists() && file.isFile())
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        if (!file.exists())
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
    }

    /**
     * download image.
     *
     * @param imageUrl     url.
     * @param downListener listener.
     * @param deleteOld    whether to delete the old files.
     * @param tag          tag.
     */
    public void downloadImage(String imageUrl, OnImageDownListener downListener, boolean deleteOld, Object tag) {
        downloadImage(imageUrl, downListener, deleteOld, tag, 3 * 1000);
    }

    /**
     * download image.
     *
     * @param imageUrl     url.
     * @param downListener listener.
     * @param deleteOld    whether to delete the old files.
     * @param tag          tag.
     * @param timeOut      times.
     */
    public void downloadImage(String imageUrl, OnImageDownListener downListener, boolean deleteOld, Object tag,
                              int timeOut) {
        String buffer = mCachePath + File.separator +
                Encryption.getMD5ForString(imageUrl) +
                ".png";
        downloadImage(imageUrl, downListener, buffer, deleteOld, tag, timeOut);
    }

    /**
     * Download the image to the specified path.
     *
     * @param imageUrl     url.
     * @param downListener listener.
     * @param path         path.
     * @param deleteOld    whether to delete the old files.
     * @param tag          tag.
     */
    public void downloadImage(String imageUrl, OnImageDownListener downListener, String path, boolean deleteOld,
                              Object tag) {
        downloadImage(imageUrl, downListener, path, deleteOld, tag, 3 * 1000);
    }

    /**
     * Download the image to the specified path.
     *
     * @param imageUrl     url.
     * @param downListener listener.
     * @param path         path.
     * @param deleteOld    whether to delete the old files.
     * @param tag          tag.
     * @param timeOut      times.
     */
    public void downloadImage(String imageUrl, OnImageDownListener downListener, String path, Boolean deleteOld,
                              Object tag, int timeOut) {
        Logger.d("ImageDownload url: " + imageUrl);
        Logger.d("ImageDownload path: " + path);
        File file = new File(path);
        if (file.exists() && deleteOld)
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        if (file.exists()) {
            ImageHolder holder = new ImageHolder();
            holder.imageUrl = imageUrl;
            holder.isSucceed = true;
            holder.imagePath = path;
            holder.downListener = downListener;
            holder.tag = tag;
            HandlerDelivery.getInstance().post(holder);
        } else
            mExecutorService.execute(new DownImageThread(imageUrl, path, downListener, tag, timeOut));
    }

    private class DownImageThread implements Runnable {

        private String mImageUrl;

        private String mImagePath;

        private Object tag;

        private OnImageDownListener mDownListener;

        private int timeOut;

        public DownImageThread(String imageUrl, String imagePath, OnImageDownListener downListener, Object tag,
                               int timeOut) {
            super();
            this.mImageUrl = imageUrl;
            this.mImagePath = imagePath;
            this.mDownListener = downListener;
            this.tag = tag;
            this.timeOut = timeOut;
        }

        @Override
        public void run() {
            ImageHolder holder = new ImageHolder();
            holder.imageUrl = mImageUrl;
            holder.downListener = mDownListener;
            holder.imagePath = mImagePath;
            holder.tag = tag;

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                URL url = new URL(mImageUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(timeOut);
                urlConnection.setReadTimeout(timeOut);
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                if (HttpURLConnection.HTTP_OK == responseCode) {
                    inputStream = IOUtils.toBufferedInputStream(urlConnection.getInputStream());
                    outputStream = IOUtils.toBufferedOutputStream(new FileOutputStream(new File(mImagePath),
                            false));
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    holder.isSucceed = true;
                    IOUtils.flushQuietly(outputStream);

                    Logger.d(mImageUrl + " download finished; path: " + mImagePath + ".");
                } else {
                    Logger.d(mImageUrl + " responseCode: " + responseCode + ".");
                }
            } catch (Exception e) {
                Logger.w(e);
            } finally {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(urlConnection);
            }
            HandlerDelivery.getInstance().post(holder);
        }
    }

    private class ImageHolder implements Runnable {
        String imageUrl;
        String imagePath;
        OnImageDownListener downListener;
        boolean isSucceed;
        Object tag;

        @Override
        public void run() {
            if (downListener != null)
                downListener.onDownFinish(imageUrl, imagePath, isSucceed, tag);
        }
    }

    public interface OnImageDownListener {
        /**
         * @param imageUrl  picture address.
         * @param path      picture save address.
         * @param isSucceed success ask for download.
         * @param tag       tag that are passed to download.
         */
        void onDownFinish(String imageUrl, String path, boolean isSucceed, Object tag);
    }
}