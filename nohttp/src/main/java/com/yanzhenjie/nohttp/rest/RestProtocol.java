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
package com.yanzhenjie.nohttp.rest;

import android.os.SystemClock;

import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.cache.CacheEntity;
import com.yanzhenjie.nohttp.error.NotFoundCacheError;
import com.yanzhenjie.nohttp.tools.CacheStore;
import com.yanzhenjie.nohttp.tools.HeaderUtil;
import com.yanzhenjie.nohttp.Connection;
import com.yanzhenjie.nohttp.HttpConnection;
import com.yanzhenjie.nohttp.IBasicRequest;
import com.yanzhenjie.nohttp.NetworkExecutor;
import com.yanzhenjie.nohttp.tools.IOUtils;

import java.io.IOException;

/**
 * <p>
 * Parsing the Http protocol related attributes, complete and the interaction of the network.
 * </p>
 * Created in Jul 28, 2015 7:33:22 PM.
 *
 * @author Yan Zhenjie.
 */
public class RestProtocol {

    private CacheStore<CacheEntity> mCacheStore;

    private HttpConnection mHttpConnection;

    public RestProtocol(CacheStore<CacheEntity> cache, NetworkExecutor executor) {
        mCacheStore = cache;
        mHttpConnection = new HttpConnection(executor);
    }

    public <T> Response<T> request(IProtocolRequest<T> request) {
        long startTime = SystemClock.elapsedRealtime();

        String cacheKey = request.getCacheKey();
        CacheMode cacheMode = request.getCacheMode();
        CacheEntity localCache = mCacheStore.get(cacheKey);

        Protocol protocol = requestCacheOrNetwork(cacheMode, localCache, request);
        analysisCacheAndProtocol(cacheKey, cacheMode, localCache, protocol);

        T result = null;
        if (protocol.exception == null) {
            try {
                result = request.parseResponse(protocol.headers, protocol.body);
            } catch (Exception e) {
                protocol.exception = e;
            }
        }
        return new RestResponse<>(request, protocol.fromCache, protocol.headers, result,
                SystemClock.elapsedRealtime() - startTime, protocol.exception);
    }

    private Protocol requestCacheOrNetwork(CacheMode cacheMode, CacheEntity localCache, IBasicRequest request) {
        Protocol protocol = null;
        switch (cacheMode) {
            case ONLY_READ_CACHE: {// Only read cache.
                protocol = new Protocol();
                if (localCache == null)
                    protocol.exception = new NotFoundCacheError("The cache mode is ONLY_READ_CACHE, but did not " +
                            "find the cache.");
                else {
                    protocol.headers = localCache.getResponseHeaders();
                    protocol.body = localCache.getData();
                    protocol.fromCache = true;
                }
                break;
            }
            case ONLY_REQUEST_NETWORK: { // Only request network.
                protocol = getHttpProtocol(request);
                break;
            }
            case NONE_CACHE_REQUEST_NETWORK: { // CacheStore none request network.
                if (localCache != null) {
                    protocol = new Protocol();
                    protocol.headers = localCache.getResponseHeaders();
                    protocol.body = localCache.getData();
                    protocol.fromCache = true;
                } else
                    protocol = getHttpProtocol(request);
                break;
            }
            case REQUEST_NETWORK_FAILED_READ_CACHE: { // Request network failed read cache.
                setRequestCacheHeader(request, localCache);
                protocol = getHttpProtocol(request);
                if (protocol.exception != null && localCache != null) {
                    protocol.headers = localCache.getResponseHeaders();
                    protocol.body = localCache.getData();
                    protocol.fromCache = true;

                    protocol.exception = null;
                }
                break;
            }
            case DEFAULT: { // Default, Comply with the RFC2616.
                if (localCache != null && localCache.getLocalExpire() > System.currentTimeMillis()) {
                    protocol = new Protocol();
                    protocol.headers = localCache.getResponseHeaders();
                    protocol.body = localCache.getData();
                    protocol.fromCache = true;
                } else {
                    setRequestCacheHeader(request, localCache);
                    protocol = getHttpProtocol(request);
                }
                break;
            }
        }
        return protocol;
    }

    /**
     * Perform the request before, Handle the cache headers.
     *
     * @param request     the request object.
     * @param cacheEntity cached entities.
     */
    private void setRequestCacheHeader(IBasicRequest request, CacheEntity cacheEntity) {
        if (cacheEntity == null) {
            request.headers().remove(Headers.HEAD_KEY_IF_NONE_MATCH);
            request.headers().remove(Headers.HEAD_KEY_IF_MODIFIED_SINCE);
        } else {
            Headers headers = cacheEntity.getResponseHeaders();
            String eTag = headers.getETag();
            if (eTag != null)
                request.headers().set(Headers.HEAD_KEY_IF_NONE_MATCH, eTag);

            long lastModified = headers.getLastModified();
            if (lastModified > 0)
                request.headers().set(Headers.HEAD_KEY_IF_MODIFIED_SINCE, HeaderUtil.formatMillisToGMT(lastModified));
        }
    }

    /**
     * Handle retries, and complete the request network here.
     *
     * @param request request object.
     * @return {@link Protocol}.
     */
    private Protocol getHttpProtocol(IBasicRequest request) {
        Protocol result = new Protocol();
        Connection connection = mHttpConnection.getConnection(request);
        result.headers = connection.responseHeaders();
        result.exception = connection.exception();
        if (result.exception == null && connection.serverStream() != null) {
            try {
                result.body = IOUtils.toByteArray(connection.serverStream());
            } catch (IOException e) {
                result.exception = e;
            }
        }
        IOUtils.closeQuietly(connection);
        return result;
    }

    /**
     * Process the response cache.
     *
     * @param cacheKey   cache key.
     * @param cacheMode  cache mode.
     * @param localCache cache key.
     * @param result     {@link Protocol}, Request the server to generate the response entity.
     * @return {@link RestProtocol}, According to the response headers and local server cache to regenerate the
     * response entity, you should use this response entity.
     */
    private void analysisCacheAndProtocol(String cacheKey, CacheMode cacheMode, CacheEntity localCache, Protocol
            result) {
        if (result.exception == null) {// Successfully.
            int responseCode = result.headers.getResponseCode();
            if (result.body == null)
                result.body = new byte[0];

            if (responseCode == 304) {
                if (localCache != null) { // Fix server error for 304.
                    result.fromCache = true;
                    result.headers = localCache.getResponseHeaders();
                    result.headers.set(Headers.HEAD_KEY_RESPONSE_CODE, "304");
                    result.body = localCache.getData();
                }
            } else {
                if (localCache == null) {
                    switch (cacheMode) {
                        case ONLY_READ_CACHE:// Only read cache.
                        case ONLY_REQUEST_NETWORK: {// Only request network.
                            break;
                        }
                        case NONE_CACHE_REQUEST_NETWORK:// CacheStore none request network.
                        case REQUEST_NETWORK_FAILED_READ_CACHE: {// Request network failed read cache.
                            long localExpire = HeaderUtil.getLocalExpires(result.headers);
                            localCache = new CacheEntity();
                            localCache.setResponseHeaders(result.headers);
                            localCache.setData(result.body);
                            localCache.setLocalExpire(localExpire);
                            mCacheStore.replace(cacheKey, localCache);
                            break;
                        }
                        case DEFAULT: {// Default, Comply with the RFC2616.
                            long localExpire = HeaderUtil.getLocalExpires(result.headers);
                            long lastModify = result.headers.getLastModified();
                            if (localExpire <= 0 && lastModify <= 0) return;
                            localCache = new CacheEntity();
                            localCache.setResponseHeaders(result.headers);
                            localCache.setData(result.body);
                            localCache.setLocalExpire(localExpire);
                            mCacheStore.replace(cacheKey, localCache);
                            break;
                        }
                    }

                } else if (!result.fromCache) {
                    long localExpire = HeaderUtil.getLocalExpires(result.headers);
                    localCache.setLocalExpire(localExpire);
                    localCache.getResponseHeaders().setAll(result.headers);
                    localCache.setData(result.body);
                    mCacheStore.replace(cacheKey, localCache);
                }
            }
        }
    }

    private static class Protocol {
        /**
         * Server response header.
         */
        private Headers headers;
        /**
         * Is the data from the cache.
         */
        private boolean fromCache;
        /**
         * Data.
         */
        private byte[] body;
        /**
         * Exception of connection.
         */
        private Exception exception;
    }
}