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
package com.yanzhenjie.nohttp.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p> Measure the length of the flow.</p>
 * Created in Dec 17, 2015 2:57:46 PM.
 *
 * @author Yan Zhenjie.
 */
public class CounterOutputStream extends OutputStream {

    private final AtomicLong length = new AtomicLong(0L);

    public CounterOutputStream() {
    }

    public void write(long count) {
        if (count > 0)
            length.addAndGet(count);
    }

    public long get() {
        return length.get();
    }

    @Override
    public void write(int oneByte) throws IOException {
        length.addAndGet(oneByte);
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        length.addAndGet(buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        length.addAndGet(count);
    }

    /**
     * Didn't do anything here.
     *
     * @throws IOException nothing.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Didn't do anything here.
     *
     * @throws IOException nothing.
     */
    @Override
    public void flush() throws IOException {
    }
}
