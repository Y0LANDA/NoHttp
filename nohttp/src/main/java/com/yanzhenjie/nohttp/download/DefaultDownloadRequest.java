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

import com.yanzhenjie.nohttp.BasicRequest;
import com.yanzhenjie.nohttp.RequestMethod;

import java.io.File;

/**
 * <p>
 * Download the implementation class of the parameter request, and convert it to the object of the network download.
 * </p>
 * Created in Jul 31, 2015 10:38:10 AM.
 *
 * @author Yan Zhenjie..
 */
public class DefaultDownloadRequest extends BasicRequest implements DownloadRequest {

    /**
     * The callback mark.
     */
    private int what;
    /**
     * The request of the listener.
     */
    private DownloadListener downloadListener;
    /**
     * File the target folder.
     */
    private final String mFileDir;
    /**
     * The file target name.
     */
    private final String mFileName;
    /**
     * According to the Http header named files automatically.
     */
    private final boolean mAutoNameByHead;
    /**
     * If is to download a file, whether the breakpoint continuing.
     */
    private final boolean isRange;
    /**
     * If there is a old files, whether to delete the old files.
     */
    private final boolean isDeleteOld;

    /**
     * Create download request.
     *
     * @param url           url.
     * @param requestMethod {@link RequestMethod}.
     * @param fileFolder    file save folder.
     * @param isDeleteOld   find the same when the file is deleted after download, or on behalf of the download is
     *                      complete, not to request the network.
     * @see #DefaultDownloadRequest(String, RequestMethod, String, String, boolean, boolean)
     */
    public DefaultDownloadRequest(String url, RequestMethod requestMethod, String fileFolder, boolean isDeleteOld) {
        this(url, requestMethod, fileFolder, null, true, false, isDeleteOld);
    }

    /**
     * Create a download object.
     *
     * @param url           download address.
     * @param requestMethod {@link RequestMethod}.
     * @param fileFolder    folder to save file.
     * @param filename      filename.
     * @param isRange       whether the breakpoint continuing.
     * @param isDeleteOld   find the same when the file is deleted after download, or on behalf of the download is
     *                      complete, not to request the network.
     * @see #DefaultDownloadRequest(String, RequestMethod, String, boolean)
     */
    public DefaultDownloadRequest(String url, RequestMethod requestMethod, String fileFolder, String filename,
                                  boolean isRange, boolean isDeleteOld) {
        this(url, requestMethod, fileFolder, filename, false, isRange, isDeleteOld);
    }

    /**
     * Create a download object.
     *
     * @param url            download address.
     * @param requestMethod  {@link RequestMethod}.
     * @param fileFolder     folder to save file.
     * @param filename       filename.
     * @param autoNameByHead according to the Http header named files automatically.
     * @param isRange        whether the breakpoint continuing.
     * @param isDeleteOld    find the same when the file is deleted after download, or on behalf of the download is
     *                       complete, not to request the network.
     */
    private DefaultDownloadRequest(String url, RequestMethod requestMethod, String fileFolder, String filename,
                                   boolean autoNameByHead, boolean isRange, boolean isDeleteOld) {
        super(url, requestMethod);
        this.mFileDir = fileFolder;
        this.mFileName = filename;
        this.mAutoNameByHead = autoNameByHead;
        this.isRange = isRange;
        this.isDeleteOld = isDeleteOld;
    }

    @Override
    public String getFileDir() {
        return this.mFileDir;
    }

    @Override
    public String getFileName() {
        return this.mFileName;
    }

    @Override
    public boolean autoNameByHead() {
        return mAutoNameByHead;
    }

    @Override
    public boolean isRange() {
        return this.isRange;
    }

    @Override
    public boolean isDeleteOld() {
        return this.isDeleteOld;
    }

    @Override
    public int checkBeforeStatus() {
        if (this.isRange) {
            try {
                File lastFile = new File(mFileDir, mFileName);
                if (lastFile.exists() && !isDeleteOld)
                    return STATUS_FINISH;
                File tempFile = new File(mFileDir, mFileName + ".nohttp");
                if (tempFile.exists())
                    return STATUS_RESUME;
            } catch (Exception e) {
            }
        }
        return STATUS_RESTART;
    }

    @Override
    public void onPreResponse(int what, DownloadListener downloadListener) {
        this.what = what;
        this.downloadListener = downloadListener;
    }

    @Override
    public int what() {
        return what;
    }

    @Override
    public DownloadListener downloadListener() {
        return downloadListener;
    }
}
