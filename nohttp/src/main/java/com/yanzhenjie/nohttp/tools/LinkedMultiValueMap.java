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

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created in Jan 10, 2016 5:03:17 PM.
 *
 * @author Yan Zhenjie.
 */
public class LinkedMultiValueMap<K, V> extends BasicMultiValueMap<K, V> {

    public LinkedMultiValueMap() {
        super(new LinkedHashMap<K, List<V>>());
    }
}
