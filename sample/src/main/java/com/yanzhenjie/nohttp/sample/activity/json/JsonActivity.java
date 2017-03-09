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
package com.yanzhenjie.nohttp.sample.activity.json;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created in Jan 31, 2016 10:16:26 PM.
 *
 * @author Yan Zhenjie.
 */
public class JsonActivity extends BaseActivity {

    @BindView(R.id.tv_result)
    TextView mTvResult;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_json);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_object_reqeust, R.id.btn_array_request})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_object_reqeust) {
            Request<JSONObject> request = NoHttp.createJsonObjectRequest(Constants.URL_NOHTTP_JSONOBJECT);
            request.add("name", "yanzhenjie");
            request.add("pwd", 123);
            request(0, request, objectListener, true, true);
        } else if (v.getId() == R.id.btn_array_request) {
            Request<JSONArray> request = NoHttp.createJsonArrayRequest(Constants.URL_NOHTTP_JSONARRAY);
            request.add("name", "yanzhenjie");
            request.add("pwd", 123);
            request(1, request, arrayListener, true, true);
        }
    }

    private HttpListener<JSONObject> objectListener = new HttpListener<JSONObject>() {
        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            JSONObject jsonObject = response.get();
            if (1 == jsonObject.optInt("error", -1)) {
                String result = getString(R.string.request_json_result);
                result = String.format(Locale.getDefault(), result, response.request().getRequestMethod()
                        .toString(), jsonObject.optString("url"), jsonObject.optString("data"), jsonObject
                        .optString("error"));
                mTvResult.setText(result);
            }
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

    private HttpListener<JSONArray> arrayListener = new HttpListener<JSONArray>() {
        @Override
        public void onSucceed(int what, Response<JSONArray> response) {
            JSONArray jsonArray = response.get();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < jsonArray.length(); i++) {
                String string = jsonArray.optString(i);
                builder.append(string).append("\n");
            }

            mTvResult.setText(builder.toString());
        }

        @Override
        public void onFailed(int what, Response<JSONArray> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

}
