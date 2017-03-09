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

import android.util.Base64;

import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.HttpHeaders;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.db.BasicEntity;

import org.json.JSONException;

/**
 * <p>CacheStore entity class.</p>
 * Created in Jan 10, 2016 12:43:10 AM.
 *
 * @author Yan Zhenjie;
 */
public class CacheEntity implements BasicEntity {

    private long id;

    /**
     * The cache key.
     */
    private String key;
    /**
     * The server response headers.
     */
    private Headers responseHeaders = new HttpHeaders();

    /**
     * CacheStore data.
     */
    private byte[] data = {};

    /**
     * Cached in the local expiration time.
     */
    private long localExpire;

    public CacheEntity() {
    }

    /**
     * @param id              id.
     * @param key             key.
     * @param responseHeaders http response headers.
     * @param data            http response data.
     * @param localExpire     local expire time.
     */
    public CacheEntity(long id, String key, Headers responseHeaders, byte[] data, long localExpire) {
        this.id = id;
        this.key = key;
        this.responseHeaders = responseHeaders;
        this.data = data;
        this.localExpire = localExpire;
    }

    /**
     * @return the id.
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the responseHeaders.
     */
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * @param responseHeaders the responseHeaders to set.
     */
    public void setResponseHeaders(Headers responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Set the {@link Headers#setJSONString(String)} can Parse the json data, format conforms to the corresponding
     * Http header format.
     *
     * @param jsonString conform to the relevant head of the Json data format.
     */
    public void setResponseHeadersJson(String jsonString) {
        try {
            this.responseHeaders.setJSONString(jsonString);
        } catch (JSONException e) {
            Logger.e(e);
        }
    }

    /**
     * To getList the json data format of the head.
     *
     * @return json.
     */
    public String getResponseHeadersJson() {
        return this.responseHeaders.toJSONString();
    }

    /**
     * @return the data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set.
     */
    public void setDataBase64(String data) {
        this.data = Base64.decode(data, Base64.DEFAULT);
    }

    /**
     * @return the data.
     */
    public String getDataBase64() {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * @param data the data to set.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the localExpire.
     */
    public long getLocalExpire() {
        return localExpire;
    }

    /**
     * @param localExpire the localExpire to set.
     */
    public void setLocalExpire(long localExpire) {
        this.localExpire = localExpire;
    }

    /**
     * @return the localExpire.
     */
    public String getLocalExpireString() {
        return Long.toOctalString(localExpire);
    }

    /**
     * @param localExpire the localExpire to set.
     */
    public void setLocalExpireString(String localExpire) {
        this.localExpire = Long.parseLong(localExpire);
    }

}
