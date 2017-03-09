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
package com.yanzhenjie.nohttp.sample.activity.download;

import android.Manifest;
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
import com.yanzhenjie.nohttp.sample.util.Constants;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.download.DownloadListener;
import com.yanzhenjie.nohttp.download.DownloadRequest;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.ServerError;
import com.yanzhenjie.nohttp.error.StorageReadWriteError;
import com.yanzhenjie.nohttp.error.StorageSpaceNotEnoughError;
import com.yanzhenjie.nohttp.error.TimeoutError;
import com.yanzhenjie.nohttp.error.URLError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.tools.IOUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * <p>下载多个文件演示。这里为了简单就把下载卸载当前activity中，建议封装到service中。</p>
 * Created on 2016/5/30.
 *
 * @author Yan Zhenjie;
 */
public class DownloadFileListActivity extends BaseActivity {

    /**
     * 文件下载进度记录
     */
    private final static String PROGRESS_KEY = "download_list_progress";

    /**
     * 文件列表适配器。
     */
    private LoadFileAdapter mLoadFileAdapter;
    /**
     * 文件列表。
     */
    private List<LoadFile> mFileList;

    /**
     * 下载任务列表。
     */
    private List<DownloadRequest> mDownloadRequests;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_download_list);

        mFileList = new ArrayList<>();
        mDownloadRequests = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            // 读取每个文件的进度
            int progress = AppConfig.getInstance().getInt(PROGRESS_KEY + i, 0);

            // 设置每个文件的状态
            String title = getString(R.string.upload_file_status_wait);
            if (progress == 100)
                title = getString(R.string.download_status_finish);
            else if (progress > 0)
                title = getSProgress(progress, 0);

            LoadFile downloadFile = new LoadFile(title, progress);
            mFileList.add(downloadFile);

            /**
             * 这里不传文件名称、不断点续传，则会从响应头中读取文件名自动命名，如果响应头中没有则会从url中截取。
             */
            // url 下载地址。
            // fileFolder 文件保存的文件夹。
            // isDeleteOld 在指定的文件夹发现同名的文件是否删除后重新下载，true则删除重新下载，false则直接通知下载成功。
            // mDownloadRequest = NoHttp.createDownloadRequest(Constants.URL_DOWNLOADS[0], AppConfig.getInstance()
            // .APP_PATH_ROOT, true);

            /**
             * 如果要使用断点续传下载，则一定要指定文件名。
             */
            // url 下载地址。
            // fileFolder 保存的文件夹。
            // fileName 文件名。
            // isRange 是否断点续传下载。
            // isDeleteOld 在指定的文件夹发现同名的文件是否删除后重新下载，true则删除重新下载，false则直接通知下载成功。
            DownloadRequest downloadRequest = NoHttp.createDownloadRequest(Constants.URL_DOWNLOADS[i], AppConfig
                    .getInstance().APP_PATH_ROOT, "nohttp_list" + i + ".apk", true, true);
            mDownloadRequests.add(downloadRequest);
        }

        mLoadFileAdapter = new LoadFileAdapter(mFileList);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_download_list_activity);
        recyclerView.setAdapter(mLoadFileAdapter);

        // 提示用wifi
        showMessageDialog(R.string.tip, R.string.download_mobile_data_tip);
    }

    /**
     * 开始下载全部。
     */
    private void download() {
        for (int i = 0; i < mDownloadRequests.size(); i++) {
            NoHttp.getDownloadQueueInstance().add(i, mDownloadRequests.get(i), downloadListener);
        }
    }

    /**
     * 下载状态监听。
     */
    /**
     * 下载监听
     */
    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onStart(int what, boolean isResume, long beforeLength, Headers headers, long allCount) {
            int progress = AppConfig.getInstance().getInt(PROGRESS_KEY, 0);
            if (allCount != 0) {
                progress = (int) (beforeLength * 100 / allCount);
            }

            updateProgress(what, progress, 0);
        }

        @Override
        public void onDownloadError(int what, Exception exception) {
            Logger.e(exception);
            String message = getString(R.string.download_error);
            String messageContent;
            if (exception instanceof ServerError) {
                messageContent = getString(R.string.download_error_server);
            } else if (exception instanceof NetworkError) {
                messageContent = getString(R.string.download_error_network);
            } else if (exception instanceof StorageReadWriteError) {
                messageContent = getString(R.string.download_error_storage);
            } else if (exception instanceof StorageSpaceNotEnoughError) {
                messageContent = getString(R.string.download_error_space);
            } else if (exception instanceof TimeoutError) {
                messageContent = getString(R.string.download_error_timeout);
            } else if (exception instanceof UnKnownHostError) {
                messageContent = getString(R.string.download_error_un_know_host);
            } else if (exception instanceof URLError) {
                messageContent = getString(R.string.download_error_url);
            } else {
                messageContent = getString(R.string.download_error_un);
            }
            message = String.format(Locale.getDefault(), message, messageContent);

            mFileList.get(what).setTitle(message);
            mLoadFileAdapter.notifyItemInserted(what);
        }

        @Override
        public void onProgress(int what, int progress, long fileCount, long speed) {
            AppConfig.getInstance().putInt(PROGRESS_KEY + what, progress);
            updateProgress(what, progress, speed);
        }

        @Override
        public void onFinish(int what, String filePath) {
            Logger.d("Download finish");

            mFileList.get(what).setTitle(R.string.download_status_finish);
            mLoadFileAdapter.notifyItemChanged(what);
        }

        @Override
        public void onCancel(int what) {
            mFileList.get(what).setTitle(R.string.download_status_be_pause);
        }


        /**
         * 更新进度。
         * @param what 哪个item。
         * @param progress 进度值。
         */
        private void updateProgress(int what, int progress, long speed) {
            mFileList.get(what).setTitle(getSProgress(progress, speed));
            mFileList.get(what).setProgress(progress);
            mLoadFileAdapter.notifyItemChanged(what);
        }
    };

    /**
     * 格式化进度标题。
     *
     * @param progress 进度。
     * @return 直接可以用的标题。
     */
    private String getSProgress(int progress, long speed) {
        double newSpeed = speed / 1024D;
        DecimalFormat decimalFormat = new DecimalFormat("###0.00");
        String sProgress = getString(R.string.download_progress);
        return String.format(Locale.getDefault(), sProgress, progress, decimalFormat.format(newSpeed));
    }

    /**
     * 删除文件
     */
    private void delete() {
        for (int i = 0; i < 4; i++) {
            File file = new File(AppConfig.getInstance().APP_PATH_ROOT, "nohttp_list" + i + ".apk");
            IOUtils.delFileOrFolder(file);

            // 还原页面状态。
            AppConfig.getInstance().putInt(PROGRESS_KEY + i, 0);
            mFileList.get(i).setProgress(0);
            mFileList.get(i).setTitle(getString(R.string.upload_file_status_wait));
            mLoadFileAdapter.notifyItemChanged(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_file_list, menu);
        return true;
    }

    @Override
    protected boolean onOptionsItemSelectedCompat(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_download_file_download) {
            if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                download();
            else
                AndPermission.with(this)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .requestCode(100)
                        .send();
        } else if (itemId == R.id.menu_download_file_delete) {
            if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                delete();
            else
                AndPermission.with(this)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .requestCode(101)
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
                switch (requestCode) {
                    case 100: {
                        download();
                        break;
                    }
                    case 101: {
                        delete();
                        break;
                    }
                }
            }

            @Override
            public void onFailed(int requestCode, List<String> deniedPermissions) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        for (DownloadRequest downloadRequest : mDownloadRequests) {
            downloadRequest.cancel();
        }
        super.onDestroy();
    }
}
