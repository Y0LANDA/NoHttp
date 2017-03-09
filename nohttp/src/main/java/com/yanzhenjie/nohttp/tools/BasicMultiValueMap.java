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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2016/6/23.
 *
 * @author Yan Zhenjie.
 */
public class BasicMultiValueMap<K, V> implements MultiValueMap<K, V> {

    private Map<K, List<V>> mSource;

    public BasicMultiValueMap(Map<K, List<V>> source) {
        mSource = source;
    }

    @Override
    public void add(K key, V value) {
        if (key != null) {
            if (!containsKey(key))
                mSource.put(key, new ArrayList<V>(1));
            mSource.get(key).add(value);
        }
    }

    @Override
    public void add(K key, List<V> values) {
        for (V value : values) {
            add(key, value);
        }
    }

    @Override
    public void set(K key, V value) {
        remove(key);
        add(key, value);
    }

    @Override
    public void set(K key, List<V> values) {
        remove(key);
        add(key, values);
    }

    @Override
    public void set(Map<K, List<V>> map) {
        clear();
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public List<V> remove(K key) {
        return mSource.remove(key);
    }

    @Override
    public void clear() {
        mSource.clear();
    }

    @Override
    public Set<K> keySet() {
        return mSource.keySet();
    }

    @Override
    public List<V> values() {
        List<V> allValues = new ArrayList<>();
        Set<K> keySet = keySet();
        for (K key : keySet) {
            allValues.addAll(getValues(key));
        }
        return allValues;
    }

    @Override
    public List<V> getValues(K key) {
        return mSource.get(key);
    }

    @Override
    public Set<Map.Entry<K, List<V>>> entrySet() {
        return mSource.entrySet();
    }

    @Override
    public V getValue(K key, int index) {
        List<V> values = getValues(key);
        if (values != null && index < values.size())
            return values.get(index);
        return null;
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public boolean isEmpty() {
        return mSource.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return mSource.containsKey(key);
    }

    public Map<K, List<V>> getSource() {
        return mSource;
    }
}