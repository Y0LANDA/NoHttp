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
import com.yanzhenjie.nohttp.sample.adapter.RecyclerListMultiAdapter;
import com.yanzhenjie.nohttp.sample.entity.ListItem;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindArray;
import butterknife.ButterKnife;

/**
 * <p>演示各种请求方法Demo.<p>
 * Created in Oct 23, 2015 1:13:06 PM.
 *
 * @author Yan Zhenjie.
 */
public class MethodActivity extends BaseActivity {

    @BindArray(R.array.activity_method_item)
    String[] titles;

    @BindArray(R.array.activity_method_item_des)
    String[] titlesDes;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_method);
        ButterKnife.bind(this);

        List<ListItem> listItems = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            listItems.add(new ListItem(titles[i], titlesDes[i]));
        }

        RecyclerListMultiAdapter listAdapter = new RecyclerListMultiAdapter(listItems, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_method_activity);
        recyclerView.setAdapter(listAdapter);
    }

    private OnItemClickListener mItemClickListener = (v, position) -> request(position);

    /**
     * @param position
     */
    private void request(int position) {
        Request<String> request = null;
        switch (position) {
            case 0:
                showMessageDialog(R.string.method_request_failed, R.string.method_request_failed_reason);
                break;
            case 1:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.GET);
                break;
            case 2:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.POST);
                break;
            case 3:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.PUT);
                break;
            case 4:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.DELETE);
                break;
            case 5:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.HEAD);
                break;
            case 6:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.PATCH);
                break;
            case 7:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.OPTIONS);
                break;
            case 8:
                request = NoHttp.createStringRequest(Constants.URL_NOHTTP_METHOD, RequestMethod.TRACE);
                break;
            default:
                break;
        }

        if (request != null) {
            request.add("name", "yanzhenjie");// String类型
            request.add("pwd", 123);
            request.add("userAge", 20);// int类型
            request.add("userSex", '1');// char类型，还支持其它类型

//        // 添加到请求队列
            request(0, request, httpListener, true, true);
        }
    }

    private HttpListener<String> httpListener = new HttpListener<String>() {

        @Override
        public void onSucceed(int what, Response<String> response) {
            if (response.getHeaders().getResponseCode() == 501) {
                showMessageDialog(R.string.request_succeed, R.string.request_method_patch);
            } else if (RequestMethod.HEAD == response.request().getRequestMethod())// 请求方法为HEAD时没有响应内容
                showMessageDialog(R.string.request_succeed, R.string.request_method_head);
            else if (response.getHeaders().getResponseCode() == 405) {
                List<String> allowList = response.getHeaders().getValues("Allow");
                String allow = getString(R.string.request_method_not_allow);
                if (allowList != null && allowList.size() > 0) {
                    allow = String.format(Locale.getDefault(), allow, allowList.get(0));
                }
                showMessageDialog(R.string.request_succeed, allow);
            } else
                showMessageDialog(R.string.request_succeed, response.get());
        }

        @Override
        public void onFailed(int what, Response<String> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

}