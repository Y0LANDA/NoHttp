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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Yan Zhenjie on 2016/10/15.
 */
public interface Network extends Closeable {

    /**
     * Gets output stream for socket.
     *
     * @return {@link OutputStream}.
     * @throws IOException maybe.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Gets response code for server.
     *
     * @return int value.
     * @throws IOException maybe.
     */
    int getResponseCode() throws IOException;

    /**
     * Gets response headers for server.
     *
     * @return {@code Map<String, List<String>>}.
     */
    Map<String, List<String>> getResponseHeaders();

    /**
     * Gets input stream for socket.
     *
     * @param responseCode response code for server.
     * @param headers      response headers for server.
     * @return {@link InputStream}.
     * @throws IOException maybe.
     */
    InputStream getServerStream(int responseCode, Headers headers) throws IOException;

}
