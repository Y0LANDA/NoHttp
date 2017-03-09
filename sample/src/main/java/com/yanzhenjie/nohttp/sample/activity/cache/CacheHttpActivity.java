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
package com.yanzhenjie.nohttp.sample.activity.cache;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.adapter.RecyclerListSingleAdapter;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.rest.CacheMode;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

/**
 * <p>Http相应头304缓存演示.</p>
 * Created in Jan 31, 2016 12:11:03 PM.
 *
 * @author Yan Zhenjie.
 */
public class CacheHttpActivity extends BaseActivity {

	/*
     * 先来普及一下响应码304缓存是什么意思：
	 * 在RFC2616中，当http响应码是304时，表示客户端缓存有效， 客户端可以使用缓存；
	 * 
	 * NoHttp实现了Http协议1.1，很好的支持RESTFUL风格的接口；根据协议当请求方式是GET时， 且服务器响应头包涵Last-Modified时，
	 * 响应内容可以被客户端缓存起来，下次请求时只需要验证缓存，验证缓存时如果服务器响应码为304时，表示客户端缓存有效， 可以
	 * 继续使用缓存数据。
	 * 
	 * 由于NoHttp只是缓存了byte[]，所以不论图片，还是String都可以很好的被缓存。
	 */

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_cache_demo);

        List<String> cacheDataTypes = Arrays.asList(getResources().getStringArray(R.array.activity_cache_item));
        RecyclerListSingleAdapter listAdapter = new RecyclerListSingleAdapter(cacheDataTypes, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_cache_demo_activity);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(listAdapter);
    }

    private OnItemClickListener mItemClickListener = (v, position) -> {
        if (0 == position) {// 请求String。
            requestString();
        } else if (1 == position) {// 请求图片。
            requestImage();
        }
    };

    /**
     * 请求String。
     */
    private void requestString() {
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_CACHE_STRING);
        request.add("name", "yanzhenjie");
        request.add("pwd", 123);
        request.setCacheKey("CacheKeyDefaultString");// 这里的key是缓存数据的主键，默认是url，使用的时候要保证全局唯一，否则会被其他相同url数据覆盖。
        request.setCacheMode(CacheMode.DEFAULT);//默认就是DEFAULT，所以这里可以不用设置，DEFAULT代表走Http标准协议。
        request(0, request, stringHttpListener, false, true);
    }

    private HttpListener<String> stringHttpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String string = response.isFromCache() ? getString(R.string.request_from_cache) : getString(R.string
                    .request_from_network);
            showMessageDialog(string, response.get());
        }

        @Override
        public void onFailed(int what, Response<String> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

    /**
     * 请求Image。
     */
    private void requestImage() {
        Request<Bitmap> request = NoHttp.createImageRequest(Constants.URL_NOHTTP_CACHE_IMAGE);
        request.setCacheKey("CacheKeyDefaultImage");// 这里的key是缓存数据的主键，默认是url，使用的时候要保证全局唯一，否则会被其他相同url数据覆盖。
        request.setCacheMode(CacheMode.DEFAULT);//默认就是DEFAULT，所以这里可以不用设置，DEFAULT代表走Http标准协议。
        request(0, request, imageHttpListener, false, true);
    }

    private HttpListener<Bitmap> imageHttpListener = new HttpListener<Bitmap>() {
        @Override
        public void onSucceed(int what, Response<Bitmap> response) {
            String string = response.isFromCache() ? getString(R.string.request_from_cache) : getString(R.string
                    .request_from_network);
            showImageDialog(string, response.get());
        }

        @Override
        public void onFailed(int what, Response<Bitmap> response) {
            showMessageDialog(R.string.request_failed, response.getException().getMessage());
        }
    };

}
