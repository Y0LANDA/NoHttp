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
import com.yanzhenjie.nohttp.RequestMethod;

/**
 * Created by Yan Zhenjie on 2016/8/20.
 */
public class ByteArrayRequest extends RestRequest<byte[]> {

    public ByteArrayRequest(String url) {
        this(url, RequestMethod.GET);
    }

    public ByteArrayRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
    }

    @Override
    public byte[] parseResponse(Headers responseHeaders, byte[] responseBody) throws Exception {
        return responseBody == null ? new byte[0] : responseBody;
    }
}
