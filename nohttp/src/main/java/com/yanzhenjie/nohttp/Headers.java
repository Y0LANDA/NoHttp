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
package com.yanzhenjie.nohttp;

import com.yanzhenjie.nohttp.tools.MultiValueMap;

import org.json.JSONException;

import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Http header.
 * </p>
 * Created in Jan 10, 2016 2:29:42 PM.
 *
 * @author Yan Zhenjie.
 */
public interface Headers extends MultiValueMap<String, String> {

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_RESPONSE_CODE = "ResponseCode";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_ACCEPT = "Accept";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_ALL = "application/json,application/xml,application/xhtml+xml,text/html;q=0.9,image/webp,*/*;q=0.8";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_APPLICATION_JSON = "application/json";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_APPLICATION_XML = "application/xml";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_ACCEPT_ENCODING_GZIP_DEFLATE = "gzip, deflate";// no sdch

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_ACCEPT_LANGUAGE = "Accept-Language";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_ACCEPT_RANGE = "Accept-Range";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CONTENT_ENCODING = "Content-Encoding";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CONTENT_LENGTH = "Content-Length";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CONTENT_RANGE = "Content-Range";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CONTENT_TYPE = "Content-Type";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CACHE_CONTROL = "Cache-Control";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_CONNECTION = "Connection";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_CONNECTION_KEEP_ALIVE = "keep-alive";

    /**
     * The value is {@value}.
     */
    String HEAD_VALUE_CONNECTION_CLOSE = "close";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_DATE = "Date";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_EXPIRES = "Expires";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_E_TAG = "ETag";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_PRAGMA = "Pragma";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_IF_NONE_MATCH = "If-None-Match";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_LAST_MODIFIED = "Last-Modified";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_LOCATION = "Location";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_USER_AGENT = "User-Agent";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_COOKIE = "Cookie";

    /**
     * The value is {@value}.
     */
    String HEAD_KEY_SET_COOKIE = "Set-Cookie";

    /**
     * Copy all head to Headers.
     *
     * @param headers headers.
     */
    void addAll(Headers headers);

    /**
     * Remove all of the head now, add a new head in.
     *
     * @param headers headers.
     */
    void setAll(Headers headers);

    /**
     * Conform to the URI of the Cookie is added to the head.
     *
     * @param uri           url.
     * @param cookieHandler cookieHandler.
     */
    void addCookie(URI uri, CookieHandler cookieHandler);

    /**
     * From the json format String parsing out the {@code Map<String, List<String>>} data.
     *
     * @param jsonString json string.
     * @throws JSONException thrown it when format error.
     */
    void setJSONString(String jsonString) throws JSONException;

    /**
     * Into a json format string.
     *
     * @return Json format data.
     */
    String toJSONString();

    /**
     * Into a single key-value map.
     *
     * @return Map.
     */
    Map<String, String> toRequestHeaders();

    /**
     * To multiple key - the value of the map.
     *
     * @return Map format data.
     */
    Map<String, List<String>> toResponseHeaders();

    /**
     * All the cookies in header information.
     *
     * @return {@code List<HttpCookie>}.
     */
    List<HttpCookie> getCookies();

    /**
     * {@value #HEAD_KEY_CACHE_CONTROL}.
     *
     * @return CacheControl.
     */
    String getCacheControl();

    /**
     * {@value HEAD_KEY_CONTENT_DISPOSITION}.
     *
     * @return {@value HEAD_KEY_CONTENT_DISPOSITION}.
     */
    String getContentDisposition();

    /**
     * {@value #HEAD_KEY_CONTENT_ENCODING}.
     *
     * @return ContentEncoding.
     */
    String getContentEncoding();

    /**
     * {@value #HEAD_KEY_CONTENT_LENGTH}.
     *
     * @return ContentLength.
     */
    int getContentLength();

    /**
     * {@value #HEAD_KEY_CONTENT_TYPE}.
     *
     * @return ContentType.
     */
    String getContentType();

    /**
     * {@value #HEAD_KEY_CONTENT_RANGE} or {@value #HEAD_KEY_ACCEPT_RANGE}.
     *
     * @return {@value #HEAD_KEY_CONTENT_RANGE} or {@value #HEAD_KEY_ACCEPT_RANGE}.
     */
    String getContentRange();

    /**
     * {@value #HEAD_KEY_DATE}.
     *
     * @return Date.
     */
    long getDate();

    /**
     * {@value #HEAD_KEY_E_TAG}.
     *
     * @return ETag.
     */
    String getETag();

    /**
     * {@value #HEAD_KEY_EXPIRES}.
     *
     * @return Expiration.
     */
    long getExpiration();

    /**
     * {@value #HEAD_KEY_LAST_MODIFIED}.
     *
     * @return LastModified.
     */
    long getLastModified();

    /**
     * {@value #HEAD_KEY_LOCATION}.
     *
     * @return Location.
     */
    String getLocation();

    /**
     * {@value #HEAD_KEY_RESPONSE_CODE}.
     *
     * @return ResponseCode.
     */
    int getResponseCode();
}
