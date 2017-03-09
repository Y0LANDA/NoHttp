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
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.IBasicRequest;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RedirectHandler;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created in Jan 31, 2016 4:30:31 PM;
 *
 * @author Yan Zhenjie.
 */
public class RedirectActivity extends BaseActivity implements HttpListener<String> {

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_redirect);

        List<String> imageItems = Arrays.asList(getResources().getStringArray(R.array.activity_redirect_item));
        RecyclerListSingleAdapter listAdapter = new RecyclerListSingleAdapter(imageItems, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_redirect_activity);
        recyclerView.setAdapter(listAdapter);
    }

    private OnItemClickListener mItemClickListener = (v, position) -> {
        if (0 == position) {
            requestAllowRedirect();
        } else {
            requestDisAllowRedirect();
        }
    };

    /**
     * 允许重定向的请求
     */
    public void requestAllowRedirect() {
        final Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_REDIRECT_BAIDU);
        request.setRedirectHandler(new RedirectHandler() {
            @Override
            public IBasicRequest onRedirect(Headers responseHeaders) {
                // 允许重定向时这个方法会被调用
                // 1. 返回null，NoHttp会自动拷贝父请求的请求方法和代理自动请求，不会拷贝其他属性。
                // 2. 返回非null，会把这个新请求的数据交给父请求去解析。
                Request<String> redirectRequest = NoHttp.createStringRequest(responseHeaders.getLocation());
                redirectRequest.setRedirectHandler(this);// 为了防止嵌套重定向，这里可以每个子级请求都监听
                return redirectRequest;
            }

            // 是否不允许重定向。
            @Override
            public boolean isDisallowedRedirect(Headers responseHeaders) {
                // 返回false表示允许重定向
                return false;
            }
        });
        request(0, request, this, false, true);
    }

    /**
     * 不允许重定向的请求
     */
    private void requestDisAllowRedirect() {
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_REDIRECT_BAIDU);
        request.setRedirectHandler(new RedirectHandler() {
            @Override
            public IBasicRequest onRedirect(Headers responseHeaders) {
                // 不允许重定向时此方法不会被调用。
                return null;
            }

            @Override
            public boolean isDisallowedRedirect(Headers responseHeaders) {
                // 返回true代表不允许重定向。
                return true;
            }
        });
        request(0, request, this, false, true);
    }

    @Override
    public void onSucceed(int what, Response<String> response) {
        Headers headers = response.getHeaders();
        if (headers.getResponseCode() == 302 || headers.getResponseCode() == 301 || headers.getResponseCode() ==
                307) {
            String message = getString(R.string.request_redirect_location);
            message = String.format(Locale.getDefault(), message, headers.getLocation());
            showMessageDialog(R.string.request_succeed, message);
        } else if (headers.getResponseCode() == 200) {
            showMessageDialog(R.string.request_succeed, response.get());
        }
    }

    @Override
    public void onFailed(int what, Response<String> response) {
        showMessageDialog(R.string.request_failed, response.getException().getMessage());
    }
}