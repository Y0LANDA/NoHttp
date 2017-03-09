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

import android.os.Build;

import com.yanzhenjie.nohttp.tools.HeaderUtil;
import com.yanzhenjie.nohttp.tools.AndroidVersion;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Yan Zhenjie on 2016/10/15.
 */
public class URLConnectionNetworkExecutor implements NetworkExecutor {

    @Override
    public Network execute(IBasicRequest request) throws Exception {
        URL url = new URL(request.url());
        HttpURLConnection connection;
        Proxy proxy = request.getProxy();
        if (proxy == null)
            connection = (HttpURLConnection) url.openConnection();
        else
            connection = (HttpURLConnection) url.openConnection(proxy);

        connection.setConnectTimeout(request.getConnectTimeout());
        connection.setReadTimeout(request.getReadTimeout());
        connection.setInstanceFollowRedirects(false);

        if (connection instanceof HttpsURLConnection) {
            SSLSocketFactory sslSocketFactory = request.getSSLSocketFactory();
            if (sslSocketFactory != null)
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
            HostnameVerifier hostnameVerifier = request.getHostnameVerifier();
            if (hostnameVerifier != null)
                ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
        }

        // Base attribute
        connection.setRequestMethod(request.getRequestMethod().toString());

        connection.setDoInput(true);
        boolean isAllowBody = isAllowBody(request.getRequestMethod());
        connection.setDoOutput(isAllowBody);

        // Adds all request header to connection.
        Headers headers = request.headers();

        // To fix bug: accidental EOFException before API 19.
        List<String> values = headers.getValues(Headers.HEAD_KEY_CONNECTION);
        if (values == null || values.size() == 0)
            headers.set(Headers.HEAD_KEY_CONNECTION, Build.VERSION.SDK_INT > AndroidVersion.KITKAT ? Headers
                    .HEAD_VALUE_CONNECTION_KEEP_ALIVE : Headers.HEAD_VALUE_CONNECTION_CLOSE);

        if (isAllowBody) {
            long contentLength = request.getContentLength();
            if (contentLength < Integer.MAX_VALUE)
                connection.setFixedLengthStreamingMode((int) contentLength);
            else if (Build.VERSION.SDK_INT >= AndroidVersion.KITKAT)
                try {
                    Class<?> connectionClass = connection.getClass();
                    Method setFixedLengthStreamingModeMethod = connectionClass.getMethod
                            ("setFixedLengthStreamingMode", long.class);
                    setFixedLengthStreamingModeMethod.invoke(connection, contentLength);
                } catch (Throwable e) {
                    connection.setChunkedStreamingMode(256 * 1024);
                }
            else
                connection.setChunkedStreamingMode(256 * 1024);
            headers.set(Headers.HEAD_KEY_CONTENT_LENGTH, Long.toString(contentLength));
        }

        Map<String, String> requestHeaders = headers.toRequestHeaders();
        for (Map.Entry<String, String> headerEntry : requestHeaders.entrySet()) {
            String headKey = headerEntry.getKey();
            String headValue = headerEntry.getValue();
            Logger.i(headKey + ": " + headValue);
            connection.setRequestProperty(headKey, headValue);
        }
        // 5. Connect
        connection.connect();
        return new URLConnectionNetwork(connection);
    }

    private boolean isAllowBody(RequestMethod requestMethod) {
        boolean allowRequestBody = requestMethod.allowRequestBody();
        // Fix Android bug.
        if (Build.VERSION.SDK_INT < AndroidVersion.LOLLIPOP)
            return allowRequestBody && requestMethod != RequestMethod.DELETE;
        return allowRequestBody;
    }

    /**
     * Get input stream from connection.
     *
     * @param responseCode    response code of connection.
     * @param contentEncoding {@value Headers#HEAD_KEY_CONTENT_ENCODING} value of the HTTP response headers.
     * @param urlConnection   connection.
     * @return when the normal return the correct input stream, returns the error when the response code is more
     * than 400 input stream.
     * @throws IOException if no InputStream could be created.
     */
    public static InputStream getServerStream(int responseCode, String contentEncoding, HttpURLConnection
            urlConnection) throws IOException {
        if (responseCode >= 400)
            return getErrorStream(contentEncoding, urlConnection);
        else {
            return getInputStream(contentEncoding, urlConnection);
        }
    }

    /**
     * Get the input stream, and automatically extract.
     *
     * @param contentEncoding {@value Headers#HEAD_KEY_CONTENT_ENCODING} value of the HTTP response headers.
     * @param urlConnection   {@link HttpURLConnection}.
     * @return http input stream.
     * @throws IOException Unpack the stream may be thrown, or if no input stream could be created.
     */
    private static InputStream getInputStream(String contentEncoding, HttpURLConnection urlConnection) throws
            IOException {
        InputStream inputStream = urlConnection.getInputStream();
        return gzipInputStream(contentEncoding, inputStream);
    }

    /**
     * Get the wrong input stream, and automatically extract.
     *
     * @param contentEncoding {@value Headers#HEAD_KEY_CONTENT_ENCODING} value of the HTTP response headers.
     * @param urlConnection   {@link HttpURLConnection}.
     * @return http error stream.
     * @throws IOException Unpack the stream may be thrown.
     */
    private static InputStream getErrorStream(String contentEncoding, HttpURLConnection urlConnection) throws
            IOException {
        InputStream inputStream = urlConnection.getErrorStream();
        return gzipInputStream(contentEncoding, inputStream);
    }

    /**
     * Pressure http input stream.
     *
     * @param contentEncoding {@value Headers#HEAD_KEY_CONTENT_ENCODING} value of the HTTP response headers.
     * @param inputStream     {@link InputStream}.
     * @return It can directly read normal data flow
     * @throws IOException if an {@code IOException} occurs.
     */
    private static InputStream gzipInputStream(String contentEncoding, InputStream inputStream) throws
            IOException {
        if (HeaderUtil.isGzipContent(contentEncoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

}
