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
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Yan Zhenjie on 2016/10/15.
 */
public class OkHttpNetworkExecutor implements NetworkExecutor {

    @Override
    public Network execute(IBasicRequest request) throws Exception {
        URL url = new URL(request.url());
        HttpURLConnection connection = URLConnectionFactory.getInstance().open(url, request.getProxy());
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
        boolean isAllowBody = request.getRequestMethod().allowRequestBody();
        connection.setDoOutput(isAllowBody);

        // Adds all request header to connection.
        Headers headers = request.headers();

        // To fix bug: accidental EOFException before API 19.
        List<String> values = headers.getValues(Headers.HEAD_KEY_CONNECTION);
        if (values == null || values.size() == 0)
            headers.add(Headers.HEAD_KEY_CONNECTION, Headers.HEAD_VALUE_CONNECTION_KEEP_ALIVE);

        if (isAllowBody)
            headers.set(Headers.HEAD_KEY_CONTENT_LENGTH, Long.toString(request.getContentLength()));

        Map<String, String> requestHeaders = headers.toRequestHeaders();
        for (Map.Entry<String, String> headerEntry : requestHeaders.entrySet()) {
            String headKey = headerEntry.getKey();
            String headValue = headerEntry.getValue();
            Logger.i(headKey + ": " + headValue);
            connection.setRequestProperty(headKey, headValue);
        }
        // 5. Connect
        connection.connect();
        return new OkHttpNetwork(connection);
    }
}
