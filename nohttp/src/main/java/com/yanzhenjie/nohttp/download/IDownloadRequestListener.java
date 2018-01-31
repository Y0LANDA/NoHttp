package com.yanzhenjie.nohttp.download;

/**
 * Project Name: NoHttp
 * Package Name:com.yanzhenjie.nohttp.download
 * Author: zhangnn
 * Create Time: 2018/1/31 下午 9:00
 * Remark: describe this class purpose
 * Note:监听下载请求的执行状态
 */

public interface IDownloadRequestListener {

    /**
     * 请求开始执行
     *
     * @param what 请求的唯一标识
     */
    void onStart(int what);

    /**
     * 请求正常结束
     *
     * @param what 请求的唯一标识
     */
    void onFinish(int what);

    /**
     * 请求出错
     *
     * @param what 请求的唯一标识
     */
    void onError(int what);
}
