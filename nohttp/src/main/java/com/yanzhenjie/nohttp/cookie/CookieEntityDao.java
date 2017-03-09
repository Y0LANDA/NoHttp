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
package com.yanzhenjie.nohttp.cookie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yanzhenjie.nohttp.db.BaseDao;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Cookie database manager.</p>
 * Created in Dec 18, 2015 7:01:31 PM.
 *
 * @author Yan Zhenjie.
 */
public class CookieEntityDao extends BaseDao<CookieEntity> {

    public CookieEntityDao(Context context) {
        super(new CookieSQLHelper(context));
    }

    /**
     * Add or update by index(name, domain, path).
     *
     * @param cookie cookie entity.
     */
    @Override
    public long replace(CookieEntity cookie) {
        SQLiteDatabase database = getWriter();
        database.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(CookieSQLHelper.URI, cookie.getUri());
        values.put(CookieSQLHelper.NAME, cookie.getName());
        values.put(CookieSQLHelper.VALUE, cookie.getValue());
        values.put(CookieSQLHelper.COMMENT, cookie.getComment());
        values.put(CookieSQLHelper.COMMENT_URL, cookie.getCommentURL());
        values.put(CookieSQLHelper.DISCARD, String.valueOf(cookie.isDiscard()));
        values.put(CookieSQLHelper.DOMAIN, cookie.getDomain());
        values.put(CookieSQLHelper.EXPIRY, cookie.getExpiry());
        values.put(CookieSQLHelper.PATH, cookie.getPath());
        values.put(CookieSQLHelper.PORT_LIST, cookie.getPortList());
        values.put(CookieSQLHelper.SECURE, String.valueOf(cookie.isSecure()));
        values.put(CookieSQLHelper.VERSION, cookie.getVersion());
        try {
            long result = database.replace(CookieSQLHelper.TABLE_NAME, null, values);
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
    protected List<CookieEntity> getList(String querySql) {
        SQLiteDatabase database = getReader();
        List<CookieEntity> cookies = new ArrayList<>();
        Cursor cursor = database.rawQuery(querySql, null);
        while (!cursor.isClosed() && cursor.moveToNext()) {
            CookieEntity cookie = new CookieEntity();
            cookie.setId(cursor.getInt(cursor.getColumnIndex(CookieSQLHelper.ID)));
            cookie.setUri(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.URI)));
            cookie.setName(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.NAME)));
            cookie.setValue(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.VALUE)));
            cookie.setComment(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.COMMENT)));
            cookie.setCommentURL(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.COMMENT_URL)));
            cookie.setDiscard("true".equals(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.DISCARD))));
            cookie.setDomain(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.DOMAIN)));
            cookie.setExpiry(cursor.getLong(cursor.getColumnIndex(CookieSQLHelper.EXPIRY)));
            cookie.setPath(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.PATH)));
            cookie.setPortList(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.PORT_LIST)));
            cookie.setSecure("true".equals(cursor.getString(cursor.getColumnIndex(CookieSQLHelper.SECURE))));
            cookie.setVersion(cursor.getInt(cursor.getColumnIndex(CookieSQLHelper.VERSION)));
            cookies.add(cookie);
        }
        closeCursor(cursor);
        closeDateBase(database);
        return cookies;
    }

    @Override
    protected String getTableName() {
        return CookieSQLHelper.TABLE_NAME;
    }
}