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

import android.text.TextUtils;

import com.yanzhenjie.nohttp.able.Cancelable;
import com.yanzhenjie.nohttp.able.Finishable;
import com.yanzhenjie.nohttp.able.Startable;
import com.yanzhenjie.nohttp.ssl.SSLUtils;
import com.yanzhenjie.nohttp.tools.CounterOutputStream;
import com.yanzhenjie.nohttp.tools.HeaderUtils;
import com.yanzhenjie.nohttp.tools.IOUtils;
import com.yanzhenjie.nohttp.tools.MultiValueMap;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * <p>
 * Request the basics to encapsulate.
 * </p>
 * Created in Nov 4, 2015 8:28:50 AM.
 *
 * @author Yan Zhenjie.
 */
public class BasicRequest<T extends BasicRequest> implements Comparable<BasicRequest>, Startable, Cancelable, Finishable {

    private final String boundary = createBoundary();
    private final String startBoundary = "--" + boundary;
    private final String endBoundary = startBoundary + "--";

    /**
     * Request priority.
     */
    private Priority mPriority = Priority.DEFAULT;
    /**
     * The sequence.
     */
    private int sequence;
    /**
     * Target address.
     */
    private String url;
    /**
     * Request method.
     */
    private RequestMethod mRequestMethod;
    /**
     * MultipartFormEnable.
     */
    private boolean isMultipartFormEnable = false;
    /**
     * Proxy server.
     */
    private Proxy mProxy;
    /**
     * SSLSockets.
     */
    private SSLSocketFactory mSSLSocketFactory = NoHttp.getInitializeConfig().getSSLSocketFactory();
    /**
     * HostnameVerifier.
     */
    private HostnameVerifier mHostnameVerifier = NoHttp.getInitializeConfig().getHostnameVerifier();
    /**
     * Connect timeout of handle.
     */
    private int mConnectTimeout = NoHttp.getInitializeConfig().getConnectTimeout();
    /**
     * Read data timeout.
     */
    private int mReadTimeout = NoHttp.getInitializeConfig().getReadTimeout();
    /**
     * After the failure of retries.
     */
    private int mRetryCount = NoHttp.getInitializeConfig().getRetryCount();
    /**
     * The params encoding.
     */
    private String mParamEncoding;
    /**
     * Request heads.
     */
    private Headers mHeaders;
    /**
     * Param collection.
     */
    private Params mParams;
    /**
     * RequestBody.
     */
    private InputStream mRequestBody;
    /**
     * Redirect handler.
     */
    private RedirectHandler mRedirectHandler;
    /**
     * The record has started.
     */
    private boolean isStart = false;
    /**
     * The handle is completed.
     */
    private boolean isFinished = false;
    /**
     * Has been canceled.
     */
    private boolean isCanceled = false;
    /**
     * Cancel sign.
     */
    private Object mCancelSign;

    /**
     * Tag of handle.
     */
    private Object mTag;

    /**
     * Create a handle, RequestMethod is {@link RequestMethod#GET}.
     *
     * @param url handle address, like: http://www.nohttp.net.
     */
    public BasicRequest(String url) {
        this(url, RequestMethod.GET);
    }

    /**
     * Create a handle.
     *
     * @param url           handle adress, like: http://www.nohttp.net.
     * @param requestMethod handle method, like {@link RequestMethod#GET}, {@link RequestMethod#POST}.
     */
    public BasicRequest(String url, RequestMethod requestMethod) {
        this.url = url;
        mRequestMethod = requestMethod;

        mHeaders = new Headers();
        mHeaders.set(Headers.HEAD_KEY_ACCEPT, Headers.HEAD_VALUE_ACCEPT_ALL);
        mHeaders.set(Headers.HEAD_KEY_ACCEPT_ENCODING, Headers.HEAD_VALUE_ACCEPT_ENCODING_GZIP_DEFLATE);
        mHeaders.set(Headers.HEAD_KEY_ACCEPT_LANGUAGE, HeaderUtils.systemAcceptLanguage());
        mHeaders.set(Headers.HEAD_KEY_USER_AGENT, UserAgent.instance());
        MultiValueMap<String, String> globalHeaders = NoHttp.getInitializeConfig().getHeaders();
        for (Map.Entry<String, List<String>> headersEntry : globalHeaders.entrySet()) {
            String key = headersEntry.getKey();
            List<String> valueList = headersEntry.getValue();
            for (String value : valueList) {
                mHeaders.add(key, value);
            }
        }

        mParams = new Params();
        MultiValueMap<String, String> globalParams = NoHttp.getInitializeConfig().getParams();
        for (Map.Entry<String, List<String>> paramsEntry : globalParams.entrySet()) {
            List<String> valueList = paramsEntry.getValue();
            for (String value : valueList) {
                mParams.add(paramsEntry.getKey(), value);
            }
        }
    }

    /**
     * Return url of handle.
     */
    public String url() {
        StringBuilder urlBuilder = new StringBuilder(url);
        // first body.
        if (hasDefineRequestBody()) {
            buildUrl(urlBuilder);
            return urlBuilder.toString();
        }
        // form or push params.
        if (getRequestMethod().allowRequestBody())
            return urlBuilder.toString();

        // third common post.
        buildUrl(urlBuilder);
        return urlBuilder.toString();
    }

    /**
     * Build complete url.
     */
    private void buildUrl(StringBuilder urlBuilder) {
        StringBuilder paramBuilder = buildCommonParams(getParamKeyValues(), getParamsEncoding());
        if (paramBuilder.length() <= 0) return;
        if (url.contains("?") && url.contains("=")) urlBuilder.append("&");
        else if (!url.endsWith("?")) urlBuilder.append("?");
        urlBuilder.append(paramBuilder);
    }

    /**
     * return method of handle.
     */
    public RequestMethod getRequestMethod() {
        return mRequestMethod;
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path(123);
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/123
     */
    public T path(int value) {
        return path(Integer.toString(value));
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path(456L);
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/456
     */
    public T path(long value) {
        return path(Long.toString(value));
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path(true);
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/true
     */
    public T path(boolean value) {
        return path(Boolean.toString(value));
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path('a');
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/a
     */
    public T path(char value) {
        return path(String.valueOf(value));
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path(456.99D);
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/456.99
     */
    public T path(double value) {
        return path(Double.toString(value));
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path(456.5F);
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/456.5
     */
    public T path(float value) {
        return path(Float.toString(value));
    }

    /**
     * Add the path to the URL, such as:
     * <pre>
     *     String url = "http://www.nohttp.net/xx";
     *     StringRequest req = new StringRequest(url);
     *     req.path("oo");
     *     ...
     * </pre>
     * The real url of Request is: http://www.nohttp.net/xx/oo
     */
    public T path(String value) {
        if (value != null) {
            value = value.trim();
            if (!TextUtils.isEmpty(value)) {
                if (!url.endsWith("/"))
                    url += "/";
                url += value;
            }
        }
        return (T) this;
    }

    /**
     * Set the connection timeout time.
     *
     * @param connectTimeout timeout number, unit is a millisecond.
     */
    public T setConnectTimeout(int connectTimeout) {
        mConnectTimeout = connectTimeout;
        return (T) this;
    }

    /**
     * Get the connection timeout time, Unit is a millisecond.
     */
    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    /**
     * Set the read timeout time.
     *
     * @param readTimeout timeout number, unit is a millisecond.
     */
    public T setReadTimeout(int readTimeout) {
        mReadTimeout = readTimeout;
        return (T) this;
    }

    /**
     * Get the read timeout time, Unit is a millisecond.
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * Set the {@link SSLSocketFactory} for this handle.
     *
     * @param socketFactory {@link SSLSocketFactory}, {@link SSLUtils}.
     * @see SSLUtils
     */
    public T setSSLSocketFactory(SSLSocketFactory socketFactory) {
        mSSLSocketFactory = socketFactory;
        return (T) this;
    }

    /**
     * Get SSLSocketFactory.
     *
     * @return {@link SSLSocketFactory}.
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLSocketFactory;
    }

    /**
     * Set the {@link HostnameVerifier}.
     *
     * @param hostnameVerifier {@link HostnameVerifier}.
     */
    public T setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        mHostnameVerifier = hostnameVerifier;
        return (T) this;
    }

    /**
     * Get the HostnameVerifier.
     *
     * @return {@link HostnameVerifier}.
     */
    public HostnameVerifier getHostnameVerifier() {
        return mHostnameVerifier;
    }

    /**
     * Set proxy server, such as:
     * <pre>
     *     Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("64.233.162.83", 80));
     * </pre>
     *
     * @param proxy {@link Proxy}.
     */
    public T setProxy(Proxy proxy) {
        this.mProxy = proxy;
        return (T) this;
    }

    /**
     * Get proxy server.
     */
    public Proxy getProxy() {
        return mProxy;
    }

    /**
     * Set redirect interface.
     *
     * @param redirectHandler {@link RedirectHandler}.
     */
    public T setRedirectHandler(RedirectHandler redirectHandler) {
        mRedirectHandler = redirectHandler;
        return (T) this;
    }

    /**
     * Get the redirect handler.
     */
    public RedirectHandler getRedirectHandler() {
        return mRedirectHandler;
    }

    /**
     * Set the handle fails retry count.The default value is 0, that is to say, after the failure will not go to
     * this to initiate the handle again.
     *
     * @param count the retry count, the default value is 0.
     */
    public T setRetryCount(int count) {
        this.mRetryCount = count;
        return (T) this;
    }

    /**
     * Get the failure after retries.
     *
     * @return The default value is 0.
     */
    public int getRetryCount() {
        return mRetryCount;
    }

    /**
     * Add a new key-value header.
     *
     * @param key   key.
     * @param value value.
     */
    public T addHeader(String key, String value) {
        mHeaders.add(key, value);
        return (T) this;
    }

    /**
     * If there is a key to delete, and then add a new key-value header.
     *
     * @param key   key.
     * @param value value.
     */
    public T setHeader(String key, String value) {
        mHeaders.set(key, value);
        return (T) this;
    }

    /**
     * <p>Add a {@link HttpCookie}.</p>
     * Just like the:
     * <pre>
     *     HttpCookie httpCookie = getHttpCookie();
     *     if(httpCookie != null)
     *          handle.addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
     *     ...
     * </pre>
     *
     * @param cookie {@link HttpCookie}.
     */
    public T addHeader(HttpCookie cookie) {
        if (cookie != null)
            mHeaders.add(Headers.HEAD_KEY_COOKIE, cookie.getName() + "=" + cookie.getValue());
        return (T) this;
    }

    /**
     * Remove the key from the information.
     *
     * @param key key.
     */
    public T removeHeader(String key) {
        mHeaders.remove(key);
        return (T) this;
    }

    /**
     * Remove all header.
     */
    public T removeAllHeader() {
        mHeaders.clear();
        return (T) this;
    }

    /**
     * Does it contain a handle header?
     */
    public boolean containsHeader(String key) {
        return mHeaders.containsKey(key);
    }

    /**
     * Get all Heads.
     *
     * @return {@code Headers}.
     */
    public Headers getHeaders() {
        return mHeaders;
    }

    /**
     * Set the accept for head.
     *
     * @param accept such as: {@code application/json}.
     */
    public T setAccept(String accept) {
        mHeaders.set(Headers.HEAD_KEY_ACCEPT, accept);
        return (T) this;
    }

    /**
     * Set the acceptLanguage for head.
     *
     * @param acceptLanguage such as "zh-CN,zh", "en-US,us".
     */
    public T setAcceptLanguage(String acceptLanguage) {
        mHeaders.set(Headers.HEAD_KEY_ACCEPT_LANGUAGE, acceptLanguage);
        return (T) this;
    }

    /**
     * The length of the handle body.
     *
     * @return such as: {@code 2048}.
     */
    public long getContentLength() {
        CounterOutputStream outputStream = new CounterOutputStream();
        try {
            onWriteRequestBody(outputStream);
        } catch (IOException e) {
            Logger.e(e);
        }
        return outputStream.get();
    }

    /**
     * Set the contentType for head.
     *
     * @param contentType such as: {@code application/json}.
     */
    public T setContentType(String contentType) {
        mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType);
        return (T) this;
    }

    /**
     * Get contentType of handle.
     *
     * @return string, such as: {@code application/json}.
     */
    public String getContentType() {
        String contentType = mHeaders.getContentType();
        if (!TextUtils.isEmpty(contentType))
            return contentType;
        if (getRequestMethod().allowRequestBody() && isMultipartFormEnable())
            return Headers.HEAD_VALUE_CONTENT_TYPE_FORM_DATA + "; boundary=" + boundary;
        else
            return Headers.HEAD_VALUE_CONTENT_TYPE_URLENCODED + "; charset=" + getParamsEncoding();
    }

    /**
     * Set the UA for client.
     *
     * @param userAgent such as: {@code Mozilla/5.0 (Android U; Android 5.0) AppleWebKit/533.1 (KHTML, like Gecko) Version/5.0 Safari/533.1}.
     */
    public T setUserAgent(String userAgent) {
        mHeaders.set(Headers.HEAD_KEY_USER_AGENT, userAgent);
        return (T) this;
    }

    /**
     * Set the params encoding.
     *
     * @param encoding such as {@code utf-8, gbk, gb2312}.
     */
    public T setParamsEncoding(String encoding) {
        this.mParamEncoding = encoding;
        return (T) this;
    }

    /**
     * Get the params encoding.
     *
     * @return such as {@code utf-8}, default is {@code utf-8}.
     * @see #setParamsEncoding(String)
     */
    public String getParamsEncoding() {
        if (TextUtils.isEmpty(mParamEncoding))
            mParamEncoding = "utf-8";
        return mParamEncoding;
    }

    /**
     * Mandatory set to form pattern to transmit data.
     * <pre>
     *     The handle method must be one of the following: {@code POST/PUT/PATCH/DELETE}.
     *     But the Android system under API level 19 does not support the DELETE.
     * </pre>
     *
     * @param enable true enable, other wise false.
     */
    public T setMultipartFormEnable(boolean enable) {
        validateMethodForBody("Form body");
        isMultipartFormEnable = enable;
        return (T) this;
    }

    /**
     * Is it a form?
     * <pre>
     *     The handle method must be one of the following: {@code POST/PUT/PATCH/DELETE}.
     *     But the Android system under API level 19 does not support the DELETE.
     * </pre>
     *
     * @return true enable, other wise false.
     */
    public boolean isMultipartFormEnable() {
        return isMultipartFormEnable || hasBinary();
    }

    /**
     * Has Binary.
     *
     * @return true, other wise is false.
     */
    private boolean hasBinary() {
        Set<String> keys = mParams.keySet();
        for (String key : keys) {
            List<Object> values = mParams.getValues(key);
            for (Object value : values) {
                if (value instanceof Binary || value instanceof File)
                    return true;
            }
        }
        return false;
    }

    /**
     * Add {@link Integer} param.
     */
    public T add(String key, int value) {
        add(key, Integer.toString(value));
        return (T) this;
    }

    /**
     * Add {@link Long} param.
     */
    public T add(String key, long value) {
        add(key, Long.toString(value));
        return (T) this;
    }

    /**
     * Add {@link Boolean} param.
     */
    public T add(String key, boolean value) {
        add(key, Boolean.toString(value));
        return (T) this;
    }

    /**
     * Add {@code char} param.
     */
    public T add(String key, char value) {
        add(key, String.valueOf(value));
        return (T) this;
    }

    /**
     * Add {@link Double} param.
     */
    public T add(String key, double value) {
        add(key, Double.toString(value));
        return (T) this;
    }

    /**
     * Add {@link Float} param.
     */
    public T add(String key, float value) {
        add(key, Float.toString(value));
        return (T) this;
    }

    /**
     * Add {@link Short} param.
     */
    public T add(String key, short value) {
        add(key, Integer.toString(value));
        return (T) this;
    }

    /**
     * Add {@link String} param.
     */
    public T add(String key, String value) {
        if (!TextUtils.isEmpty(key))
            mParams.add(key, TextUtils.isEmpty(value) ? "" : value);
        return (T) this;
    }

    /**
     * Set {@link String} param.
     */
    public T set(String key, String value) {
        if (!TextUtils.isEmpty(key))
            mParams.set(key, TextUtils.isEmpty(value) ? "" : value);
        return (T) this;
    }

    /**
     * Validate method for handle body.
     *
     * @param methodObject message.
     */
    private void validateMethodForBody(String methodObject) {
        if (!getRequestMethod().allowRequestBody())
            throw new IllegalArgumentException(methodObject + " only supports these handle methods: POST/PUT/PATCH/DELETE.");
    }

    /**
     * Add {@link File} param.
     */
    public T add(String key, File file) {
        validateMethodForBody("The File param");
        add(key, new FileBinary(file));
        return (T) this;
    }

    /**
     * Add {@link Binary} param.
     */
    public T add(String key, Binary binary) {
        validateMethodForBody("The Binary param");
        mParams.add(key, binary);
        return (T) this;
    }

    /**
     * Set {@link Binary} param.
     */
    public T set(String key, Binary binary) {
        validateMethodForBody("The Binary param");
        mParams.set(key, binary);
        return (T) this;
    }

    /**
     * Add {@link Binary} params;
     */
    public T add(String key, List<Binary> binaries) {
        validateMethodForBody("The List<Binary> param");
        for (Binary binary : binaries)
            mParams.add(key, binary);
        return (T) this;
    }

    /**
     * Set {@link Binary} params.
     */
    public T set(String key, List<Binary> binaries) {
        validateMethodForBody("The List<Binary> param");
        mParams.remove(key);
        for (Binary binary : binaries)
            mParams.add(key, binary);
        return (T) this;
    }

    /**
     * Add all params.
     */
    public T add(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) value = "";

            if (value instanceof File) {
                mParams.add(key, new FileBinary((File) value));
            } else if (value instanceof Binary) {
                mParams.add(key, value);
            } else if (value instanceof List) {
                List values = (List) value;
                for (int i = 0; i < values.size(); i++) {
                    Object o = values.get(i);
                    if (o == null) o = "";

                    if (o instanceof File) {
                        mParams.add(key, new FileBinary((File) o));
                    } else if (o instanceof Binary) {
                        mParams.add(key, value);
                    } else {
                        mParams.add(key, o.toString());
                    }
                }
            } else {
                mParams.add(key, value.toString());
            }
        }
        return (T) this;
    }

    /**
     * Set all params.
     */
    public T set(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) value = "";

            if (value instanceof File) {
                mParams.set(key, new FileBinary((File) value));
            } else if (value instanceof Binary) {
                mParams.set(key, value);
            } else if (value instanceof List) {
                mParams.remove(key);
                List values = (List) value;
                for (int i = 0; i < values.size(); i++) {
                    Object o = values.get(i);
                    if (o == null) o = "";

                    if (o instanceof File) {
                        mParams.add(key, new FileBinary((File) o));
                    } else if (o instanceof Binary) {
                        mParams.add(key, value);
                    } else {
                        mParams.add(key, o.toString());
                    }
                }
            } else {
                mParams.set(key, value.toString());
            }
        }
        return (T) this;
    }

    /**
     * Remove a handle param by key.
     */
    public T remove(String key) {
        mParams.remove(key);
        return (T) this;
    }

    /**
     * Remove all handle param.
     */
    public T removeAll() {
        mParams.clear();
        return (T) this;
    }

    /**
     * Get the parameters of key-value pairs.
     *
     * @return Not empty Map.
     */
    public MultiValueMap<String, Object> getParamKeyValues() {
        return mParams;
    }

    /**
     * Validate param null.
     *
     * @param body        handle body.
     * @param contentType content type.
     */
    private void validateParamForBody(Object body, String contentType) {
        if (body == null || TextUtils.isEmpty(contentType))
            throw new NullPointerException("The requestBody and contentType must be can't be null");
    }

    /**
     * Is there a custom handle inclusions.
     *
     * @return Returns true representatives have, return false on behalf of the no.
     */
    private boolean hasDefineRequestBody() {
        return mRequestBody != null;
    }

    /**
     * Set the package body, which can be any data stream. But the type of stream must be
     * {@link ByteArrayInputStream} or {@link FileInputStream}.
     *
     * @param requestBody any data stream, you don't need to close it.
     * @param contentType such as: {@code application/json;json}, {@code image/*}.
     * @see #setDefineRequestBody(String, String)
     * @see #setDefineRequestBodyForJson(JSONObject)
     * @see #setDefineRequestBodyForJson(String)
     * @see #setDefineRequestBodyForXML(String)
     */
    public T setDefineRequestBody(InputStream requestBody, String contentType) {
        validateMethodForBody("Request body");
        validateParamForBody(requestBody, contentType);
        if (requestBody instanceof ByteArrayInputStream || requestBody instanceof FileInputStream) {
            this.mRequestBody = requestBody;
            mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType);
        } else
            throw new IllegalArgumentException("Can only accept ByteArrayInputStream and FileInputStream type of stream");
        return (T) this;
    }

    /**
     * Set the handle body and content type.
     *
     * @param requestBody string body.
     * @param contentType such as: {@code application/json;json}, {@code image/*}.
     * @see #setDefineRequestBody(InputStream, String)
     * @see #setDefineRequestBodyForJson(JSONObject)
     * @see #setDefineRequestBodyForJson(String)
     * @see #setDefineRequestBodyForXML(String)
     */
    public T setDefineRequestBody(String requestBody, String contentType) {
        validateMethodForBody("Request body");
        validateParamForBody(requestBody, contentType);
        try {
            mRequestBody = IOUtils.toInputStream(requestBody, getParamsEncoding());
            mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType + "; charset=" + getParamsEncoding());
        } catch (UnsupportedEncodingException e) {
            mRequestBody = IOUtils.toInputStream(requestBody);
            mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType);
        }
        return (T) this;
    }

    /**
     * Set the handle json body.
     *
     * @param jsonBody json body.
     * @see #setDefineRequestBody(InputStream, String)
     * @see #setDefineRequestBody(String, String)
     * @see #setDefineRequestBodyForJson(JSONObject)
     * @see #setDefineRequestBodyForXML(String)
     */
    public T setDefineRequestBodyForJson(String jsonBody) {
        setDefineRequestBody(jsonBody, Headers.HEAD_VALUE_CONTENT_TYPE_JSON);
        return (T) this;
    }

    /**
     * Set the handle json body.
     *
     * @param jsonBody json body.
     * @see #setDefineRequestBody(InputStream, String)
     * @see #setDefineRequestBody(String, String)
     * @see #setDefineRequestBodyForJson(String)
     * @see #setDefineRequestBodyForXML(String)
     */
    public T setDefineRequestBodyForJson(JSONObject jsonBody) {
        setDefineRequestBody(jsonBody.toString(), Headers.HEAD_VALUE_CONTENT_TYPE_JSON);
        return (T) this;
    }

    /**
     * Set the handle XML body.
     *
     * @param xmlBody xml body.
     * @see #setDefineRequestBody(InputStream, String)
     * @see #setDefineRequestBody(String, String)
     * @see #setDefineRequestBody(String, String)
     * @see #setDefineRequestBodyForJson(String)
     */
    public T setDefineRequestBodyForXML(String xmlBody) {
        setDefineRequestBody(xmlBody, Headers.HEAD_VALUE_CONTENT_TYPE_XML);
        return (T) this;
    }

    /**
     * To getList custom inclusions.
     *
     * @return {@link InputStream}.
     */
    public InputStream getDefineRequestBody() {
        return mRequestBody;
    }

    /**
     * Call before carry out the handle, you can do some preparation work.
     */
    public void onPreExecute() {
        // Do some time-consuming operation.
    }

    /**
     * Send handle body data.
     */
    public void onWriteRequestBody(OutputStream writer) throws IOException {
        if (hasDefineRequestBody()) {
            writeRequestBody(writer);
        } else if (isMultipartFormEnable()) {
            writeFormStreamData(writer);
        } else {
            writeParamStreamData(writer);
        }
    }

    /**
     * Send handle requestBody.
     */
    private void writeRequestBody(OutputStream writer) throws IOException {
        if (mRequestBody != null) {
            if (writer instanceof CounterOutputStream) {
                ((CounterOutputStream) writer).writeLength(mRequestBody.available());
            } else {
                IOUtils.write(mRequestBody, writer);
                IOUtils.closeQuietly(mRequestBody);
                mRequestBody = null;
            }
        }
    }

    /**
     * Send form data.
     */
    private void writeFormStreamData(OutputStream writer) throws IOException {
        if (isCanceled()) return;
        Set<String> keys = mParams.keySet();
        for (String key : keys) {
            if (TextUtils.isEmpty(key)) continue;

            List<Object> values = mParams.getValues(key);
            for (Object value : values) {
                if (value instanceof String) {
                    if (!(writer instanceof CounterOutputStream))
                        Logger.i(key + "=" + value);
                    writeFormString(writer, key, (String) value);
                } else if (value instanceof Binary) {
                    if (!(writer instanceof CounterOutputStream))
                        Logger.i(key + " is Binary");
                    writeFormBinary(writer, key, (Binary) value);
                }
                writer.write("\r\n".getBytes());
            }
        }
        writer.write((endBoundary).getBytes());
    }

    /**
     * Send text data in a form.
     *
     * @param key   equivalent to form the name of the input label, {@code "Content-Disposition: form-data; name=key"}.
     * @param value equivalent to form the value of the input label.
     */
    private void writeFormString(OutputStream writer, String key, String value) throws IOException {
        String stringFieldBuilder = startBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n";

        writer.write(stringFieldBuilder.getBytes(getParamsEncoding()));
        writer.write(value.getBytes(getParamsEncoding()));
    }

    /**
     * Send binary data in a form.
     */
    private void writeFormBinary(OutputStream writer, String key, Binary value) throws IOException {
        String binaryFieldBuilder = startBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + value.getFileName() + "\"\r\n" +
                "Content-Type: " + value.getMimeType() + "\r\n\r\n";
        writer.write(binaryFieldBuilder.getBytes());

        if (writer instanceof CounterOutputStream) {
            ((CounterOutputStream) writer).writeLength(value.getLength());
        } else {
            value.onWriteBinary(writer);
        }
    }

    /**
     * Write params.
     */
    private void writeParamStreamData(OutputStream writer) throws IOException {
        StringBuilder paramBuilder = BasicRequest.buildCommonParams(mParams, getParamsEncoding());
        if (paramBuilder.length() > 0) {
            String params = paramBuilder.toString();
            if (!(writer instanceof CounterOutputStream))
                Logger.i("Body: " + params);
            IOUtils.write(params.getBytes(), writer);
        }
    }

    /**
     * Set tag of task, you can use this mTag cancel this request ,
     * but this tag should uniqueness,
     * At the end of the task is returned to you.
     */
    public T setTag(Object tag) {
        this.mTag = tag;
        return (T) this;
    }

    /**
     * Should to return the tag of the object.
     */
    public Object getTag() {
        return this.mTag;
    }

    /**
     * Set the priority of the handle object. The default priority is {@link Priority#DEFAULT}.
     *
     * @param priority {@link Priority}.
     */
    public T setPriority(Priority priority) {
        this.mPriority = priority;
        return (T) this;
    }

    /**
     * Get the priority of the handle object.
     */
    public Priority getPriority() {
        return mPriority;
    }

    /**
     * Set the sequence of handle.
     *
     * @param sequence sequence code.
     */
    public T setSequence(int sequence) {
        this.sequence = sequence;
        return (T) this;
    }

    /**
     * Get the sequence in the queue, under the condition of two requests as priority.
     * <pre>
     *     Calculation method:
     *     {@code left.sequence-right.sequence} decision to order.
     * </pre>
     */
    public int getSequence() {
        return this.sequence;
    }

    @Override
    public final int compareTo(BasicRequest another) {
        final Priority me = getPriority();
        final Priority it = another.getPriority();
        return me == it ? getSequence() - another.getSequence() : it.ordinal() - me.ordinal();
    }

    @Override
    public void start() {
        this.isStart = true;
    }

    @Override
    public boolean isStarted() {
        return isStart;
    }

    @Override
    public void cancel() {
        if (!isCanceled) {
            isCanceled = true;
            if (mRequestBody != null)
                IOUtils.closeQuietly(mRequestBody);

            // cancel file upload
            Set<String> keys = mParams.keySet();
            for (String key : keys) {
                if (TextUtils.isEmpty(key)) continue;

                List<Object> values = mParams.getValues(key);
                for (Object value : values)
                    if (value != null && value instanceof Binary)
                        ((Binary) value).cancel();
            }
        }
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public void finish() {
        this.isFinished = true;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Set cancel sign.
     *
     * @param sign a object.
     */
    public T setCancelSign(Object sign) {
        this.mCancelSign = sign;
        return (T) this;
    }

    public Object getCancelSign(){
        return mCancelSign;
    }

    /**
     * Cancel operation by contrast the sign.
     *
     * @param sign an object that can be null.
     */
    public void cancelBySign(Object sign) {
        if (mCancelSign == sign || (sign != null && mCancelSign != null && mCancelSign.equals(sign)))
            cancel();
    }

    ////////// static module /////////

    /**
     * Split joint non form data.
     *
     * @param paramMap      param map.
     * @param encodeCharset charset.
     * @return string parameter combination, each key value on nails with {@code "&"} space.
     */
    public static StringBuilder buildCommonParams(MultiValueMap<String, Object> paramMap, String encodeCharset) {
        StringBuilder paramBuilder = new StringBuilder();
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            if (TextUtils.isEmpty(key)) continue;

            List<Object> values = paramMap.getValues(key);
            for (Object value : values) {
                if (value != null && value instanceof CharSequence) {
                    paramBuilder.append("&").append(key).append("=");
                    try {
                        paramBuilder.append(URLEncoder.encode(value.toString(), encodeCharset));
                    } catch (UnsupportedEncodingException e) {
                        paramBuilder.append(value.toString());
                    }
                }
            }
        }
        if (paramBuilder.length() > 0)
            paramBuilder.deleteCharAt(0);
        return paramBuilder;
    }

    /**
     * Randomly generated boundary mark.
     *
     * @return Random code.
     */
    public static String createBoundary() {
        StringBuilder sb = new StringBuilder("----NoHttpFormBoundary");
        for (int t = 1; t < 12; t++) {
            long time = System.currentTimeMillis() + t;
            if (time % 3L == 0L) {
                sb.append((char) (int) time % '\t');
            } else if (time % 3L == 1L) {
                sb.append((char) (int) (65L + time % 26L));
            } else {
                sb.append((char) (int) (97L + time % 26L));
            }
        }
        return sb.toString();
    }

}