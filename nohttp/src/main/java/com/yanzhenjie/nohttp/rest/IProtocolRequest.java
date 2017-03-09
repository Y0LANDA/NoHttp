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
package com.yanzhenjie.nohttp.rest;

import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.IBasicRequest;

/**
 * <p>For the Request to encapsulate some Http protocol related properties.</p>
 * Created by Yan Zhenjie on 2016/8/20.
 */
public interface IProtocolRequest<T> extends IBasicRequest {

    /**
     * Set the request cache primary key, it should be globally unique.
     *
     * @param key unique key.
     * @return {@link IProtocolRequest}.
     */
    IProtocolRequest setCacheKey(String key);

    /**
     * Get key of cache data.
     *
     * @return cache key.
     */
    String getCacheKey();

    /**
     * Set the cache mode.
     *
     * @param cacheMode The value from {@link CacheMode}.
     * @return {@link IProtocolRequest}.
     */
    IProtocolRequest setCacheMode(CacheMode cacheMode);

    /**
     * He got the request cache mode.
     *
     * @return value from {@link CacheMode}.
     */
    CacheMode getCacheMode();

    /**
     * Parse request results for generic objects.
     *
     * @param responseHeaders response headers of server.
     * @param responseBody    response data of server.
     * @return your response result.
     * @throws Exception parse error.
     */
    T parseResponse(Headers responseHeaders, byte[] responseBody) throws Exception;

}
