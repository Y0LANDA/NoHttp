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
package com.yanzhenjie.nohttp.tools;

import android.text.TextUtils;

import com.yanzhenjie.nohttp.Headers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Created on 2016/6/21.
 *
 * @author Yan Zhenjie.
 */
public class HeaderUtil {

    /**
     * Accept-Language.
     */
    private static String acceptLanguageInstance;

    /**
     * Format of http head.
     */
    public static final String FORMAT_HTTP_DATA = "EEE, dd MMM y HH:mm:ss 'GMT'";

    /**
     * Commmon TimeZone for GMT.
     */
    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * Parsing the TimeZone of time in milliseconds.
     *
     * @param gmtTime GRM Time, Format such as: {@value #FORMAT_HTTP_DATA}.
     * @return The number of milliseconds from 1970.1.1.
     * @throws ParseException if an error occurs during parsing.
     */
    public static long parseGMTToMillis(String gmtTime) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_HTTP_DATA, Locale.US);
        formatter.setTimeZone(GMT_TIME_ZONE);
        Date date = formatter.parse(gmtTime);
        return date.getTime();
    }

    /**
     * Parsing the TimeZone of time from milliseconds.
     *
     * @param milliseconds the number of milliseconds from 1970.1.1.
     * @return GRM Time, Format such as: {@value #FORMAT_HTTP_DATA}.
     */
    public static String formatMillisToGMT(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_HTTP_DATA, Locale.US);
        simpleDateFormat.setTimeZone(GMT_TIME_ZONE);
        return simpleDateFormat.format(date);
    }

    /**
     * Returned the local number of milliseconds after 100.
     *
     * @return Long format time.
     */
    public static long getMaxExpiryMillis() {
        return System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 365L * 100L;
    }

    /**
     * Create acceptLanguage.
     *
     * @return Returns the client can accept the language types. Such as:zh-CN,zh.
     */
    public static String systemAcceptLanguage() {
        if (TextUtils.isEmpty(acceptLanguageInstance)) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage();
            String country = locale.getCountry();
            StringBuilder acceptLanguageBuilder = new StringBuilder(language);
            if (!TextUtils.isEmpty(country))
                acceptLanguageBuilder.append('-').append(country).append(',').append(language);
            acceptLanguageInstance = acceptLanguageBuilder.toString();
        }
        return acceptLanguageInstance;
    }

    /**
     * A value of the header information.
     *
     * @param content      like {@code text/html;charset=utf-8}.
     * @param key          like {@code charset}.
     * @param defaultValue list {@code utf-8}.
     * @return If you have a value key, you will return the parsed value if you don't return the default value.
     */
    public static String parseHeadValue(String content, String key, String defaultValue) {
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(key)) {
            StringTokenizer stringTokenizer = new StringTokenizer(content, ";");
            while (stringTokenizer.hasMoreElements()) {
                String valuePair = stringTokenizer.nextToken();
                int index = valuePair.indexOf('=');
                if (index > 0) {
                    String name = valuePair.substring(0, index).trim();
                    if (key.equalsIgnoreCase(name)) {
                        defaultValue = valuePair.substring(index + 1).trim();
                        break;
                    }
                }
            }
        }
        return defaultValue;
    }

    /**
     * Whether the content has been compressed.
     *
     * @param contentEncoding read the data from the server's head.
     * @return True: yes, false: no inclusion.
     */
    public static boolean isGzipContent(String contentEncoding) {
        return contentEncoding != null && contentEncoding.contains("gzip");
    }

    /**
     * Analysis the response of the cache is valid time.
     *
     * @param responseHeaders http response header.
     * @return Time corresponding milliseconds.
     */
    public static long getLocalExpires(Headers responseHeaders) {
        long now = System.currentTimeMillis();
        long date = responseHeaders.getDate();
        long expires = responseHeaders.getExpiration();

        long maxAge = 0;
        long staleWhileRevalidate = 0;

        String cacheControl = responseHeaders.getCacheControl();
        if (!TextUtils.isEmpty(cacheControl)) {
            StringTokenizer tokens = new StringTokenizer(cacheControl, ",");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim().toLowerCase(Locale.getDefault());
                if ((token.equals("no-cache") || token.equals("no-store"))) {
                    return 0;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.startsWith("stale-while-revalidate=")) {
                    try {
                        staleWhileRevalidate = Long.parseLong(token.substring(23));
                    } catch (Exception e) {
                    }
                }
            }
        }

        long localExpire = 0;// Local expires time of cache.

        // If must-revalidate, It must be from the server to validate expired.
        // Have CacheControl.
        if (!TextUtils.isEmpty(cacheControl)) {
            localExpire = now + maxAge * 1000;
            if (staleWhileRevalidate > 0)
                localExpire += staleWhileRevalidate * 1000;
        }

        // If the server through control the cache Expires.
        if ((localExpire == 0 || localExpire == now) && date > 0 && expires >= date) {
            localExpire = now + (expires - date);
        }

        return localExpire;
    }

}
