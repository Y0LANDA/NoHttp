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

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created on 2016/6/23.
 *
 * @author Yan Zhenjie.
 */
public class TreeMultiValueMap<K, V> extends BasicMultiValueMap<K, V> {

    public TreeMultiValueMap() {
        super(new TreeMap<K, List<V>>());
    }

    public TreeMultiValueMap(Comparator<K> keyComparator) {
        super(new TreeMap<K, List<V>>(keyComparator));
    }
}
