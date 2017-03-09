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

import com.yanzhenjie.nohttp.tools.CounterOutputStream;
import com.yanzhenjie.nohttp.tools.HeaderUtil;
import com.yanzhenjie.nohttp.tools.IOUtils;
import com.yanzhenjie.nohttp.tools.LinkedMultiValueMap;
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
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * <p>
 * Implement all the methods of the base class {@link IBasicRequest}.
 * </p>
 * Created in Nov 4, 2015 8:28:50 AM.
 *
 * @author Yan Zhenjie.
 */
public abstract class BasicRequest implements IBasicRequest {

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
    private SSLSocketFactory mSSLSocketFactory = null;
    /**
     * HostnameVerifier.
     */
    private HostnameVerifier mHostnameVerifier = null;
    /**
     * Connect timeout of request.
     */
    private int mConnectTimeout = NoHttp.getConnectTimeout();
    /**
     * Read data timeout.
     */
    private int mReadTimeout = NoHttp.getReadTimeout();
    /**
     * Request heads.
     */
    private Headers mHeaders;
    /**
     * After the failure of retries.
     */
    private int mRetryCount;
    /**
     * The params encoding.
     */
    private String mParamEncoding;
    /**
     * Param collection.
     */
    private MultiValueMap<String, Object> mParamKeyValues;
    /**
     * RequestBody.
     */
    private InputStream mRequestBody;
    /**
     * Redirect handler.
     */
    private RedirectHandler mRedirectHandler;
    /**
     * Request queue
     */
    private BlockingQueue<?> blockingQueue;
    /**
     * The record has started.
     */
    private boolean isStart = false;
    /**
     * The request is completed.
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
     * Tag of request.
     */
    private Object mTag;

    /**
     * Create a request, RequestMethod is {@link RequestMethod#GET}.
     *
     * @param url request address, like: http://www.yanzhenjie.com.
     */
    public BasicRequest(String url) {
        this(url, RequestMethod.GET);
    }

    /**
     * Create a request.
     *
     * @param url           request adress, like: http://www.yanzhenjie.com.
     * @param requestMethod request method, like {@link RequestMethod#GET}, {@link RequestMethod#POST}.
     */
    public BasicRequest(String url, RequestMethod requestMethod) {
        this.url = url;
        mRequestMethod = requestMethod;

        mHeaders = new HttpHeaders();
        mHeaders.set(Headers.HEAD_KEY_ACCEPT, Headers.HEAD_VALUE_ACCEPT_ALL);
        mHeaders.set(Headers.HEAD_KEY_ACCEPT_ENCODING, Headers.HEAD_VALUE_ACCEPT_ENCODING_GZIP_DEFLATE);
        mHeaders.set(Headers.HEAD_KEY_ACCEPT_LANGUAGE, HeaderUtil.systemAcceptLanguage());
        mHeaders.set(Headers.HEAD_KEY_USER_AGENT, UserAgent.instance());

        mParamKeyValues = new LinkedMultiValueMap<>();
    }

    @Override
    public IBasicRequest setPriority(Priority priority) {
        this.mPriority = priority;
        return this;
    }

    @Override
    public Priority getPriority() {
        return mPriority;
    }

    @Override
    public IBasicRequest setSequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public int getSequence() {
        return this.sequence;
    }

    @Override
    public final int compareTo(IBasicRequest another) {
        final Priority me = getPriority();
        final Priority it = another.getPriority();
        return me == it ? getSequence() - another.getSequence() : it.ordinal() - me.ordinal();
    }

    @Override
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
     *
     * @param urlBuilder url StringBuilder.
     */
    private void buildUrl(StringBuilder urlBuilder) {
        StringBuilder paramBuilder = buildCommonParams(getParamKeyValues(), getParamsEncoding());
        if (paramBuilder.length() <= 0) return;
        if (url.contains("?") && url.contains("=")) urlBuilder.append("&");
        else if (!url.endsWith("?")) urlBuilder.append("?");
        urlBuilder.append(paramBuilder);
    }

    @Override
    public RequestMethod getRequestMethod() {
        return mRequestMethod;
    }

    @Override
    public IBasicRequest setMultipartFormEnable(boolean enable) {
        validateMethodForBody("Form body");
        isMultipartFormEnable = enable;
        return this;
    }

    @Override
    public boolean isMultipartFormEnable() {
        return isMultipartFormEnable || hasBinary();
    }

    @Override
    public IBasicRequest setProxy(Proxy proxy) {
        this.mProxy = proxy;
        return this;
    }

    @Override
    public Proxy getProxy() {
        return mProxy;
    }

    @Override
    public IBasicRequest setSSLSocketFactory(SSLSocketFactory socketFactory) {
        mSSLSocketFactory = socketFactory;
        return this;
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLSocketFactory;
    }

    @Override
    public IBasicRequest setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        mHostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return mHostnameVerifier;
    }

    @Override
    public IBasicRequest setConnectTimeout(int connectTimeout) {
        mConnectTimeout = connectTimeout;
        return this;
    }

    @Override
    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    @Override
    public IBasicRequest setReadTimeout(int readTimeout) {
        mReadTimeout = readTimeout;
        return this;
    }

    @Override
    public int getReadTimeout() {
        return mReadTimeout;
    }

    @Override
    public IBasicRequest addHeader(String key, String value) {
        mHeaders.add(key, value);
        return this;
    }

    @Override
    public IBasicRequest setHeader(String key, String value) {
        mHeaders.set(key, value);
        return this;
    }

    @Override
    public IBasicRequest addHeader(HttpCookie cookie) {
        if (cookie != null)
            mHeaders.add(Headers.HEAD_KEY_COOKIE, cookie.getName() + "=" + cookie.getValue());
        return this;
    }

    @Override
    public IBasicRequest removeHeader(String key) {
        mHeaders.remove(key);
        return this;
    }

    @Override
    public IBasicRequest removeAllHeader() {
        mHeaders.clear();
        return this;
    }

    @Override
    public Headers headers() {
        return mHeaders;
    }

    @Override
    public IBasicRequest setAccept(String accept) {
        mHeaders.set(Headers.HEAD_KEY_ACCEPT, accept);
        return this;
    }

    @Override
    public IBasicRequest setAcceptLanguage(String acceptLanguage) {
        mHeaders.set(Headers.HEAD_KEY_ACCEPT_LANGUAGE, acceptLanguage);
        return this;
    }

    @Override
    public long getContentLength() {
        CounterOutputStream outputStream = new CounterOutputStream();
        try {
            onWriteRequestBody(outputStream);
        } catch (IOException e) {
            Logger.e(e);
        }
        return outputStream.get();
    }

    @Override
    public IBasicRequest setContentType(String contentType) {
        mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public String getContentType() {
        String contentType = mHeaders.getValue(Headers.HEAD_KEY_CONTENT_TYPE, 0);
        if (!TextUtils.isEmpty(contentType))
            return contentType;
        if (getRequestMethod().allowRequestBody() && isMultipartFormEnable())
            return Headers.HEAD_VALUE_ACCEPT_MULTIPART_FORM_DATA + "; boundary=" + boundary;
        else
            return Headers.HEAD_VALUE_ACCEPT_APPLICATION_X_WWW_FORM_URLENCODED + "; charset=" + getParamsEncoding();
    }

    @Override
    public IBasicRequest setUserAgent(String userAgent) {
        mHeaders.set(Headers.HEAD_KEY_USER_AGENT, userAgent);
        return this;
    }

    @Override
    public IBasicRequest setRetryCount(int count) {
        this.mRetryCount = count;
        return this;
    }

    @Override
    public int getRetryCount() {
        return mRetryCount;
    }

    @Override
    public IBasicRequest setParamsEncoding(String encoding) {
        this.mParamEncoding = encoding;
        return this;
    }

    @Override
    public String getParamsEncoding() {
        if (TextUtils.isEmpty(mParamEncoding))
            mParamEncoding = "utf-8";
        return mParamEncoding;
    }

    @Override
    public IBasicRequest add(String key, int value) {
        add(key, Integer.toString(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, long value) {
        add(key, Long.toString(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, boolean value) {
        add(key, String.valueOf(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, char value) {
        add(key, String.valueOf(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, double value) {
        add(key, Double.toString(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, float value) {
        add(key, Float.toString(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, short value) {
        add(key, Integer.toString(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, byte value) {
        add(key, Integer.toString(value));
        return this;
    }

    @Override
    public IBasicRequest add(String key, String value) {
        if (value != null) {
            mParamKeyValues.add(key, value);
        }
        return this;
    }

    @Override
    public IBasicRequest set(String key, String value) {
        if (value != null)
            mParamKeyValues.set(key, value);
        return this;
    }

    /**
     * Validate method for request body.
     *
     * @param methodObject message.
     */
    private void validateMethodForBody(String methodObject) {
        if (!getRequestMethod().allowRequestBody())
            throw new IllegalArgumentException(methodObject + " only supports these request methods: " +
                    "POST/PUT/PATCH/DELETE.");
    }

    @Override
    public IBasicRequest add(String key, Binary binary) {
        validateMethodForBody("The Binary param");
        mParamKeyValues.add(key, binary);
        return this;
    }

    @Override
    public IBasicRequest set(String key, Binary binary) {
        validateMethodForBody("The Binary param");
        mParamKeyValues.set(key, binary);
        return this;
    }

    @Override
    public IBasicRequest add(String key, File file) {
        validateMethodForBody("The File param");
        add(key, new FileBinary(file));
        return this;
    }

    @Override
    public IBasicRequest set(String key, File file) {
        validateMethodForBody("The File param");
        set(key, new FileBinary(file));
        return this;
    }

    @Override
    public IBasicRequest add(String key, List<Binary> binaries) {
        validateMethodForBody("The List<Binary> param");
        if (binaries != null) {
            for (Binary binary : binaries)
                mParamKeyValues.add(key, binary);
        }
        return this;
    }

    @Override
    public IBasicRequest set(String key, List<Binary> binaries) {
        validateMethodForBody("The List<Binary> param");
        mParamKeyValues.remove(key);
        add(key, binaries);
        return this;
    }

    @Override
    public IBasicRequest add(Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> stringEntry : params.entrySet())
                add(stringEntry.getKey(), stringEntry.getValue());
        }
        return this;
    }

    @Override
    public IBasicRequest set(Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> stringEntry : params.entrySet())
                set(stringEntry.getKey(), stringEntry.getValue());
        }
        return this;
    }

    @Override
    public IBasicRequest remove(String key) {
        mParamKeyValues.remove(key);
        return this;
    }

    @Override
    public IBasicRequest removeAll() {
        mParamKeyValues.clear();
        return this;
    }

    @Override
    public MultiValueMap<String, Object> getParamKeyValues() {
        return mParamKeyValues;
    }

    /**
     * Validate param null.
     *
     * @param body        request body.
     * @param contentType content type.
     */
    private void validateParamForBody(Object body, String contentType) {
        if (body == null || TextUtils.isEmpty(contentType))
            throw new NullPointerException("The requestBody and contentType must be can't be null");
    }

    @Override
    public IBasicRequest setDefineRequestBody(InputStream requestBody, String contentType) {
        validateMethodForBody("Request body");
        validateParamForBody(requestBody, contentType);
        if (requestBody instanceof ByteArrayInputStream || requestBody instanceof FileInputStream) {
            this.mRequestBody = requestBody;
            mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType);
        } else {
            throw new IllegalArgumentException("Can only accept ByteArrayInputStream and FileInputStream type of " +
                    "stream");
        }
        return this;
    }

    @Override
    public IBasicRequest setDefineRequestBody(String requestBody, String contentType) {
        validateMethodForBody("Request body");
        validateParamForBody(requestBody, contentType);
        try {
            mRequestBody = IOUtils.toInputStream(requestBody, getParamsEncoding());
            mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType + "; charset=" + getParamsEncoding());
        } catch (UnsupportedEncodingException e) {
            mRequestBody = IOUtils.toInputStream(requestBody);
            mHeaders.set(Headers.HEAD_KEY_CONTENT_TYPE, contentType);
        }
        return this;
    }

    @Override
    public IBasicRequest setDefineRequestBodyForJson(String jsonBody) {
        setDefineRequestBody(jsonBody, Headers.HEAD_VALUE_ACCEPT_APPLICATION_JSON);
        return this;
    }

    @Override
    public IBasicRequest setDefineRequestBodyForJson(JSONObject jsonBody) {
        setDefineRequestBody(jsonBody.toString(), Headers.HEAD_VALUE_ACCEPT_APPLICATION_JSON);
        return this;
    }

    @Override
    public IBasicRequest setDefineRequestBodyForXML(String xmlBody) {
        setDefineRequestBody(xmlBody, Headers.HEAD_VALUE_ACCEPT_APPLICATION_XML);
        return this;
    }

    /**
     * Has Binary.
     *
     * @return true, other wise is false.
     */
    protected boolean hasBinary() {
        Set<String> keys = mParamKeyValues.keySet();
        for (String key : keys) {
            List<Object> values = mParamKeyValues.getValues(key);
            for (Object value : values) {
                if (value instanceof Binary)
                    return true;
            }
        }
        return false;
    }

    /**
     * Is there a custom request inclusions.
     *
     * @return Returns true representatives have, return false on behalf of the no.
     */
    protected boolean hasDefineRequestBody() {
        return mRequestBody != null;
    }

    /**
     * To getList custom inclusions.
     *
     * @return {@link InputStream}.
     */
    protected InputStream getDefineRequestBody() {
        return mRequestBody;
    }

    @Override
    public void onPreExecute() {
        // Do some time-consuming operation.
    }

    @Override
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
     * Send request requestBody.
     *
     * @param writer {@link OutputStream}.
     * @throws IOException write error.
     */
    protected void writeRequestBody(OutputStream writer) throws IOException {
        if (mRequestBody != null) {
            if (writer instanceof CounterOutputStream) {
                writer.write(mRequestBody.available());
            } else {
                IOUtils.write(mRequestBody, writer);
                IOUtils.closeQuietly(mRequestBody);
                mRequestBody = null;
            }
        }
    }

    /**
     * Send form data.
     *
     * @param writer {@link OutputStream}.
     * @throws IOException write error.
     */
    protected void writeFormStreamData(OutputStream writer) throws IOException {
        Set<String> keys = mParamKeyValues.keySet();
        for (String key : keys) {
            List<Object> values = mParamKeyValues.getValues(key);
            for (Object value : values) {
                if (!isCanceled()) {
                    if (value != null && value instanceof String) {
                        if (!(writer instanceof CounterOutputStream))
                            Logger.i(key + "=" + value);
                        writeFormString(writer, key, (String) value);
                    } else if (value != null && value instanceof Binary) {
                        if (!(writer instanceof CounterOutputStream))
                            Logger.i(key + " is Binary");
                        writeFormBinary(writer, key, (Binary) value);
                    }
                    writer.write("\r\n".getBytes());
                }
            }
        }
        writer.write((endBoundary).getBytes());
    }

    /**
     * Send text data in a form.
     *
     * @param writer {@link OutputStream}
     * @param key    equivalent to form the name of the input label, {@code "Content-Disposition: form-data; name=key"}.
     * @param value  equivalent to form the value of the input label.
     * @throws IOException Write the data may be abnormal.
     */
    private void writeFormString(OutputStream writer, String key, String value) throws IOException {
        String stringFieldBuilder = startBoundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + key + "\"\r\n"
                + "Content-Type: text/plain; charset=" + getParamsEncoding() + "\r\n\r\n";

        writer.write(stringFieldBuilder.getBytes(getParamsEncoding()));
        writer.write(value.getBytes(getParamsEncoding()));
    }

    /**
     * Send binary data in a form.
     */
    private void writeFormBinary(OutputStream writer, String key, Binary value) throws IOException {
        if (!value.isCanceled()) {
            String binaryFieldBuilder = startBoundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"" + key + "\"" + "; filename=\"" + value.getFileName() +
                    "\"\r\n"
                    + "Content-Type: " + value.getMimeType() + "\r\n"
                    + "Content-Transfer-Encoding: binary\r\n\r\n";
            writer.write(binaryFieldBuilder.getBytes());

            if (writer instanceof CounterOutputStream) {
                ((CounterOutputStream) writer).write(value.getLength());
            } else {
                value.onWriteBinary(writer);
            }
        }
    }

    /**
     * Write params.
     *
     * @param writer {@link OutputStream}.
     * @throws IOException IOException.
     */
    private void writeParamStreamData(OutputStream writer) throws IOException {
        StringBuilder paramBuilder = buildCommonParams(mParamKeyValues, getParamsEncoding());
        if (paramBuilder.length() > 0) {
            String params = paramBuilder.toString();
            Logger.i("Body: " + params);
            IOUtils.write(params.getBytes(), writer);
        }
    }

    @Override
    public IBasicRequest setRedirectHandler(RedirectHandler redirectHandler) {
        mRedirectHandler = redirectHandler;
        return this;
    }

    @Override
    public RedirectHandler getRedirectHandler() {
        return mRedirectHandler;
    }

    @Override
    public IBasicRequest setTag(Object tag) {
        this.mTag = tag;
        return this;
    }

    @Override
    public Object getTag() {
        return this.mTag;
    }

    @Override
    public void setQueue(BlockingQueue<?> queue) {
        blockingQueue = queue;
    }

    @Override
    public boolean inQueue() {
        return blockingQueue != null && blockingQueue.contains(this);
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
    public void finish() {
        this.isFinished = true;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void cancel() {
        if (!isCanceled) {
            isCanceled = true;
            if (mRequestBody != null)
                IOUtils.closeQuietly(mRequestBody);

            if (blockingQueue != null)
                blockingQueue.remove(this);

            // cancel file upload
            Set<String> keys = mParamKeyValues.keySet();
            for (String key : keys) {
                List<Object> values = mParamKeyValues.getValues(key);
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

    public IBasicRequest setCancelSign(Object sign) {
        this.mCancelSign = sign;
        return this;
    }

    @Override
    public void cancelBySign(Object sign) {
        if (mCancelSign == sign)
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
            List<Object> values = paramMap.getValues(key);
            for (Object value : values) {
                if (value != null && value instanceof CharSequence) {
                    paramBuilder.append("&").append(key).append("=");
                    try {
                        paramBuilder.append(URLEncoder.encode(value.toString(), encodeCharset));
                    } catch (UnsupportedEncodingException e) {
                        Logger.e("Encoding " + encodeCharset + " format is not supported by the system.");
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