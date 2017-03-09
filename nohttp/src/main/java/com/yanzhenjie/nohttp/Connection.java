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

import com.yanzhenjie.nohttp.tools.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created in May 3, 2016 11:05:03 PM.
 *
 * @author Yan Zhenjie.
 */
public class Connection implements Closeable {

    /**
     * NetworkExecutor
     */
    private Network network;
    /**
     * Server response header.
     */
    private Headers mResponseHeaders;
    /**
     * Server data stream, may be the error or input.
     */
    private InputStream mServerStream;
    /**
     * Exception of network.
     */
    private Exception mException;

    /**
     * Create a response.
     *
     * @param network         {@link Network}.
     * @param responseHeaders response headers.
     * @param serverStream    According to the response code, the incoming data stream server.
     * @param exception       network exceptions that occur in the process.
     */
    public Connection(Network network, Headers responseHeaders, InputStream serverStream, Exception exception) {
        this.network = network;
        this.mResponseHeaders = responseHeaders;
        this.mServerStream = serverStream;
        this.mException = exception;
    }

    /**
     * Get response headers.
     *
     * @return the responseHeaders.
     */
    public Headers responseHeaders() {
        return mResponseHeaders;
    }

    /**
     * Get stream from server.
     *
     * @return the inputStream.
     */
    public InputStream serverStream() {
        return mServerStream;
    }

    /**
     * Get exception for execution.
     *
     * @return the exception.
     */
    public Exception exception() {
        return mException;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(mServerStream);
        IOUtils.closeQuietly(network);
    }

}
