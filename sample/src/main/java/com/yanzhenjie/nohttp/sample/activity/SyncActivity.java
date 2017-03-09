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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.dialog.WaitDialog;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <p>同步请求。</p>
 * Created in Oct 23, 2015 1:13:06 PM.
 *
 * @author Yan Zhenjie.
 */
public class SyncActivity extends BaseActivity {

    /**
     * 等待的dialog。
     */
    private WaitDialog waitDialog;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sync);
        ButterKnife.bind(this);
    }

    /**
     * 解析响应.
     */
    private void response(Response<String> response) {
        if (response.isSucceed()) {
            showMessageDialog(R.string.request_succeed, response.get());
        } else {
            showMessageDialog(R.string.request_succeed, response.getException().getMessage());
        }
    }

    /**
     * handler接受子线程结果.
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {// 如果是同步请求发回来的结果
                closeDialog();

                @SuppressWarnings("unchecked")
                Response<String> response = (Response<String>) msg.obj;
                response(response);
            }
        }
    };

    @OnClick(R.id.btn_start)
    public void onClick(View v) {
        showDialog();
        new Thread() {
            public void run() {
                // 在子线程中可以使用同步请求
                Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_JSONOBJECT,
                        RequestMethod.GET);
                request.add("name", "yanzhenjie");
                request.add("pwd", 123);
                Response<String> response = NoHttp.startRequestSync(request);
                handler.obtainMessage(0, response).sendToTarget();
            }
        }.start();
    }

    /**
     * 显示等待Dialog。
     */
    private void showDialog() {
        if (waitDialog == null) {
            waitDialog = new WaitDialog(this);
        }
        if (!waitDialog.isShowing())
            waitDialog.show();
    }

    /**
     * 关闭等待dialog。
     */
    private void closeDialog() {
        if (waitDialog != null && waitDialog.isShowing())
            waitDialog.dismiss();
    }

}