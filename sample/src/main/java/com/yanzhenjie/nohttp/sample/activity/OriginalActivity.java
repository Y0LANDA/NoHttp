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
import android.widget.TextView;

import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.NotFoundCacheError;
import com.yanzhenjie.nohttp.error.TimeoutError;
import com.yanzhenjie.nohttp.error.URLError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.dialog.WaitDialog;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.sample.util.Toast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <p>最原始的使用方法。这里没有任何的对NoHttp的封装，就是new队列，然后把new请求，把请求添加到队列，完成请求。</p>
 * Created in Nov 4, 2015 1:38:02 PM.
 *
 * @author Yan Zhenjie.
 */
public class OriginalActivity extends BaseActivity {

    /**
     * 用来标志请求的what, 类似handler的what一样，这里用来区分请求。
     */
    private static final int NOHTTP_WHAT_TEST = 0x001;

    /**
     * 请求的时候等待框。
     */
    private WaitDialog mWaitDialog;

    /**
     * 请求队列。
     */
    private RequestQueue mQueue;

    @BindView(R.id.tv_result)
    TextView mTvResult;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_original);
        ButterKnife.bind(this);

        mWaitDialog = new WaitDialog(this);

        // 初始化请求队列，传入的参数是请求并发值。
        mQueue = NoHttp.newRequestQueue();
    }

    private Object sign = new Object();

    @OnClick(R.id.btn_start)
    public void onClick(View v) {
        // 创建请求对象。
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_JSONOBJECT, RequestMethod.GET);

        // 添加请求参数。
        request.add("name", "yanzhenjie") // String型。
                .add("pwd", 123) // int型。
                .add("userAge", 1.25) // double型。
                .add("nooxxx", 1.2F) // flocat型。

                // 单个请求的超时时间，不指定就会使用全局配置。
                .setConnectTimeout(10 * 1000) // 设置连接超时。
                .setReadTimeout(20 * 1000) // 设置读取超时时间，也就是服务器的响应超时。

                // 请求头，是否要添加头，添加什么头，要看开发者服务器端的要求。
                .addHeader("Author", "sample")
                .setHeader("User", "Jason")

                // 设置一个tag, 在请求完(失败/成功)时原封不动返回; 多数情况下不需要。
                .setTag(new Object())
                // 设置取消标志。
                .setCancelSign(sign);

		/*
         * what: 当多个请求同时使用同一个OnResponseListener时用来区分请求, 类似handler的what一样。
		 * request: 请求对象。
		 * onResponseListener 回调对象，接受请求结果。
		 */
        mQueue.add(NOHTTP_WHAT_TEST, request, onResponseListener);
    }

    /**
     * 回调对象，接受请求结果.
     */
    private OnResponseListener<String> onResponseListener = new OnResponseListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            if (what == NOHTTP_WHAT_TEST) {// 根据what判断是哪个请求的返回，这样就可以用一个OnResponseListener来接受多个请求的结果。
                int responseCode = response.getHeaders().getResponseCode();// 服务器响应码。

                String result = response.get();// 响应结果。

                mTvResult.setText(result);

                Object tag = response.getTag();// 拿到请求时设置的tag。

                // 响应头
                Headers headers = response.getHeaders();
                String headResult = getString(R.string.request_original_result);
                headResult = String.format(Locale.getDefault(), headResult, headers.getResponseCode(),
                        response.getNetworkMillis());
                mTvResult.setText(headResult);
            }
        }

        @Override
        public void onStart(int what) {
            // 请求开始，这里可以显示一个dialog
            if (mWaitDialog != null && !mWaitDialog.isShowing())
                mWaitDialog.show();
        }

        @Override
        public void onFinish(int what) {
            // 请求结束，这里关闭dialog
            if (mWaitDialog != null && mWaitDialog.isShowing())
                mWaitDialog.dismiss();
        }

        @Override
        public void onFailed(int what, Response<String> response) {
            //TODO 特别注意：这里可能有人会想到是不是每个地方都要这么判断，其实不用，请参考HttpResponseListener类的封装，你也可以这么封装。

            // 请求失败
            Throwable exception = response.getException();
            if (exception instanceof NetworkError) {// 网络不好
                Toast.show(OriginalActivity.this, R.string.error_please_check_network);
            } else if (exception instanceof TimeoutError) {// 请求超时
                Toast.show(OriginalActivity.this, R.string.error_timeout);
            } else if (exception instanceof UnKnownHostError) {// 找不到服务器
                Toast.show(OriginalActivity.this, R.string.error_not_found_server);
            } else if (exception instanceof URLError) {// URL是错的
                Toast.show(OriginalActivity.this, R.string.error_url_error);
            } else if (exception instanceof NotFoundCacheError) {
                // 这个异常只会在仅仅查找缓存时没有找到缓存时返回
                Toast.show(OriginalActivity.this, R.string.error_not_found_cache);
            } else {
                Toast.show(OriginalActivity.this, R.string.error_unknow);
            }
            Logger.e("错误：" + exception.getMessage());
        }
    };

    @Override
    protected void onDestroy() {
        // 和声明周期绑定，退出时取消这个队列中的所有请求，当然可以在你想取消的时候取消也可以，不一定和声明周期绑定。
        mQueue.cancelBySign(sign);

        // 因为回调函数持有了activity，所以退出activity时请停止队列。
        mQueue.stop();

        super.onDestroy();
    }
}
