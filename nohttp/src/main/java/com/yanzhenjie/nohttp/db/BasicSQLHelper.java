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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>General field class.</p>
 * Created in Jan 11, 2016 12:46:38 PM.
 *
 * @author Yan Zhenjie.
 */
public abstract class BasicSQLHelper extends SQLiteOpenHelper {

    public static final String ID = "_id";

    public static final String ALL = "*";

    public BasicSQLHelper(Context context, String dbName, SQLiteDatabase.CursorFactory cursorFactory, int dbVersion) {
        super(context, dbName, cursorFactory, dbVersion);
    }

}
