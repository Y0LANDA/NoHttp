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
package com.yanzhenjie.nohttp.able;

/**
 * <p>Cancel interface.</p>
 * Created in Dec 17, 2015 11:42:10 AM.
 *
 * @author Yan Zhenjie;
 */
public interface Cancelable {

    /**
     * Cancel handle.
     */
    void cancel();

    /**
     * Whether has been cancelled.
     *
     * @return true: canceled, false: there is no cancel.
     */
    boolean isCanceled();

}
