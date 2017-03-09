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
package com.yanzhenjie.nohttp.cache;

import android.content.Context;

import com.yanzhenjie.nohttp.db.Where;
import com.yanzhenjie.nohttp.tools.CacheStore;
import com.yanzhenjie.nohttp.db.BaseDao;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Http cache interface implementation.</p>
 * Created in Jan 10, 2016 12:45:34 AM.
 *
 * @author Yan Zhenjie;
 */
public class DBCacheStore implements CacheStore<CacheEntity> {

    /**
     * Database sync lock.
     */
    private Lock mLock;
    /**
     * Database manager.
     */
    private BaseDao<CacheEntity> mManager;

    private boolean mEnable = true;

    public DBCacheStore(Context context) {
        mLock = new ReentrantLock();
        mManager = new CacheEntityDao(context);
    }

    public CacheStore<CacheEntity> setEnable(boolean enable) {
        this.mEnable = enable;
        return this;
    }

    @Override
    public CacheEntity get(String key) {
        mLock.lock();
        try {
            if (!mEnable) return null;
            Where where = new Where(CacheSQLHelper.KEY, Where.Options.EQUAL, key);
            List<CacheEntity> cacheEntities = mManager.getList(where.get(), null, null, null);
            return cacheEntities.size() > 0 ? cacheEntities.get(0) : null;
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public CacheEntity replace(String key, CacheEntity cacheEntity) {
        mLock.lock();
        try {
            if (!mEnable) return cacheEntity;
            cacheEntity.setKey(key);
            mManager.replace(cacheEntity);
            return cacheEntity;
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public boolean remove(String key) {
        mLock.lock();
        try {
            if (key == null || !mEnable)
                return false;
            Where where = new Where(CacheSQLHelper.KEY, Where.Options.EQUAL, key);
            return mManager.delete(where.toString());
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public boolean clear() {
        mLock.lock();
        try {
            if (!mEnable) return false;
            return mManager.deleteAll();
        } finally {
            mLock.unlock();
        }
    }

}
