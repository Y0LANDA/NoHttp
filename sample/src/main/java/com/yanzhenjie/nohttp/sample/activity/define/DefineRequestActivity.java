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
package com.yanzhenjie.nohttp.sample.activity.define;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.entity.YanZhenjie;
import com.yanzhenjie.nohttp.sample.nohttp.FastJsonRequest;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.nohttp.JavaBeanRequest;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created in Feb 1, 2016 9:14:37 AM.
 *
 * @author Yan Zhenjie.
 */
public class DefineRequestActivity extends BaseActivity {

    @BindView(R.id.tv_result)
    TextView mTvResult;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_define_reqeust);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_fast_json, R.id.btn_java_bean})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_fast_json) {
            /**
             * 这里用的demo自定义的FastJsonRequest解析服务器的json。
             */
            Request<JSONObject> request = new FastJsonRequest(Constants.URL_NOHTTP_JSONOBJECT, RequestMethod.GET);
            request.add("name", "yanzhenjie");
            request.add("pwd", 123);
            request(0, request, jsonHttpListener, false, true);
        } else if (v.getId() == R.id.btn_java_bean) {
            /**
             * 这里用的是demo自定义的JavaBeanRequest对象对请求，里面用fastjson解析服务器的数据。
             */
            Request<YanZhenjie> request = new JavaBeanRequest<>(Constants.URL_NOHTTP_JSONOBJECT, YanZhenjie.class);
            request.add("name", "yanzhenjie");
            request.add("pwd", 123);
            request(0, request, zhenjieHttpListener, false, true);
        }
    }

    /**
     * 接受FastJson的相应结果。
     */
    private HttpListener<JSONObject> jsonHttpListener = new HttpListener<JSONObject>() {
        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            JSONObject jsonObject = response.get();
            if (1 == jsonObject.getIntValue("error")) {
                String result = getString(R.string.request_json_result);
                result = String.format(Locale.getDefault(), result, response.request().getRequestMethod()
                        .toString(), jsonObject.getString("url"), jsonObject.getString("data"), jsonObject
                        .getString("error"));
                mTvResult.setText(result);
            }
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

    /**
     * 接受JavaBean响应。
     */
    private HttpListener<YanZhenjie> zhenjieHttpListener = new HttpListener<YanZhenjie>() {
        @Override
        public void onSucceed(int what, Response<YanZhenjie> response) {
            YanZhenjie yanZhenjie = response.get();
            mTvResult.setText(yanZhenjie.toString());
        }

        @Override
        public void onFailed(int what, Response<YanZhenjie> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

}
