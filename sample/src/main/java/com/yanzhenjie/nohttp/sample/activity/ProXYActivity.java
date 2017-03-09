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
import android.view.View;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <p>通过代理服务器访问。</p>
 * Created in Jan 31, 2016 9:11:29 PM.
 *
 * @author Yan Zhenjie.
 */
public class ProXYActivity extends BaseActivity implements HttpListener<String> {

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_proxy);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_start)
    public void onClick(View v) {
        // 本来百度需要重定向，这里直接代理到百度的IP上，应该不需要重定向了。
        Request<String> request = NoHttp.createStringRequest("http://www.baidu.com");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("119.75.218.70", 80));
        request.setProxy(proxy);
        request(0, request, this, false, true);
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
