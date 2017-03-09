/*
 * Copyright © Yan Zhenjie. All Rights Reserved
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

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.internal.huc.OkHttpURLConnection;
import okhttp3.internal.huc.OkHttpsURLConnection;

/**
 * Created by Yan Zhenjie on 2016/9/4.
 */
public class URLConnectionFactory implements Cloneable {

    private static URLConnectionFactory instance;

    /**
     * Gets instance.
     *
     * @return {@link URLConnectionFactory}.
     */
    public static URLConnectionFactory getInstance() {
        if (instance == null) {
            synchronized (URLConnectionFactory.class) {
                if (instance == null) {
                    instance = new URLConnectionFactory(new OkHttpClient());
                }
            }
        }
        return instance;
    }

    private OkHttpClient mClient;

    public URLConnectionFactory(OkHttpClient client) {
        this.mClient = client;
    }

    /**
     * Gets OkHttpClient.
     *
     * @return {@link OkHttpClient}.
     */
    public OkHttpClient client() {
        return mClient;
    }

    @Override
    public URLConnectionFactory clone() {
        return new URLConnectionFactory(mClient);
    }

    /**
     * Open url.
     *
     * @param url {@link URL}.
     * @return {@link HttpURLConnection}.
     */
    public HttpURLConnection open(URL url) {
        return open(url, null);
    }

    /**
     * Open url.
     *
     * @param url   {@link URL}.
     * @param proxy {@link Proxy}.
     * @return {@link HttpURLConnection}.
     */
    public HttpURLConnection open(URL url, Proxy proxy) {
        OkHttpClient copy = mClient.newBuilder().proxy(proxy).build();

        String protocol = url.getProtocol();
        if (protocol.equals("http")) return new OkHttpURLConnection(url, copy);
        if (protocol.equals("https")) return new OkHttpsURLConnection(url, copy);
        throw new IllegalArgumentException("Unexpected protocol: " + protocol);
    }
}
