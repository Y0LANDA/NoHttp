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
package com.yanzhenjie.nohttp.download;

import android.os.Process;

import com.yanzhenjie.nohttp.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * <p>
 * Download queue polling thread.
 * </p>
 * Created in Oct 21, 2015 2:46:23 PM.
 *
 * @author Yan Zhenjie.
 *
 * modify zhangnn on  2018-1-31-21:55
 */
class DownloadDispatcher extends Thread {

    private final BlockingQueue<DownloadRequest> mRequestQueue;
    private final Map<DownloadRequest, Messenger> mMessengerMap;
    private final IDownloadRequestListener downloadRequestListener;

    private boolean mQuit = false;

    public DownloadDispatcher(BlockingQueue<DownloadRequest> requestQueue, Map<DownloadRequest, Messenger> messengerMap,IDownloadRequestListener downloadRequestListener) {
        this.mRequestQueue = requestQueue;
        this.mMessengerMap = messengerMap;
        this.downloadRequestListener = downloadRequestListener;
    }

    /**
     * Quit this thread.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (!mQuit) {
            final DownloadRequest request;
            try {
                request = mRequestQueue.take();
            } catch (InterruptedException e) {
                if (mQuit)
                    return;
                continue;
            }

            if (request.isCanceled()) {
                mRequestQueue.remove(request);
                mMessengerMap.remove(request);
                Logger.d(request.url() + " is canceled.");
                continue;
            }

            request.start();
            SyncDownloadExecutor.INSTANCE.execute(0, request, new ListenerDelegate(request, mMessengerMap),downloadRequestListener);
            request.finish();

            // remove it from queue.
            mRequestQueue.remove(request);
            mMessengerMap.remove(request);
        }
    }
}
