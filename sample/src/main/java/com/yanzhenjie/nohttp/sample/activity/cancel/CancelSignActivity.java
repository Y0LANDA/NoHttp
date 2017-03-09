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
package com.yanzhenjie.nohttp.sample.activity.cancel;

import android.os.Bundle;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;

/**
 * <p>根据sign取消某几个请求。</p>
 * Created on 2016/5/31.
 *
 * @author Yan Zhenjie;
 */
public class CancelSignActivity extends BaseActivity {

    /**
     * 用来标志请求的sign。
     */
    private Object cancelSign = new Object();

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_cacel_demo);

        // 请求1。
        Request<String> request1 = NoHttp.createStringRequest(Constants.URL_NOHTTP_JSONOBJECT, RequestMethod.GET);
        request1.setCancelSign(cancelSign);

        // 请求2。
        Request<String> request2 = NoHttp.createStringRequest(Constants.URL_NOHTTP_JSONOBJECT, RequestMethod.GET);
        request2.setCancelSign(cancelSign);

        // 请求3。
        Request<String> request3 = NoHttp.createStringRequest(Constants.URL_NOHTTP_JSONOBJECT, RequestMethod.GET);
        request3.setCancelSign(cancelSign);

        /**
         * 1. 第一步：
         * 这里假设有好多请求在队列中。
         * 我们给每一个请求setSign。
         */
//        CallServer.getRequestInstance().add(this, 0, request1, this, true, false);
//        CallServer.getRequestInstance().add(this, 1, request2, this, true, false);
//        CallServer.getRequestInstance().add(this, 2, request3, this, true, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 2. 第二步‘：
         * 通过队列的cancelBySign可以取消上面setSign的所有请求，包括正在执行的请求。
         *
         * 注意：这里的sign一定得是同一个对象。
         *
         * 特别注意：有人把"123"这种字符串穿进去，取消的时候又了"123"字符串，这样就不是同一个对象了，不能成功的取消请求。
         */
        cancelBySign(cancelSign);
    }
}
