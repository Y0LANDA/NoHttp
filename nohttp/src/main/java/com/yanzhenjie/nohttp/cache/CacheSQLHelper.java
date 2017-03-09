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
import android.database.sqlite.SQLiteDatabase;

import com.yanzhenjie.nohttp.db.BasicSQLHelper;

/**
 * <p>CacheStore database operation class.</p>
 * Created in Jan 10, 2016 12:39:15 AM.
 *
 * @author Yan Zhenjie;
 */
class CacheSQLHelper extends BasicSQLHelper {

    private static final String DB_CACHE_NAME = "_nohttp_cache_db.db";
    private static final int DB_CACHE_VERSION = 3;
    static final String TABLE_NAME = "cache_table";
    static final String KEY = "key";
    static final String HEAD = "head";
    static final String DATA = "data";
    static final String LOCAL_EXPIRES = "local_expires";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE cache_table(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "key TEXT, head TEXT, data text, local_expires text)";
    private static final String SQL_CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX cache_unique_index ON cache_table" +
            "(\"key\")";
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS cache_table";

    public CacheSQLHelper(Context context) {
        super(context, DB_CACHE_NAME, null, DB_CACHE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_TABLE);
            db.execSQL(SQL_CREATE_UNIQUE_INDEX);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != oldVersion) {
            db.beginTransaction();
            try {
                db.execSQL(SQL_DELETE_TABLE);
                db.execSQL(SQL_CREATE_TABLE);
                db.execSQL(SQL_CREATE_UNIQUE_INDEX);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
