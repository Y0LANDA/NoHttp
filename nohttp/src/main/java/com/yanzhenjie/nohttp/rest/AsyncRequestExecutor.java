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

import com.yanzhenjie.nohttp.Delivery;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.HandlerDelivery;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yan Zhenjie on 2017/2/15.
 */
public enum AsyncRequestExecutor {

    INSTANCE;

    /**
     * Delivery.
     */
    private Delivery mDelivery;
    /**
     * ExecutorService.
     */
    private ExecutorService mExecutorService;

    AsyncRequestExecutor() {
        mExecutorService = Executors.newCachedThreadPool();
        mDelivery = HandlerDelivery.newInstance();
    }

    public <T> void execute(int what, Request<T> request, OnResponseListener<T> responseListener) {
        request.onPreResponse(what, responseListener);
        mExecutorService.execute(new RequestTask<>(request, mDelivery));
    }

    private static class RequestTask<T> implements Runnable {

        private Request<T> request;
        private Delivery mDelivery;

        private RequestTask(Request<T> request, Delivery mDelivery) {
            this.request = request;
            this.mDelivery = mDelivery;
        }

        @Override
        public void run() {
            if (request.isCanceled()) {
                Logger.d(request.url() + " is canceled.");
                return;
            }

            final int what = request.what();
            final OnResponseListener<T> listener = request.responseListener();

            // start.
            request.start();
            Messenger.prepare(what, listener)
                    .start()
                    .post(mDelivery);

            // request.
            Response<T> response = SyncRequestExecutor.INSTANCE.execute(request);

            if (request.isCanceled())
                Logger.d(request.url() + " finish, but it's canceled.");
            else
                Messenger.prepare(what, listener)
                        .response(response)
                        .post(mDelivery);

            // finish.
            request.finish();
            Messenger.prepare(what, listener)
                    .finish()
                    .post(mDelivery);
        }
    }

}
