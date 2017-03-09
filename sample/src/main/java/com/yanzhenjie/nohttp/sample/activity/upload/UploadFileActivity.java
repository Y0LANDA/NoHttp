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
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.adapter.RecyclerListSingleAdapter;
import com.yanzhenjie.nohttp.sample.config.AppConfig;
import com.yanzhenjie.nohttp.sample.dialog.WaitDialog;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.nohttp.tools.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

/**
 * <p>
 * 上传文件demo.
 * </p>
 * Created in Oct 23, 2015 8:40:52 AM.
 *
 * @author Yan Zhenjie.
 */
public class UploadFileActivity extends BaseActivity {

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload);

        List<String> imageItems = Arrays.asList(getResources().getStringArray(R.array.activity_upload_item));
        RecyclerListSingleAdapter listAdapter = new RecyclerListSingleAdapter(imageItems, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_upload_activity);
        recyclerView.setAdapter(listAdapter);

        if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            saveFile();
        else
            AndPermission.with(this)
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .requestCode(100)
                    .send();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionListener() {
            @Override
            public void onSucceed(int requestCode, List<String> grantPermissions) {
                AppConfig.getInstance().initialize();
                saveFile();
            }

            @Override
            public void onFailed(int requestCode, List<String> deniedPermissions) {
                finish();
            }
        });
    }

    private OnItemClickListener mItemClickListener = (v, position) -> {
        if (0 == position) {
            startActivity(new Intent(UploadFileActivity.this, UploadSingleFileActivity.class));
        } else if (position == 1) {
            startActivity(new Intent(UploadFileActivity.this, UploadMultiFileActivity.class));
        } else if (position == 2) {
            startActivity(new Intent(UploadFileActivity.this, UploadFileListActivity.class));
        } else if (position == 3) {
            startActivity(new Intent(UploadFileActivity.this, UploadAlbumActivity.class));
        }
    };

	/* ====================先保存文件到SD卡==================== */

    private WaitDialog mDialog;

    private void saveFile() {
        mDialog = new WaitDialog(this);
        mDialog.show();
        new Thread(saveFileThread).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            mDialog.dismiss();
        }
    };

    private Runnable saveFileThread = () -> {
        try {
            AppConfig.getInstance().initialize();

            InputStream inputStream = getAssets().open("123.jpg");
            IOUtils.write(inputStream, new FileOutputStream(AppConfig.getInstance().APP_PATH_ROOT + File
                    .separator +
                    "image1.jpg"));
            IOUtils.closeQuietly(inputStream);

            inputStream = getAssets().open("234.jpg");
            IOUtils.write(inputStream, new FileOutputStream(AppConfig.getInstance().APP_PATH_ROOT + File
                    .separator +
                    "image2.jpg"));
            IOUtils.closeQuietly(inputStream);

            inputStream = getAssets().open("456.png");
            IOUtils.write(inputStream, new FileOutputStream(AppConfig.getInstance().APP_PATH_ROOT + File
                    .separator +
                    "image3.png"));
            IOUtils.closeQuietly(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        handler.obtainMessage().sendToTarget();
    };
}
