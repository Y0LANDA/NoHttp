/*
 * Copyright © Yan Zhenjie. All Rights Reserved
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

/**
 * Created by Yan Zhenjie on 2017/2/14.
 */
public interface Delivery {

    boolean post(Runnable r);

    boolean postDelayed(Runnable r, long delayMillis);

    boolean postAtFrontOfQueue(Runnable r);

    boolean postAtTime(Runnable r, long uptimeMillis);

    boolean postAtTime(Runnable r, Object token, long uptimeMillis);

}
