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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.tools.Encryption;
import com.yanzhenjie.nohttp.db.BaseDao;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>CacheStore database manager.</p>
 * Created in Jan 10, 2016 12:42:29 AM.
 *
 * @author Yan Zhenjie;
 */
public class CacheEntityDao extends BaseDao<CacheEntity> {

    /**
     *
     */
    private Encryption mEncryption;
    /**
     * Encryption key.
     */
    private String encryptionKey = DBCacheStore.class.getSimpleName();

    public CacheEntityDao(Context context) {
        super(new CacheSQLHelper(context));
        mEncryption = new Encryption(encryptionKey);
    }

    @Override
    public long replace(CacheEntity cacheEntity) {
        SQLiteDatabase database = getWriter();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(CacheSQLHelper.KEY, cacheEntity.getKey());
            values.put(CacheSQLHelper.HEAD, encrypt(cacheEntity.getResponseHeadersJson()));
            values.put(CacheSQLHelper.DATA, encrypt(Base64.encodeToString(cacheEntity.getData(), Base64.DEFAULT)));
            values.put(CacheSQLHelper.LOCAL_EXPIRES, encrypt(Long.toString(cacheEntity.getLocalExpire())));
            long result = database.replace(getTableName(), null, values);
            database.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            return -1;
        } finally {
            database.endTransaction();
            closeDateBase(database);
        }
    }

    @Override
    protected List<CacheEntity> getList(String querySql) {
        SQLiteDatabase database = getReader();
        List<CacheEntity> cacheEntities = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(querySql, null);
            while (!cursor.isClosed() && cursor.moveToNext()) {
                CacheEntity cacheEntity = new CacheEntity();
                cacheEntity.setId(cursor.getInt(cursor.getColumnIndex(CacheSQLHelper.ID)));
                cacheEntity.setKey(cursor.getString(cursor.getColumnIndex(CacheSQLHelper.KEY)));
                cacheEntity.setResponseHeadersJson(decrypt(cursor.getString(cursor.getColumnIndex(CacheSQLHelper
                        .HEAD))));
                cacheEntity.setData(Base64.decode(decrypt(cursor.getString(cursor.getColumnIndex(CacheSQLHelper.DATA)
                )), Base64.DEFAULT));
                cacheEntity.setLocalExpire(Long.parseLong(decrypt(cursor.getString(cursor.getColumnIndex
                        (CacheSQLHelper.LOCAL_EXPIRES)))));
                cacheEntities.add(cacheEntity);
            }
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            closeCursor(cursor);
            closeDateBase(database);
        }
        return cacheEntities;
    }

    @Override
    protected String getTableName() {
        return CacheSQLHelper.TABLE_NAME;
    }

    private String encrypt(String encryptionText) throws Exception {
        return mEncryption.encrypt(encryptionText);
    }

    private String decrypt(String cipherText) throws Exception {
        return mEncryption.decrypt(cipherText);
    }
}
