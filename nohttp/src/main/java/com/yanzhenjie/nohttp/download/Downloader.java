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
package com.yanzhenjie.nohttp.download;

import android.text.TextUtils;
import android.util.Log;

import com.yanzhenjie.nohttp.Connection;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NetworkExecutor;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.ServerError;
import com.yanzhenjie.nohttp.error.StorageReadWriteError;
import com.yanzhenjie.nohttp.error.StorageSpaceNotEnoughError;
import com.yanzhenjie.nohttp.error.URLError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.tools.HeaderUtil;
import com.yanzhenjie.nohttp.tools.IOUtils;
import com.yanzhenjie.nohttp.tools.NetUtil;
import com.yanzhenjie.nohttp.HttpConnection;
import com.yanzhenjie.nohttp.error.TimeoutError;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.UnknownHostException;

/**
 * <p>
 * The network layer to download missions.
 * </p>
 * Created in Jul 31, 2015 9:11:55 AM.
 *
 * @author Yan Zhenjie.
 */
public class Downloader {

    private HttpConnection mHttpConnection;

    public Downloader(NetworkExecutor executor) {
        this.mHttpConnection = new HttpConnection(executor);
    }

    private void validateParam(DownloadRequest downloadRequest, DownloadListener downloadListener) {
        if (downloadRequest == null)
            throw new IllegalArgumentException("DownloadRequest == null.");
        if (downloadListener == null)
            throw new IllegalArgumentException("DownloadListener == null.");
    }

    private void validateDevice(String savePathDir) throws Exception {
        if (!NetUtil.isNetworkAvailable())
            throw new NetworkError("Network is not available, please check network and permission: INTERNET, " +
                    "ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE.");

        if (!IOUtils.createFolder(savePathDir))
            throw new StorageReadWriteError("SD isn't available, please check SD card and permission: " +
                    "WRITE_EXTERNAL_STORAGE, and you must pay attention to Android6.0 RunTime Permissions: " +
                    "https://github.com/yanzhenjie/AndPermission.");
    }

    private long handleRange(File tempFile, DownloadRequest downloadRequest) {
        if (tempFile.exists()) {
            if (tempFile.isDirectory())
                IOUtils.delFileOrFolder(tempFile);

            if (downloadRequest.isRange() && tempFile.exists()) {
                long rangeSize = tempFile.length();
                if (rangeSize > 0)
                    // 例如：从1024开始下载：Range:bytes=1024-。
                    downloadRequest.setHeader("Range", "bytes=" + rangeSize + "-");
                return rangeSize;
            } else {
                downloadRequest.removeHeader("Range"); // Fix developer add.
                IOUtils.delFileOrFolder(tempFile);
            }
        }
        return 0;
    }

    public void download(int what, DownloadRequest downloadRequest, DownloadListener downloadListener) {
        validateParam(downloadRequest, downloadListener);

        Connection connection = null;
        RandomAccessFile randomAccessFile = null;
        String savePathDir = downloadRequest.getFileDir();
        String fileName = downloadRequest.getFileName();
        try {
            if (TextUtils.isEmpty(savePathDir))
                savePathDir = NoHttp.getContext().getFilesDir().getAbsolutePath();

            validateDevice(savePathDir);

            if (TextUtils.isEmpty(fileName))// auto named.
                fileName = Long.toString(System.currentTimeMillis());

            File tempFile = new File(savePathDir, fileName + ".nohttp");
            long rangeSize = handleRange(tempFile, downloadRequest);// 断点开始处。


            // 连接服务器。
            connection = mHttpConnection.getConnection(downloadRequest);
            Exception exception = connection.exception();
            if (exception != null)
                throw exception;

            Logger.i("----------Response Start----------");
            Headers responseHeaders = connection.responseHeaders();
            int responseCode = responseHeaders.getResponseCode();

            // getList filename from server.
            if (downloadRequest.autoNameByHead()) {
                String contentDisposition = responseHeaders.getContentDisposition();
                if (!TextUtils.isEmpty(contentDisposition)) {
                    fileName = HeaderUtil.parseHeadValue(contentDisposition, "filename", null);
                    if (!TextUtils.isEmpty(fileName)) {
                        try {
                            fileName = URLDecoder.decode(fileName, downloadRequest.getParamsEncoding());
                        } catch (UnsupportedEncodingException e) {
                            // Do nothing.
                        }
                        if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                            fileName = fileName.substring(1, fileName.length() - 1);
                        }
                    }
                }

                // From url.
                if (TextUtils.isEmpty(fileName)) {
                    String tempUrl = downloadRequest.url();
                    String[] slash = tempUrl.split("/");
                    fileName = slash[slash.length - 1];
                    int paramIndex = fileName.indexOf("?");
                    if (paramIndex > 0) {
                        fileName = fileName.substring(0, paramIndex);
                    }
                }
            }

            InputStream serverStream = connection.serverStream();
            if (responseCode >= 400) {
                ServerError error = new ServerError("Download failed, the server response code is " + responseCode +
                        ": " + downloadRequest.url());
                error.setErrorBody(IOUtils.toString(serverStream));
                throw error;
            } else {
                long contentLength;
                // 文件总大小
                if (responseCode == 206) {
                    // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]。
                    String range = responseHeaders.getContentRange(); // Sample：Accept-Range:bytes 1024-2047/2048。
                    try {
                        contentLength = Long.parseLong(range.substring(range.indexOf('/') + 1));// 截取'/'之后的总大小。
                    } catch (Throwable e) {
                        throw new ServerError("ResponseCode is 206, but content-Range error in Server HTTP header " +
                                "information: " + range + ".");
                    }
                } else if (responseCode == 304) {
                    int httpContentLength = responseHeaders.getContentLength();
                    downloadListener.onStart(what, true, httpContentLength, responseHeaders, httpContentLength);
                    downloadListener.onProgress(what, 100, httpContentLength, 0);
                    Logger.d("-------Download finish-------");
                    downloadListener.onFinish(what, savePathDir + File.separator + fileName);
                    return;
                } else { // such as: 200.
                    rangeSize = 0L; // 服务器不支持Range。
                    contentLength = responseHeaders.getContentLength();// 直接下载。
                }

                // 验证文件已经存在。
                File lastFile = new File(savePathDir, fileName);
                if (lastFile.exists()) {
                    if (downloadRequest.isDeleteOld())
                        IOUtils.delFileOrFolder(lastFile);
                    else {
                        downloadListener.onStart(what, true, lastFile.length(), responseHeaders, lastFile.length());
                        downloadListener.onProgress(what, 100, lastFile.length(), 0);
                        Logger.d("-------Download finish-------");
                        downloadListener.onFinish(what, lastFile.getAbsolutePath());
                        return;
                    }
                }

                if (IOUtils.getDirSize(savePathDir) < contentLength)
                    throw new StorageSpaceNotEnoughError("The folder is not enough space to save the downloaded file:" +
                            " " + savePathDir + ".");

                // 需要重新下载，生成临时文件。
                if (responseCode != 206 && !IOUtils.createNewFile(tempFile))
                    throw new StorageReadWriteError("SD isn't available, please check SD card and permission: " +
                            "WRITE_EXTERNAL_STORAGE, and you must pay attention to Android6.0 RunTime " +
                            "Permissions: https://github.com/yanzhenjie/AndPermission.");

                if (downloadRequest.isCanceled()) {
                    Log.w("NoHttpDownloader", "Download request is canceled.");
                    downloadListener.onCancel(what);
                    return;
                }

                // 通知开始下载了。
                Logger.d("-------Download start-------");
                downloadListener.onStart(what, rangeSize > 0, rangeSize, responseHeaders, contentLength);

                randomAccessFile = new RandomAccessFile(tempFile, "rws");
                randomAccessFile.seek(rangeSize);

                byte[] buffer = new byte[8096];
                int len;

                int oldProgress = 0;// 旧的进度记录，防止重复通知。
                long count = rangeSize;// 追加目前已经下载的进度。

                long startTime = System.currentTimeMillis();
                long speedCount = 0;
                long oldSpeed = 0;

                while (((len = serverStream.read(buffer)) != -1)) {
                    if (downloadRequest.isCanceled()) {
                        Log.i("NoHttpDownloader", "Download request is canceled.");
                        downloadListener.onCancel(what);
                        break;
                    } else {
                        randomAccessFile.write(buffer, 0, len);

                        count += len;
                        speedCount += len;

                        long time = System.currentTimeMillis() - startTime;

                        long speed = speedCount * 1000 / time;

                        boolean speedChanged = oldSpeed != speed && time >= 300;

                        Logger.i("speedCount: " + speedCount + "; time: " + time + "; speed: " + speed + "; changed: " +
                                "" + speedChanged);

                        if (contentLength != 0) {
                            int progress = (int) (count * 100 / contentLength);
                            if (progress != oldProgress && speedChanged) {
                                downloadListener.onProgress(what, progress, count, speed);

                                speedCount = 0;
                                oldSpeed = speed;
                                startTime = System.currentTimeMillis();
                            } else if (speedChanged) {
                                downloadListener.onProgress(what, oldProgress, count, speed);

                                speedCount = 0;
                                oldSpeed = speed;
                                startTime = System.currentTimeMillis();
                            } else if (progress != oldProgress) {
                                downloadListener.onProgress(what, progress, count, oldSpeed);
                            }
                            oldProgress = progress;
                        } else if (speedChanged) {
                            downloadListener.onProgress(what, 0, count, speed);

                            speedCount = 0;
                            oldSpeed = speed;
                            startTime = System.currentTimeMillis();
                        } else {
                            downloadListener.onProgress(what, 0, count, oldSpeed);
                        }
                    }
                }
                if (!downloadRequest.isCanceled()) {
                    //noinspection ResultOfMethodCallIgnored
                    tempFile.renameTo(lastFile);
                    Logger.d("-------Download finish-------");
                    downloadListener.onFinish(what, lastFile.getAbsolutePath());
                }
            }
        } catch (MalformedURLException e) {
            Logger.e(e);
            downloadListener.onDownloadError(what, new URLError(e.getMessage()));
        } catch (UnknownHostException e) {
            Logger.e(e);
            downloadListener.onDownloadError(what, new UnKnownHostError(e.getMessage()));
        } catch (SocketTimeoutException e) {
            Logger.e(e);
            downloadListener.onDownloadError(what, new TimeoutError(e.getMessage()));
        } catch (IOException e) {
            Exception newException = e;
            if (!IOUtils.canWrite(savePathDir))
                newException = new StorageReadWriteError("This folder cannot be written to the file: " + savePathDir
                        + ".");
            else if (IOUtils.getDirSize(savePathDir) < 1024)
                newException = new StorageSpaceNotEnoughError("The folder is not enough space to save the downloaded " +
                        "file: " + savePathDir + ".");
            Logger.e(newException);
            downloadListener.onDownloadError(what, newException);
        } catch (Exception e) {// NetworkError | ServerError | StorageCantWriteError | StorageSpaceNotEnoughError
            if (!NetUtil.isNetworkAvailable())
                e = new NetworkError("The network is not available.");
            Logger.e(e);
            downloadListener.onDownloadError(what, e);
        } finally {
            Logger.i("----------Response End----------");
            IOUtils.closeQuietly(randomAccessFile);
            IOUtils.closeQuietly(connection);
        }
    }

}
