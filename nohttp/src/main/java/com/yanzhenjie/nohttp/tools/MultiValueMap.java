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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Can save multiple the value of the map.</p>
 * Created in Jan 10, 2016 5:00:07 PM.
 *
 * @author Yan Zhenjie.
 */
public interface MultiValueMap<K, V> {

    /**
     * Add a value for a key.
     *
     * @param key   key.
     * @param value value.
     */
    void add(K key, V value);

    /**
     * Add more value to a key.
     *
     * @param key    key.
     * @param values values.
     */
    void add(K key, List<V> values);

    /**
     * Set the value for a key, if the key has the value, delete all of the old value, then the new value added.
     *
     * @param key   key.
     * @param value values.
     */
    void set(K key, V value);

    /**
     * @param key    key.
     * @param values values.
     * @see #set(Object, Object)
     */
    void set(K key, List<V> values);

    /**
     * The removal of all key/value pair, add new keys to enter.
     *
     * @param values values.
     */
    void set(Map<K, List<V>> values);

    /**
     * Delete a key-value.
     *
     * @param key key.
     * @return value.
     */
    List<V> remove(K key);

    /**
     * Remove all key-value.
     */
    void clear();

    /**
     * Get the key set.
     *
     * @return Set.
     */
    Set<K> keySet();

    /**
     * To getList all key of all values.
     *
     * @return List.
     */
    List<V> values();

    /**
     * EntrySet.
     *
     * @return {@link Set}.
     */
    Set<Map.Entry<K, List<V>>> entrySet();

    /**
     * To getList the key of the at index value.
     *
     * @param key   key.
     * @param index index value.
     * @return The value.
     */
    V getValue(K key, int index);

    /**
     * To getList key of all values.
     *
     * @param key key.
     * @return values.
     */
    List<V> getValues(K key);

    /**
     * The size of the map.
     *
     * @return size.
     */
    int size();

    /**
     * If the map has no value.
     *
     * @return True: empty, false: not empty.
     */
    boolean isEmpty();

    /**
     * Whether the map with a key.
     *
     * @param key key.
     * @return True: contain, false: none.
     */
    boolean containsKey(K key);

}
