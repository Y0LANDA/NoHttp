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
package com.yanzhenjie.nohttp.db;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.List;

/**
 * <p>Database management generic class, has realized the basic functions, inheritance of the subclass only need to
 * implement {@link #replace(BasicEntity)}, {@link #getList(String)} and
 * {@link #getTableName()}.</p>
 * Created in Jan 10, 2016 8:18:28 PM.
 *
 * @author Yan Zhenjie.
 */
public abstract class BaseDao<T extends BasicEntity> {

    /**
     * A helper class to manage database creation and version management.
     */
    private SQLiteOpenHelper liteOpenHelper;

    public BaseDao(SQLiteOpenHelper disk) {
        this.liteOpenHelper = disk;
    }

    /**
     * Open the database when the read data.
     *
     * @return {@link SQLiteDatabase}.
     */
    protected final SQLiteDatabase getReader() {
        return liteOpenHelper.getReadableDatabase();
    }

    /**
     * Open the database when the write data.
     *
     * @return {@link SQLiteDatabase}.
     */
    protected final SQLiteDatabase getWriter() {
        return liteOpenHelper.getWritableDatabase();
    }

    /**
     * Close the database when reading data.
     *
     * @param database {@link SQLiteDatabase}.
     */
    protected final void closeDateBase(SQLiteDatabase database) {
        if (database != null && database.isOpen())
            database.close();
    }

    /**
     * Close the database when writing data.
     *
     * @param cursor {@link Cursor}.
     */
    protected final void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * The query id number.
     *
     * @return int format.
     */
    public final int count() {
        return countColumn(BasicSQLHelper.ID);
    }

    /**
     * According to the "column" query "column" number.
     *
     * @param columnName ColumnName.
     * @return column count.
     */
    public final int countColumn(String columnName) {
        return count("SELECT COUNT(" + columnName + ") FROM " + getTableName());
    }

    /**
     * According to the "column" query number.
     *
     * @param sql sql.
     * @return count
     */
    public final int count(String sql) {
        SQLiteDatabase database = getReader();
        Cursor cursor = database.rawQuery(sql, null);
        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        closeDateBase(database);
        return count;
    }

    /**
     * Delete all data.
     *
     * @return a boolean value, whether deleted successfully.
     */
    public final boolean deleteAll() {
        return delete("1=1");
    }

    /**
     * Must have the id.
     *
     * @param ts delete the queue list.
     * @return a boolean value, whether deleted successfully.
     */
    public final boolean delete(List<T> ts) {
        StringBuilder where = new StringBuilder(BasicSQLHelper.ID).append(" IN(");
        for (T t : ts) {
            long id = t.getId();
            if (id > 0) {
                where.append(',');
                where.append(id);
            }
        }
        where.append(')');
        if (',' == where.charAt(6))
            where.deleteCharAt(6);
        return delete(where.toString());
    }

    /**
     * According to the where to delete data.
     *
     * @param where performs conditional.
     * @return a boolean value, whether deleted successfully.
     */
    public final boolean delete(String where) {
        SQLiteDatabase database = getWriter();
        String sql = "DELETE FROM " + getTableName() + " WHERE " + where;
        database.beginTransaction();
        try {
            database.execSQL(sql);
            database.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            database.endTransaction();
            closeDateBase(database);
        }
    }

    /**
     * Query all data.
     *
     * @return list data.
     */
    public final List<T> getAll() {
        return getList(null, null, null, null);
    }

    /**
     * All the data query a column.
     *
     * @param where   such as: {@code age > 20}.
     * @param orderBy such as: {@code "age"}.
     * @param limit   such as. {@code '20'}.
     * @param offset  offset.
     * @return list data.
     */
    public final List<T> getList(String where, String orderBy, String limit, String offset) {
        StringBuilder sqlBuild = new StringBuilder("SELECT ").append(BasicSQLHelper.ALL).append(" FROM ").append
                (getTableName());
        if (!TextUtils.isEmpty(where)) {
            sqlBuild.append(" WHERE ");
            sqlBuild.append(where);
        }
        if (!TextUtils.isEmpty(orderBy)) {
            sqlBuild.append(" ORDER BY ");
            sqlBuild.append(orderBy);
        }
        if (!TextUtils.isEmpty(limit)) {
            sqlBuild.append(" LIMIT ");
            sqlBuild.append(limit);
        }
        if (!TextUtils.isEmpty(limit) && !TextUtils.isEmpty(offset)) {
            sqlBuild.append(" OFFSET ");
            sqlBuild.append(offset);
        }
        return getList(sqlBuild.toString());
    }

    /**
     * According to the SQL query data list.
     *
     * @param querySql sql.
     * @return list data.
     */
    protected abstract List<T> getList(String querySql);

    /**
     * According to the unique index adds or updates a row data.
     *
     * @param t {@link T}.
     * @return long.
     */
    public abstract long replace(T t);

    /**
     * Table name should be.
     *
     * @return table name.
     */
    protected abstract String getTableName();

}
