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

import java.util.List;

/**
 * Created in Dec 19, 2015 4:16:24 PM.
 *
 * @author Yan Zhenjie.
 */
public class Where {

    /**
     * Structure where the symbols.
     */
    public enum Options {

        IN("IN"), EQUAL("="), NO_EQUAL("!="), ThAN_LARGE(">"), THAN_SMALL("<");

        private String value;

        Options(String value) {
            this.value = value;
        }

        @Override
        public final String toString() {
            return this.value;
        }
    }

    private StringBuilder builder;

    public Where() {
        builder = new StringBuilder();
    }

    /**
     * @param columnName columnName.
     * @param op         such as: {@code >, =, <, IN}, but it's come from {@link Options}.
     * @param value      {@link Character}, {@link Integer}, {@link Long}, {@link Short}, {@link Double}, {@link Float}.
     */
    public Where(CharSequence columnName, Options op, Object value) {
        builder = new StringBuilder();
        add(columnName, op, value);
    }

    public final Where clear() {
        builder.delete(0, builder.length());
        return this;
    }

    public final Where append(Object row) {
        builder.append(row);
        return this;
    }

    public final Where set(String row) {
        clear().append(row);
        return this;
    }

    public final Where isNull(CharSequence columnName) {
        builder.append("\"").append(columnName).append("\" ").append("IS ").append("NULL");
        return this;
    }

    private Where addColumnName(CharSequence columnName, Options op) {
        builder.append("\"").append(columnName).append("\" ").append(op.toString()).append(' ');
        return this;
    }

    /**
     * @param columnName columnName.
     * @param op         such as: {@code >, =, <, IN}, but it's come from {@link Options}.
     * @param value      {@link Character}, {@link Integer}, {@link Long}, {@link Short}, {@link Double}, {@link Float}.
     * @return {@link Where}.
     */
    public final Where add(CharSequence columnName, Options op, Object value) {
        if (Options.EQUAL.equals(op) || Options.ThAN_LARGE.equals(op) || Options.THAN_SMALL.equals(op) || Options
                .NO_EQUAL.equals(op)) {
            addColumnName(columnName, op);
            if (isNumber(value))
                builder.append(value);
            else
                builder.append("'").append(value).append("'");
        } else if (Options.IN.equals(op) && value instanceof List<?>)
            addColumnName(columnName, op).append(value).in((List<?>) value);
        else
            throw new IllegalArgumentException("Value is not supported by the data type");
        return this;
    }

    private <T> Where in(List<T> values) {
        builder.append(Options.IN).append(" (");
        String sep = ", ";
        for (T value : values) {
            if (value instanceof CharSequence)
                builder.append("'").append(value).append("'");
            else if (value instanceof Integer || value instanceof Long || value instanceof Short)
                builder.append(value);
            builder.append(sep);
        }
        if (builder.lastIndexOf(sep) > 0)
            builder.delete(builder.length() - 2, builder.length());
        builder.append(")");
        return this;
    }

    private Where and() {
        if (builder.length() > 0)
            builder.append(" AND ");
        return this;
    }

    /**
     * @param columnName columnName.
     * @param op         such as: {@code >, =, <, IN}, but it's come from {@link Options}.
     * @param value      {@link Character}, {@link Integer}, {@link Long}, {@link Short}, {@link Double}, {@link Float}.
     * @return {@link Where}.
     */
    public final Where and(CharSequence columnName, Options op, Object value) {
        return and().add(columnName, op, value);
    }

    public final Where andNull(CharSequence columnName) {
        return and().isNull(columnName);
    }

    public final Where and(Where where) {
        return and().append(where);
    }

    private Where or() {
        if (builder.length() > 0)
            builder.append(" OR ");
        return this;
    }

    /**
     * @param columnName columnName.
     * @param op         such as: {@code >, =, <, IN}, but it's come from {@link Options}.
     * @param value      {@link Character}, {@link Integer}, {@link Long}, {@link Short}, {@link Double}, {@link Float}.
     * @return {@link Where}.
     */
    public final Where or(CharSequence columnName, Options op, Object value) {
        return or().add(columnName, op, value);
    }

    public final Where orNull(CharSequence columnName) {
        return or().isNull(columnName);
    }

    public final Where or(Where where) {
        return or().append(where);
    }

    public final Where bracket() {
        return insert(0, "(").append(')');
    }

    public final Where insert(int index, CharSequence s) {
        builder.insert(index, s);
        return this;
    }

    public final String get() {
        return builder.toString();
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public static boolean isNumber(Object value) {
        return value != null && (value instanceof Character || value instanceof Integer || value instanceof Long ||
                value instanceof Short || value instanceof Double || value instanceof Float);
    }
}
