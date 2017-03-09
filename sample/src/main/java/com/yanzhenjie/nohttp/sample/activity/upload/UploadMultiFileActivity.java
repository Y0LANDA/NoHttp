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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.adapter.LoadFileAdapter;
import com.yanzhenjie.nohttp.sample.config.AppConfig;
import com.yanzhenjie.nohttp.sample.entity.LoadFile;
import com.yanzhenjie.nohttp.sample.nohttp.HttpListener;
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.nohttp.BasicBinary;
import com.yanzhenjie.nohttp.BitmapBinary;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.InputStreamBinary;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OnUploadListener;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.tools.ImageLocalLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * <p>上传多个文件，需要多个key，如果一个传FileList，请看{@link UploadFileListActivity}。</p>
 * Created on 2016/5/30.
 *
 * @author Yan Zhenjie;
 */
public class UploadMultiFileActivity extends BaseActivity {
    /**
     * 文件item。
     */
    private List<LoadFile> uploadFiles;
    /**
     * 上传文件list的适配器。
     */
    private LoadFileAdapter mUploadFileAdapter;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload_file_multi);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_upload_file_multi_activity);

        uploadFiles = new ArrayList<>();
        uploadFiles.add(new LoadFile(R.string.upload_file_status_wait, 0));
        uploadFiles.add(new LoadFile(R.string.upload_file_status_wait, 0));
        uploadFiles.add(new LoadFile(R.string.upload_file_status_wait, 0));

        mUploadFileAdapter = new LoadFileAdapter(uploadFiles);
        recyclerView.setAdapter(mUploadFileAdapter);
    }

    /**
     * 一个key上传多个文件，上传文件list。
     */
    private void uploadMultiListFile() {
        Request<String> request = NoHttp.createStringRequest(Constants.URL_NOHTTP_UPLOAD, RequestMethod.POST);

        // 添加普通参数。
        request.add("user", "yolanda");

        try {
            // 上传文件需要实现NoHttp的Binary接口，NoHttp默认实现了FileBinary、InputStreamBinary、ByteArrayBitnary、BitmapBinary。

            // 1. FileBinary用法。
            File file1 = new File(AppConfig.getInstance().APP_PATH_ROOT + "/image1.jpg");
            BasicBinary binary1 = new FileBinary(file1);
            /**
             * 监听上传过程，如果不需要监听就不用设置。
             * 第一个参数：what，what和handler的what一样，会在回调被调用的回调你开发者，作用是一个Listener可以监听多个文件的上传状态。
             * 第二个参数： 监听器。
             */
            binary1.setUploadListener(0, mOnUploadListener);
            request.add("image1", binary1);


            // 2. BitmapBinary用法。
            Bitmap file2 = ImageLocalLoader.getInstance().readImage(AppConfig.getInstance().APP_PATH_ROOT +
                    "/image2" +
                    ".jpg", 720, 1280);
            /**
             * 第一个参数是bitmap。
             * 第二个参数是文件名，因为bitmap无法获取文件名，所以需要传，如果你的服务器不关心这个参数，你可以不传。
             */
            BasicBinary binary2 = new BitmapBinary(file2, "userHead.jpg");// 或者：BasicBinary binary2 = new
            // BitmapBinary(file2, null);
            binary2.setUploadListener(1, mOnUploadListener);
            request.add("image2", binary2);


            // 3. InputStreamBinary用法。
            File file3 = new File(AppConfig.getInstance().APP_PATH_ROOT + "/image3.png");
            /**
             * 第一个参数是inputStream。
             * 第二个参数是文件名，因为bitmap无法获取文件名，所以需要传fileName，如果你的服务器不关心这个参数，你可以不传。
             */
            BasicBinary binary3 = new InputStreamBinary(new FileInputStream(file3), file3.getName());
            binary3.setUploadListener(2, mOnUploadListener);
            request.add("image3", binary3);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        request(0, request, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                showMessageDialog(R.string.request_succeed, response.get());
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showMessageDialog(R.string.request_succeed, response.getException().getMessage());
            }
        }, false, true);
    }

    /**
     * 文件上传监听。
     */
    private OnUploadListener mOnUploadListener = new OnUploadListener() {

        @Override
        public void onStart(int what) {// 这个文件开始上传。
            uploadFiles.get(what).setTitle(R.string.upload_start);
            mUploadFileAdapter.notifyItemChanged(what);
        }

        @Override
        public void onCancel(int what) {// 这个文件的上传被取消时。
            uploadFiles.get(what).setTitle(R.string.upload_cancel);
            mUploadFileAdapter.notifyItemChanged(what);
        }

        @Override
        public void onProgress(int what, int progress) {// 这个文件的上传进度发生边耍
            uploadFiles.get(what).setProgress(progress);
            mUploadFileAdapter.notifyItemChanged(what);
        }

        @Override
        public void onFinish(int what) {// 文件上传完成
            uploadFiles.get(what).setTitle(R.string.upload_succeed);
            mUploadFileAdapter.notifyItemChanged(what);
        }

        @Override
        public void onError(int what, Exception exception) {// 文件上传发生错误。
            uploadFiles.get(what).setTitle(R.string.upload_error);
            mUploadFileAdapter.notifyItemChanged(what);
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
                uploadMultiListFile();
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
                uploadMultiListFile();
            }

            @Override
            public void onFailed(int requestCode, List<String> deniedPermissions) {
            }
        });
    }
}
