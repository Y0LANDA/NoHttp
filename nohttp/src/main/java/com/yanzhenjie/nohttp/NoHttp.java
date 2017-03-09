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
package com.yanzhenjie.nohttp;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.yanzhenjie.nohttp.cache.CacheEntity;
import com.yanzhenjie.nohttp.cache.DBCacheStore;
import com.yanzhenjie.nohttp.cookie.DBCookieStore;
import com.yanzhenjie.nohttp.download.DownloadRequest;
import com.yanzhenjie.nohttp.rest.ByteArrayRequest;
import com.yanzhenjie.nohttp.rest.IProtocolRequest;
import com.yanzhenjie.nohttp.rest.ImageRequest;
import com.yanzhenjie.nohttp.rest.JsonArrayRequest;
import com.yanzhenjie.nohttp.rest.JsonObjectRequest;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.nohttp.rest.SyncRequestExecutor;
import com.yanzhenjie.nohttp.tools.CacheStore;
import com.yanzhenjie.nohttp.download.DefaultDownloadRequest;
import com.yanzhenjie.nohttp.download.DownloadQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

/**
 * <p>
 * NoHttp.
 * </p>
 * Created in Jul 28, 2015 7:32:22 PM.
 *
 * @author Yan Zhenjie.
 */
public class NoHttp {

    /**
     * Context.
     */
    private static Context sContext;
    private static NoHttp instance;

    private int mConnectTimeout;
    private int mReadTimeout;

    private CookieManager mCookieManager;
    private NetworkExecutor mNetworkExecutor;
    private CacheStore<CacheEntity> mCacheStore;

    private NoHttp(Config config) {
        mConnectTimeout = config.mConnectTimeout;
        mReadTimeout = config.mReadTimeout;

        CookieStore cookieStore = config.mCookieStore == null ? new DBCookieStore(NoHttp.getContext()) : config
                .mCookieStore;
        mCookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        mCacheStore = config.mCacheStore == null ? new DBCacheStore(NoHttp.getContext()) : config.mCacheStore;
        mNetworkExecutor = config.mNetworkExecutor == null ? new URLConnectionNetworkExecutor() : config
                .mNetworkExecutor;
    }

    /**
     * Initialize NoHttp, should invoke on {@link android.app.Application#onCreate()}.
     *
     * @param context {@link Context}.
     */
    public static void initialize(Context context) {
        initialize(context, null);
    }

    /**
     * Initialize NoHttp, should invoke on {@link android.app.Application#onCreate()}.
     *
     * @param context {@link Context}.
     * @param config  {@link }.
     */
    public static void initialize(Context context, Config config) {
        if (sContext == null) {
            sContext = context.getApplicationContext();
            instance = new NoHttp(config == null ? new Config() : config);
        }
    }

    /**
     * Gets context of app.
     *
     * @return {@link Context}.
     */
    public static Context getContext() {
        testInitialize();
        return sContext;
    }

    /**
     * Gets instance for config.
     *
     * @return {@link NoHttp}.
     */
    private static NoHttp getInstance() {
        testInitialize();
        return instance;
    }

    /**
     * Test initialized.
     */
    private static void testInitialize() {
        if (sContext == null)
            throw new ExceptionInInitializerError("Please invoke NoHttp.initialize(Application) on " +
                    "Application#onCreate()");
    }


    /**
     * Gets connect timeout.
     *
     * @return ms.
     */
    public static int getConnectTimeout() {
        return getInstance().mConnectTimeout;
    }

    /**
     * Gets read timeout.
     *
     * @return ms.
     */
    public static int getReadTimeout() {
        return getInstance().mReadTimeout;
    }

    /**
     * Gets cookie manager.
     *
     * @return {@link CookieHandler}.
     */
    public static CookieManager getCookieManager() {
        return getInstance().mCookieManager;
    }

    /**
     * Gets cache store.
     *
     * @return {@link CacheStore}.
     */
    public static CacheStore<CacheEntity> getCacheStore() {
        return getInstance().mCacheStore;
    }

    /**
     * Gets executor implement of http.
     *
     * @return {@link NetworkExecutor}.
     */
    public static NetworkExecutor getNetworkExecutor() {
        return getInstance().mNetworkExecutor;
    }

    /**
     * Create a queue of request, the default thread pool size is 3.
     *
     * @return returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue(int)
     */
    public static RequestQueue newRequestQueue() {
        return newRequestQueue(3);
    }

    /**
     * Create a queue of request.
     *
     * @param threadPoolSize request the number of concurrent.
     * @return returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue()
     */
    public static RequestQueue newRequestQueue(int threadPoolSize) {
        RequestQueue requestQueue = new RequestQueue(threadPoolSize);
        requestQueue.start();
        return requestQueue;
    }

    /**
     * Create a String type request, the request method is {@link RequestMethod#GET}.
     *
     * @param url such as: {@code http://www.google.com}.
     * @return {@code Request<String>}.
     * @see #createStringRequest(String, RequestMethod)
     */
    public static Request<String> createStringRequest(String url) {
        return new StringRequest(url);
    }

    /**
     * Create a String type request, custom request method, method from {@link RequestMethod}.
     *
     * @param url           such as: {@code http://www.google.com}.
     * @param requestMethod {@link RequestMethod}.
     * @return {@code Request<String>}.
     * @see #createStringRequest(String)
     */
    public static Request<String> createStringRequest(String url, RequestMethod requestMethod) {
        return new StringRequest(url, requestMethod);
    }

    /**
     * Create a JSONObject type request, the request method is {@link RequestMethod#GET}.
     *
     * @param url such as: {@code http://www.google.com}.
     * @return {@code Request<JSONObject>}.
     * @see #createJsonObjectRequest(String, RequestMethod)
     */
    public static Request<JSONObject> createJsonObjectRequest(String url) {
        return new JsonObjectRequest(url);
    }

    /**
     * Create a JSONObject type request, custom request method, method from {@link RequestMethod}.
     *
     * @param url           such as: {@code http://www.google.com}.
     * @param requestMethod {@link RequestMethod}.
     * @return {@code Request<JSONObject>}.
     * @see #createJsonObjectRequest(String)
     */
    public static Request<JSONObject> createJsonObjectRequest(String url, RequestMethod requestMethod) {
        return new JsonObjectRequest(url, requestMethod);
    }

    /**
     * Create a JSONArray type request, the request method is {@link RequestMethod#GET}.
     *
     * @param url such as: {@code http://www.google.com}.
     * @return {@code Request<JSONArray>}.
     * @see #createJsonArrayRequest(String, RequestMethod)
     */
    public static Request<JSONArray> createJsonArrayRequest(String url) {
        return new JsonArrayRequest(url);
    }

    /**
     * Create a JSONArray type request, custom request method, method from {@link RequestMethod}.
     *
     * @param url           such as: {@code http://www.google.com}.
     * @param requestMethod {@link RequestMethod}.
     * @return {@code Request<JSONArray>}.
     * @see #createJsonArrayRequest(String)
     */
    public static Request<JSONArray> createJsonArrayRequest(String url, RequestMethod requestMethod) {
        return new JsonArrayRequest(url, requestMethod);
    }

    /**
     * Create a Image type request, the request method is {@link RequestMethod#GET}.
     *
     * @param url such as: {@code http://www.google.com}.
     * @return {@code Request<Bitmap>}.
     * @see #createImageRequest(String, RequestMethod)
     * @see #createImageRequest(String, RequestMethod, int, int, Bitmap.Config, ImageView.ScaleType)
     */
    public static Request<Bitmap> createImageRequest(String url) {
        return createImageRequest(url, RequestMethod.GET);
    }

    /**
     * Create a Image type request.
     *
     * @param url           such as: {@code http://www.google.com}.
     * @param requestMethod {@link RequestMethod}.
     * @return {@code Request<Bitmap>}.
     * @see #createImageRequest(String)
     * @see #createImageRequest(String, RequestMethod, int, int, Bitmap.Config, ImageView.ScaleType)
     */
    public static Request<Bitmap> createImageRequest(String url, RequestMethod requestMethod) {
        return createImageRequest(url, requestMethod, 1000, 1000, Bitmap.Config.ARGB_8888, ImageView.ScaleType
                .CENTER_INSIDE);
    }

    /**
     * Create a Image type request.
     *
     * @param url           such as: {@code http://www.google.com}.
     * @param requestMethod {@link RequestMethod}.
     * @param maxWidth      width.
     * @param maxHeight     height.
     * @param config        config.
     * @param scaleType     scaleType.
     * @return {@code Request<Bitmap>}.
     * @see #createImageRequest(String)
     * @see #createImageRequest(String, RequestMethod)
     */
    public static Request<Bitmap> createImageRequest(String url, RequestMethod requestMethod, int maxWidth, int
            maxHeight, Bitmap.Config config, ImageView.ScaleType scaleType) {
        return new ImageRequest(url, requestMethod, maxWidth, maxHeight, config, scaleType);
    }

    /**
     * Create a byte array request, the request method is {@link RequestMethod#GET}.
     *
     * @param url url.
     * @return {@code Request<byte[]>}.
     * @see #createByteArrayRequest(String, RequestMethod)
     */
    public static Request<byte[]> createByteArrayRequest(String url) {
        return new ByteArrayRequest(url);
    }

    /**
     * Create a byte array request.
     *
     * @param url    url.
     * @param method {@link RequestMethod}.
     * @return {@code Request<byte[]>}.
     * @see #createByteArrayRequest(String)
     */
    public static Request<byte[]> createByteArrayRequest(String url, RequestMethod method) {
        return new ByteArrayRequest(url, method);
    }

    /**
     * Initiate a synchronization request.
     *
     * @param request request object.
     * @param <T>     {@link T}.
     * @return {@link Response}.
     */
    public static <T> Response<T> startRequestSync(IProtocolRequest<T> request) {
        return SyncRequestExecutor.INSTANCE.execute(request);
    }

    /**
     * Create a new download queue, the default thread pool size is 3.
     *
     * @return {@link DownloadQueue}.
     * @see #newDownloadQueue(int)
     */
    public static DownloadQueue newDownloadQueue() {
        return newDownloadQueue(3);
    }

    /**
     * Create a new download queue.
     *
     * @param threadPoolSize thread pool number, here is the number of concurrent tasks.
     * @return {@link DownloadQueue}.
     * @see #newDownloadQueue()
     */
    public static DownloadQueue newDownloadQueue(int threadPoolSize) {
        DownloadQueue downloadQueue = new DownloadQueue(threadPoolSize);
        downloadQueue.start();
        return downloadQueue;
    }

    /**
     * Create a download object, auto named file. The request method is {@link RequestMethod#GET}.
     *
     * @param url         download address.
     * @param fileFolder  folder to save file.
     * @param isDeleteOld find the same when the file is deleted after download, or on behalf of the download is
     *                    complete, not to request the network.
     * @return {@link DownloadRequest}.
     * @see #createDownloadRequest(String, RequestMethod, String, String, boolean, boolean)
     */
    public static DownloadRequest createDownloadRequest(String url, String fileFolder, boolean isDeleteOld) {
        return createDownloadRequest(url, RequestMethod.GET, fileFolder, isDeleteOld);
    }

    /**
     * Create a download object, auto named file.
     *
     * @param url           download address.
     * @param requestMethod {@link RequestMethod}.
     * @param fileFolder    folder to save file.
     * @param isDeleteOld   find the same when the file is deleted after download, or on behalf of the download is
     *                      complete, not to request the network.
     * @return {@link DownloadRequest}.
     * @see #createDownloadRequest(String, RequestMethod, String, String, boolean, boolean)
     */
    public static DownloadRequest createDownloadRequest(String url, RequestMethod requestMethod, String fileFolder,
                                                        boolean isDeleteOld) {
        return new DefaultDownloadRequest(url, requestMethod, fileFolder, isDeleteOld);
    }

    /**
     * Create a download object. The request method is {@link RequestMethod#GET}.
     *
     * @param url         download address.
     * @param fileFolder  folder to save file.
     * @param filename    filename.
     * @param isRange     whether the breakpoint continuing.
     * @param isDeleteOld find the same when the file is deleted after download, or on behalf of the download is
     *                    complete, not to request the network.
     * @return {@link DownloadRequest}.
     * @see #createDownloadRequest(String, RequestMethod, String, String, boolean, boolean)
     */
    public static DownloadRequest createDownloadRequest(String url, String fileFolder, String filename, boolean
            isRange, boolean isDeleteOld) {
        return createDownloadRequest(url, RequestMethod.GET, fileFolder, filename, isRange, isDeleteOld);
    }

    /**
     * Create a download object.
     *
     * @param url           download address.
     * @param requestMethod {@link RequestMethod}.
     * @param fileFolder    folder to save file.
     * @param filename      filename.
     * @param isRange       whether the breakpoint continuing.
     * @param isDeleteOld   find the same when the file is deleted after download, or on behalf of the download is
     *                      complete, not to request the network.
     * @return {@link DownloadRequest}.
     * @see #createDownloadRequest(String, String, String, boolean, boolean)
     */
    public static DownloadRequest createDownloadRequest(String url, RequestMethod requestMethod, String fileFolder,
                                                        String filename, boolean isRange, boolean isDeleteOld) {
        return new DefaultDownloadRequest(url, requestMethod, fileFolder, filename, isRange, isDeleteOld);
    }

    /**
     * Default thread pool size for request queue.
     */
    private static RequestQueue sRequestQueueInstance;

    /**
     * Default thread pool size for request queue.
     */
    private static DownloadQueue sDownloadQueueInstance;

    /**
     * Get default RequestQueue.
     *
     * @return {@link RequestQueue}.
     */
    public static RequestQueue getRequestQueueInstance() {
        if (sRequestQueueInstance == null)
            synchronized (NoHttp.class) {
                if (sRequestQueueInstance == null) {
                    sRequestQueueInstance = newRequestQueue();
                }
            }
        return sRequestQueueInstance;
    }

    /**
     * Get default DownloadQueue.
     *
     * @return {@link DownloadQueue}.
     */
    public static DownloadQueue getDownloadQueueInstance() {
        if (sDownloadQueueInstance == null)
            synchronized (NoHttp.class) {
                if (sDownloadQueueInstance == null) {
                    sDownloadQueueInstance = newDownloadQueue();
                }
            }
        return sDownloadQueueInstance;
    }

    public static final class Config {

        private int mConnectTimeout = 10 * 1000;
        private int mReadTimeout = 10 * 1000;

        private CookieStore mCookieStore;
        private CacheStore<CacheEntity> mCacheStore;

        private NetworkExecutor mNetworkExecutor;

        public Config() {
        }

        /**
         * Set default connect timeout.
         *
         * @param timeout ms.
         * @return {@link Config}.
         */
        public Config setConnectTimeout(int timeout) {
            mConnectTimeout = timeout;
            return this;
        }

        /**
         * Set default read timeout.
         *
         * @param timeout ms.
         * @return {@link Config}.
         */
        public Config setReadTimeout(int timeout) {
            mReadTimeout = timeout;
            return this;
        }

        /**
         * Sets cookie manager.
         *
         * @param cookieStore {@link CookieStore}.
         * @return {@link Config}.
         */
        public Config setCookieStore(CookieStore cookieStore) {
            this.mCookieStore = cookieStore;
            return this;
        }

        /**
         * Sets cache store.
         *
         * @param cacheStore {@link CacheStore}.
         * @return {@link Config}.
         */
        public Config setCacheStore(CacheStore<CacheEntity> cacheStore) {
            this.mCacheStore = cacheStore;
            return this;
        }

        /**
         * Set the Http request interface, realizes the Http request.
         *
         * @param executor {@link NetworkExecutor}.
         * @return {@link Config}.
         */
        public Config setNetworkExecutor(NetworkExecutor executor) {
            this.mNetworkExecutor = executor;
            return this;
        }
    }

}
