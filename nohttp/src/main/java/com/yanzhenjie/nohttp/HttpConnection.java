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

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.TimeoutError;
import com.yanzhenjie.nohttp.error.URLError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.tools.IOUtils;
import com.yanzhenjie.nohttp.tools.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * Created by Yan Zhenjie on 2016/9/4.
 */
public class HttpConnection {

    private NetworkExecutor mExecutor;

    public HttpConnection(NetworkExecutor executor) {
        this.mExecutor = executor;
    }

    /**
     * Send the request, send only head, parameters, such as file information.
     *
     * @param request {@link IBasicRequest}.
     * @return {@link Connection}.
     */
    public Connection getConnection(IBasicRequest request) {
        Logger.d("--------------Request start--------------");

        Headers responseHeaders = new HttpHeaders();
        InputStream inputStream = null;
        Exception exception = null;

        Network network = null;
        String url = request.url();
        try {
            if (!NetUtil.isNetworkAvailable())
                throw new NetworkError("The network is not available, please check the network. The requested url is:" +
                        " " + url);

            // MalformedURLException, IOException, ProtocolException, UnknownHostException, SocketTimeoutException
            network = createConnectionAndWriteData(request);
            Logger.d("-------Response start-------");
            int responseCode = network.getResponseCode();
            responseHeaders = parseResponseHeaders(new URI(request.url()), responseCode, network.getResponseHeaders());

            // handle body
            if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) {
                Connection redirectConnection = handleRedirect(request, responseHeaders);
                responseHeaders = redirectConnection.responseHeaders();
                inputStream = redirectConnection.serverStream();
                exception = redirectConnection.exception();
            } else if (hasResponseBody(request.getRequestMethod(), responseCode)) {
                inputStream = network.getServerStream(responseCode, responseHeaders);
            }
            Logger.d("-------Response end-------");
        } catch (MalformedURLException e) {
            exception = new URLError("The url is malformed: " + url + ".");
        } catch (UnknownHostException e) {
            exception = new UnKnownHostError("Hostname can not be resolved: " + url + ".");
        } catch (SocketTimeoutException e) {
            exception = new TimeoutError("Request time out: " + url + ".");
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception != null)
                Logger.e(exception);
        }
        Logger.d("--------------Request finish--------------");
        return new Connection(network, responseHeaders, inputStream, exception);
    }

    /**
     * Handle retries, and complete the request network here.
     *
     * @param request {@link IBasicRequest}.
     * @return {@link Network}.
     * @throws Exception {@link #createNetwork(IBasicRequest)}.
     */
    private Network createConnectionAndWriteData(IBasicRequest request) throws Exception {
        Network network = null;
        Exception exception = null;
        int retryCount = request.getRetryCount() + 1;
        boolean failed = true;
        for (; failed && retryCount > 0; retryCount--) {
            try {
                network = createNetwork(request);
                exception = null;
                failed = false;
            } catch (Exception e) {
                exception = e;
            }
        }
        if (failed) {
            throw exception;
        } else if (request.getRequestMethod().allowRequestBody()) {
            writeRequestBody(request, network.getOutputStream());
        }
        return network;
    }

    /**
     * The connection is established, including the head and send the request body.
     *
     * @param request {@link IBasicRequest}.
     * @return {@link HttpURLConnection} Have been established and the server connection, and send the complete data,
     * you can directly determine the response code and read the data.
     * @throws Exception can happen when the connection is established and send data.
     */
    private Network createNetwork(IBasicRequest request) throws Exception {
        // Pre operation notice.
        request.onPreExecute();

        // Print url, method.
        String url = request.url();
        Logger.i("Request address: " + url);
        Logger.i("Request method: " + request.getRequestMethod());

        Headers headers = request.headers();
        headers.set(Headers.HEAD_KEY_CONTENT_TYPE, request.getContentType());

        // Connection.
        List<String> values = headers.getValues(Headers.HEAD_KEY_CONNECTION);
        if (values == null || values.size() == 0)
            headers.add(Headers.HEAD_KEY_CONNECTION, Headers.HEAD_VALUE_CONNECTION_KEEP_ALIVE);

        // Content-Length.
        RequestMethod requestMethod = request.getRequestMethod();
        if (requestMethod.allowRequestBody())
            headers.set(Headers.HEAD_KEY_CONTENT_LENGTH, Long.toString(request.getContentLength()));

        // Cookie.
        headers.addCookie(new URI(url), NoHttp.getCookieManager());
        return mExecutor.execute(request);
    }

    /**
     * Write request params.
     *
     * @param request      {@link IBasicRequest}.
     * @param outputStream {@link OutputStream}.
     * @throws IOException io exception.
     */
    private void writeRequestBody(IBasicRequest request, OutputStream outputStream) throws IOException {
        // 6. Write request body
        Logger.i("-------Send request data start-------");
        OutputStream realOutputStream = IOUtils.toBufferedOutputStream(outputStream);
        request.onWriteRequestBody(realOutputStream);
        IOUtils.closeQuietly(realOutputStream);
        Logger.i("-------Send request data end-------");
    }

    /**
     * The redirection process any response.
     *
     * @param oldRequest      need to redirect the {@link Request}.
     * @param responseHeaders need to redirect the request of the responding head.
     * @return {@link Connection}.
     */
    private Connection handleRedirect(IBasicRequest oldRequest, Headers responseHeaders) {
        // redirect request
        IBasicRequest redirectRequest = null;
        RedirectHandler redirectHandler = oldRequest.getRedirectHandler();
        if (redirectHandler != null) {
            if (redirectHandler.isDisallowedRedirect(responseHeaders))
                return new Connection(null, responseHeaders, null, null);
            else {
                redirectRequest = redirectHandler.onRedirect(responseHeaders);
            }
        }
        if (redirectRequest == null) {
            String location = responseHeaders.getLocation();

            if (!URLUtil.isNetworkUrl(location)) {
                String oldUrl = oldRequest.url();
                try {
                    URL url = new URL(oldUrl);
                    location = url.getProtocol() + "://" + url.getHost() + location;
                } catch (MalformedURLException e) {
                    // nothing.
                }
            }

            redirectRequest = new BasicRequest(location, oldRequest.getRequestMethod()) {
            };
            redirectRequest.setRedirectHandler(oldRequest.getRedirectHandler());
            redirectRequest.setSSLSocketFactory(oldRequest.getSSLSocketFactory());
            redirectRequest.setHostnameVerifier(oldRequest.getHostnameVerifier());
            redirectRequest.setParamsEncoding(oldRequest.getParamsEncoding());
            redirectRequest.setProxy(oldRequest.getProxy());
        }
        return getConnection(redirectRequest);
    }

    /**
     * Parse server response headers, here will save cookies.
     *
     * @param uri             according to the requested URL generated uris.
     * @param responseCode    responseCode.
     * @param responseHeaders responseHeaders of server.
     * @return response headers of server.
     */
    private Headers parseResponseHeaders(URI uri, int responseCode, Map<String, List<String>> responseHeaders) {
        // handle cookie
        try {
            NoHttp.getCookieManager().put(uri, responseHeaders);
        } catch (IOException e) {
            Logger.e(e, "Save cookie filed: " + uri.toString() + ".");
        }

        // handle headers
        Headers headers = new HttpHeaders();
        headers.set(responseHeaders);
        headers.set(Headers.HEAD_KEY_RESPONSE_CODE, Integer.toString(responseCode));
        // print
        for (String headKey : headers.keySet()) {
            List<String> headValues = headers.getValues(headKey);
            for (String headValue : headValues) {
                StringBuilder builder = new StringBuilder();
                if (!TextUtils.isEmpty(headKey))
                    builder.append(headKey).append(": ");
                if (!TextUtils.isEmpty(headValue))
                    builder.append(headValue);
                Logger.i(builder.toString());
            }
        }
        return headers;
    }

    ////////// Read response body //////////

    /**
     * This requestMethod and responseCode has responseBody ?
     *
     * @param requestMethod it's come from {@link RequestMethod}.
     * @param responseCode  responseCode from server.
     * @return true: there is data, false: no data.
     */
    public static boolean hasResponseBody(RequestMethod requestMethod, int responseCode) {
        return requestMethod != RequestMethod.HEAD && hasResponseBody(responseCode);
    }

    /**
     * According to the response code to judge whether there is data.
     *
     * @param responseCode responseCode.
     * @return true: there is data, false: no data.
     */
    public static boolean hasResponseBody(int responseCode) {
        return !(100 <= responseCode && responseCode < 200) && responseCode != 204 && responseCode != 205 && !(300 <=
                responseCode && responseCode < 400);
    }

}
