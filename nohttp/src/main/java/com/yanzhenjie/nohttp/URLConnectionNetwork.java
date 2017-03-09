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

import com.yanzhenjie.nohttp.tools.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by Yan Zhenjie on 2017/2/12.
 */
public class URLConnectionNetwork implements Network {

    private HttpURLConnection mUrlConnection;

    public URLConnectionNetwork(HttpURLConnection urlConnection) {
        this.mUrlConnection = urlConnection;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return mUrlConnection.getOutputStream();
    }

    @Override
    public int getResponseCode() throws IOException {
        return mUrlConnection.getResponseCode();
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return mUrlConnection.getHeaderFields();
    }

    @Override
    public InputStream getServerStream(int responseCode, Headers headers) throws IOException {
        return URLConnectionNetworkExecutor.getServerStream(responseCode, headers.getContentEncoding(),
                mUrlConnection);
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(mUrlConnection);
    }
}