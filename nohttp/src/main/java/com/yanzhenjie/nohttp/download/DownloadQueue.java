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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Download queue.
 * </p>
 * Created in Oct 21, 2015 2:44:19 PM.
 *
 * @author Yan Zhenjie.
 *
 * modify zhangnn on  2018-1-31-21:55
 */
public class DownloadQueue implements IDownloadRequestListener {

    private AtomicInteger mInteger = new AtomicInteger();
    private final BlockingQueue<DownloadRequest> mRequestQueue = new PriorityBlockingQueue<>();
    private final Map<DownloadRequest, Messenger> mMessengerMap = new LinkedHashMap<>();
    private final Map<Integer, DownloadRequest> mRequestMap = new HashMap<>();

    private DownloadDispatcher[] mDispatchers;

    /**
     * Create download queue manager.
     *
     * @param threadPoolSize number of thread pool.
     */
    public DownloadQueue(int threadPoolSize) {
        mDispatchers = new DownloadDispatcher[threadPoolSize];
    }

    /**
     * Start polling the download queue, a one of the implementation of the download task, if you have started to
     * poll the download queue, then it will stop all the threads, to re create thread
     * execution.
     */
    public void start() {
        stop();
        for (int i = 0; i < mDispatchers.length; i++) {
            DownloadDispatcher networkDispatcher = new DownloadDispatcher(mRequestQueue, mMessengerMap, this);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    /**
     * Add a download task to download queue, waiting for execution, if there is no task in the queue or the number
     * of tasks is less than the number of thread pool, will be executed immediately.
     *
     * @param what             used to distinguish Download.
     * @param request          download handle object.
     * @param downloadListener download results monitor.
     */
    public void add(int what, DownloadRequest request, DownloadListener downloadListener) {
        request.setSequence(mInteger.incrementAndGet());
        mMessengerMap.put(request, Messenger.newInstance(what, downloadListener));
        mRequestQueue.add(request);
        mRequestMap.put(what, request);
    }

    /**
     * Don't start return handle queue size.
     *
     * @return size.
     */
    public int size() {
        return mRequestQueue.size();
    }

    /**
     * Polling the queue will not be executed, and this will not be canceled.
     */
    public void stop() {
        for (DownloadDispatcher dispatcher : mDispatchers) {
            if (dispatcher != null)
                dispatcher.quit();
        }
    }

    /**
     * All requests for the sign specified in the queue, if you are executing, will interrupt the download task.
     *
     * @param sign this sign will be the same as sign's DownloadRequest, and if it is the same, then cancel the task.
     */
    public void cancelBySign(Object sign) {
        if (mRequestMap.isEmpty()) return;
        //已经开始执行的请求，mRequestQueue会移除该请求，所以只能遍历缓存的请求集合
        synchronized (mRequestMap) {
            Iterator<Map.Entry<Integer, DownloadRequest>> iterator = mRequestMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, DownloadRequest> entry = iterator.next();
                DownloadRequest request = entry.getValue();
                Object tag = request.getTag();
                //从缓存集合中找到需要取消的请求，如果请求已经执行，则取消；
                //如果还没有开始执行，则从执行队列中移除，并从缓存中移除
                if (tag == sign || (sign != null && tag != null && tag.equals(sign))) {
                    if (request.isStarted()) {
                        request.cancel();
                    } else {
                        mRequestQueue.remove(request);
                        mRequestMap.remove(entry.getKey());
                    }
                }
            }
        }
    }

    /**
     * Cancel all requests, Already in the execution of the handle can't use this method.
     */
    public synchronized void cancelAll() {
        //没有分发出去的请求，全部移除
        if (mRequestQueue.size() > 0) mRequestQueue.clear();
        //已经分发出去的请求需要取消
        if (mRequestMap.size() > 0) {
            for (DownloadRequest request : mRequestMap.values()) {
                if (request.isStarted()) {
                    request.cancel();
                }
            }
            //剩余的是正在执行的请求，也需要从该列表中移除
            mRequestMap.clear();
        }

    }

    @Override
    public void onStart(int what) {

    }

    @Override
    public void onFinish(int what) {
        //从备份队列中移除
        mRequestMap.remove(what);
    }

    @Override
    public void onError(int what) {
        //从备份队列中移除
        mRequestMap.remove(what);
    }
}
