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

import com.yanzhenjie.nohttp.Delivery;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * <p>
 * Download queue polling thread.
 * </p>
 * Created in Oct 21, 2015 2:46:23 PM.
 *
 * @author Yan Zhenjie.
 */
class DownloadDispatcher extends Thread {

    /**
     * Un finish task queue.
     */
    private final BlockingQueue<DownloadRequest> mUnFinishQueue;
    /**
     * Download task queue.
     */
    private final BlockingQueue<DownloadRequest> mDownloadQueue;
    /**
     * Delivery.
     */
    private Delivery mDelivery;
    /**
     * Are you out of this thread.
     */
    private boolean mQuit = false;

    /**
     * Create a thread that executes the download queue.
     *
     * @param unFinishQueue un finish queue.
     * @param downloadQueue download queue to be polled.
     */
    public DownloadDispatcher(BlockingQueue<DownloadRequest> unFinishQueue, BlockingQueue<DownloadRequest>
            downloadQueue, Delivery delivery) {
        this.mUnFinishQueue = unFinishQueue;
        this.mDownloadQueue = downloadQueue;
        this.mDelivery = delivery;
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
                request = mDownloadQueue.take();
            } catch (InterruptedException e) {
                if (mQuit)
                    return;
                continue;
            }

            if (request.isCanceled()) {
                Logger.d(request.url() + " is canceled.");
                continue;
            }

            request.start();
            SyncDownloadExecutor.INSTANCE.execute(request.what(), request, new DownloadListener() {

                @Override
                public void onStart(int what, boolean isResume, long beforeLength, Headers headers, long allCount) {
                    Messenger.prepare(what, request.downloadListener())
                            .onStart(isResume, beforeLength, headers, allCount)
                            .post(mDelivery);
                }

                @Override
                public void onDownloadError(int what, Exception exception) {
                    Messenger.prepare(what, request.downloadListener())
                            .onError(exception)
                            .post(mDelivery);
                }

                @Override
                public void onProgress(int what, int progress, long fileCount, long speed) {
                    Messenger.prepare(what, request.downloadListener())
                            .onProgress(progress, fileCount, speed)
                            .post(mDelivery);
                }

                @Override
                public void onFinish(int what, String filePath) {
                    Messenger.prepare(what, request.downloadListener())
                            .onFinish(filePath)
                            .post(mDelivery);
                }

                @Override
                public void onCancel(int what) {
                    Messenger.prepare(what, request.downloadListener())
                            .onCancel()
                            .post(mDelivery);
                }
            });
            request.finish();
            mUnFinishQueue.remove(request);
        }
    }
}
