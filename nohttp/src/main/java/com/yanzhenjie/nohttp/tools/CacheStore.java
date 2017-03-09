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

/**
 * <p>CacheStore interface.</p>
 * Created in Dec 14, 2015 5:52:41 PM.
 *
 * @author Yan Zhenjie;
 */
public interface CacheStore<T> {

    /**
     * According to the key to getList the cache data.
     *
     * @param key unique key.
     * @return cache data.
     */
    T get(String key);

    /**
     * According to the key to replace or save the data.
     *
     * @param key  unique key.
     * @param data cache data.
     * @return cache data.
     */
    T replace(String key, T data);

    /**
     * According to the key to remove the data.
     *
     * @param key unique.
     * @return cache data.
     */
    boolean remove(String key);

    /**
     * Clear all data.
     *
     * @return return to true to clear the failure when the false is cleared.
     */
    boolean clear();
}
