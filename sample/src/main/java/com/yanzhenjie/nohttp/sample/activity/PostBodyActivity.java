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
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.sample.util.Snackbar;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.tools.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <p>提交Json到服务器。</p>
 * Created on 2016/5/29.
 *
 * @author Yan Zhenjie;
 */
public class PostBodyActivity extends BaseActivity {

    /**
     * 要提交的数据。
     */
    @BindView(R.id.edt_post_body)
    EditText mEdtPostBody;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_post_body);
        ButterKnife.bind(this);

        try {
            InputStream inputStream = getAssets().open("json");
            String s = IOUtils.toString(inputStream);
            mEdtPostBody.setText(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_start)
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            pushBody();
        }
    }

    /**
     * 提交JSON、XML、String、InputStream、ByteArray。
     */
    private void pushBody() {
        /**
         * 这里要注意的是：
         * 1. 请求方法一定是POST、PUT等可以直接写流出去的方法。
         */
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_POSTBODY, RequestMethod.POST);
        request.add("name", "yanzhenjie");
        request.add("pwd", 123);

        /**
         * 下面就是怎么setBody，几种方法，根据自己的需要选择：
         */

//        1. 这里可以push任何数据上去（比如String、Json、XML、图片）。
//        request.setDefineRequestBody(InputStream body, String contentType);

//        2. 这里可以push任何string数据（json、xml等），并可以指定contentType。
//        request.setDefineRequestBody(String body, String contentType);

//        3. 下面的两个的contentType默认为application/json，传进去的数据要为json。
//        request.setDefineRequestBodyForJson(JSONObject jsonBody);
//        request.setDefineRequestBodyForJson(String jsonBody);

//        4. 这里的contentType默认为application/xml。
//        request.setDefineRequestBodyForXML(String xmlBody);

        // 这里我们用json多例子
        String jsonBody = mEdtPostBody.getText().toString();
        if (TextUtils.isEmpty(jsonBody)) {
            Snackbar.show(this, R.string.request_json_body_input_tip);
        } else {
            Logger.i("提交的数据：" + jsonBody);
            request.setDefineRequestBodyForJson(jsonBody);
            request(0, request, httpListener, false, true);
        }
    }

    private HttpListener<String> httpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            showMessageDialog(R.string.request_succeed, response.get());
        }

        @Override
        public void onFailed(int what, Response<String> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };
}
