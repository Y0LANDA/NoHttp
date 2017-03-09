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
package com.yanzhenjie.nohttp.download;

import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.Delivery;

/**
 * Created by Yan Zhenjie on 2017/2/18.
 */
public class Messenger {

    private final int what;
    private final DownloadListener listener;

    // command
    private int command;

    // start
    private Headers headers;
    private long allCount;
    private boolean isResume;
    private long beforeLength;

    // progress
    private int progress;
    private long fileCount;
    private long speed;

    // error
    private Exception exception;

    // finish
    private String filePath;

    private Messenger(int what, DownloadListener listener) {
        this.what = what;
        this.listener = listener;
    }

    static Messenger prepare(int what, DownloadListener listener) {
        return new Messenger(what, listener);
    }

    Messenger onStart(boolean isResume, long beforeLength, Headers headers, long allCount) {
        this.command = -1;
        this.isResume = isResume;
        this.beforeLength = beforeLength;
        this.headers = headers;
        this.allCount = allCount;
        return this;
    }

    Messenger onProgress(int progress, long fileCount, long speed) {
        this.command = -2;
        this.progress = progress;
        this.fileCount = fileCount;
        this.speed = speed;
        return this;
    }

    Messenger onError(Exception exception) {
        this.command = -3;
        this.exception = exception;
        return this;
    }

    Messenger onCancel() {
        this.command = -4;
        return this;
    }

    Messenger onFinish(String filePath) {
        this.command = -5;
        this.filePath = filePath;
        return this;
    }

    void post(Delivery delivery) {
        if (listener == null) return;
        delivery.post(new Runnable() {
            @Override
            public void run() {
                switch (command) {
                    case -1:
                        listener.onStart(what, isResume, beforeLength, headers, allCount);
                        break;
                    case -2:
                        listener.onProgress(what, progress, fileCount, speed);
                        break;
                    case -3:
                        listener.onDownloadError(what, exception);
                        break;
                    case -4:
                        listener.onCancel(what);
                        break;
                    case -5:
                        listener.onFinish(what, filePath);
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
