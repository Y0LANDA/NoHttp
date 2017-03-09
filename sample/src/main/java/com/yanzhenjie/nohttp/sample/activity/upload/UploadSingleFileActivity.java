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
package com.yanzhenjie.nohttp.sample.activity.upload;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.config.AppConfig;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.nohttp.BasicBinary;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OnUploadListener;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>上传单个文件。</p>
 * Created on 2016/5/30.
 *
 * @author Yan Zhenjie;
 */
public class UploadSingleFileActivity extends BaseActivity {

    /**
     * 单个文件上传监听的标志。
     */
    private final int WHAT_UPLOAD_SINGLE = 0x01;

    /**
     * 文件的上传状态。
     */
    @BindView(R.id.tv_result)
    TextView mTvResult;
    /**
     * 进度条。
     */
    @BindView(R.id.pb_progress)
    ProgressBar mPbProgress;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload_single);
        ButterKnife.bind(this);
        ButterKnife.findById(this, R.id.rv_upload_file_single_root).setClickable(true);
    }

    /**
     * 上传单个文件。
     */
    private void uploadSingleFile() {
        mTvResult.setText(null);
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_UPLOAD, RequestMethod.POST);

        // 添加普通参数。
        request.add("user", "yolanda");

        // 上传文件需要实现NoHttp的Binary接口，NoHttp默认实现了FileBinary、InputStreamBinary、ByteArrayBitnary、BitmapBinary。

        // FileBinary用法
        String filePath = AppConfig.getInstance().APP_PATH_ROOT + "/image1.jpg";
        BasicBinary binary = new FileBinary(new File(filePath));

        /**
         * 监听上传过程，如果不需要监听就不用设置。
         * 第一个参数：what，what和handler的what一样，会在回调被调用的回调你开发者，作用是一个Listener可以监听多个文件的上传状态。
         * 第二个参数： 监听器。
         */
        binary.setUploadListener(WHAT_UPLOAD_SINGLE, mOnUploadListener);

        request.add("image0", binary);// 添加1个文件
//            request.add("image1", fileBinary1);// 添加2个文件

        request(0, request, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                showMessageDialog(R.string.request_succeed, response.get());
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showMessageDialog(R.string.request_failed, response.getException().getMessage());
            }
        }, false, true);
    }


    /**
     * 文件上传监听。
     */
    private OnUploadListener mOnUploadListener = new OnUploadListener() {

        @Override
        public void onStart(int what) {// 这个文件开始上传。
            mTvResult.setText(R.string.upload_start);
        }

        @Override
        public void onCancel(int what) {// 这个文件的上传被取消时。
            mTvResult.setText(R.string.upload_cancel);
        }

        @Override
        public void onProgress(int what, int progress) {// 这个文件的上传进度发生边耍
            mPbProgress.setProgress(progress);
        }

        @Override
        public void onFinish(int what) {// 文件上传完成
            mTvResult.setText(R.string.upload_succeed);
        }

        @Override
        public void onError(int what, Exception exception) {// 文件上传发生错误。
            mTvResult.setText(R.string.upload_error);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_upload_file, menu);
        return true;
    }

    @Override
    protected boolean onOptionsItemSelectedCompat(MenuItem item) {
        if (item.getItemId() == R.id.menu_upload_file_request) {
            if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                uploadSingleFile();
            else
                AndPermission.with(this)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .requestCode(100)
                        .send();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionListener() {
            @Override
            public void onSucceed(int requestCode, List<String> grantPermissions) {
                uploadSingleFile();
            }

            @Override
            public void onFailed(int requestCode, List<String> deniedPermissions) {
            }
        });
    }
}