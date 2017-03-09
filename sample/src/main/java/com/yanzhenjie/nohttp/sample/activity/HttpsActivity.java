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
package com.yanzhenjie.nohttp.sample.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.adapter.RecyclerListSingleAdapter;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;
import com.yanzhenjie.nohttp.sample.util.SSLContextUtil;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import butterknife.ButterKnife;

/**
 * Created in Nov 3, 2015 1:48:34 PM.
 *
 * @author Yan Zhenjie.
 */
public class HttpsActivity extends BaseActivity implements HttpListener<String> {

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_https);

        List<String> imageItems = Arrays.asList(getResources().getStringArray(R.array.activity_https_item));
        RecyclerListSingleAdapter listAdapter = new RecyclerListSingleAdapter(imageItems, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_https_activity);
        recyclerView.setAdapter(listAdapter);
    }

    private OnItemClickListener mItemClickListener = (v, position) -> {
        if (0 == position) {
            httpsVerify();
        } else {
            httpsNoVerify();
        }
    };

    /**
     * Https请求，带证书。
     */
    private void httpsVerify() {
        Request<String> httpsRequest = NoHttp.createStringRequest("https://kyfw.12306.cn/otn/", RequestMethod.GET);
        SSLContext sslContext = SSLContextUtil.getSSLContext();

        // 主要是需要一个SocketFactory对象，这个对象是java通用的，具体用法还请Google、Baidu。
        if (sslContext != null)
            httpsRequest.setSSLSocketFactory(sslContext.getSocketFactory());
        request(0, httpsRequest, this, false, true);
    }

    /**
     * Https请求，不带证书。
     */
    private void httpsNoVerify() {
        Request<String> httpsRequest = NoHttp.createStringRequest("https://kyfw.12306.cn/otn/", RequestMethod.GET);
        SSLContext sslContext = SSLContextUtil.getDefaultSLLContext();
        if (sslContext != null)
            httpsRequest.setSSLSocketFactory(sslContext.getSocketFactory());
        httpsRequest.setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
        request(0, httpsRequest, this, false, true);
    }

    @Override
    public void onSucceed(int what, Response<String> response) {
        showMessageDialog(R.string.request_succeed, response.get());
    }

    @Override
    public void onFailed(int what, Response<String> response) {
        showMessageDialog(R.string.request_failed, response.getException().getMessage());
    }
}
