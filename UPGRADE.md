# NoHttp升级日志
## 1.1.1
 1. 修改包名为`com.yanzhenjie.com`，开发者从旧版升级后会发生编译错误，请使用全局替换，将`com.yolanda.nohttp`替换为`com.yanzhenjie.nohttp`即可。
 因此依赖方式也由`com.yolanda.nohttp:nohttp:version`变为`com.yanzhenjie.nohttp:nohttp:version`
 2. 新增一种非队列的异步请求方式，调用后会立即发起请求：
 ```java
 Request<String> req = NoHttp.createStringRequest(url, RequestMethod.GET);
 AsyncRequestExecutor.INSTANCE.execute(0, req, new OnResponseListener<String>() {
        @Override
        public void onStart(int what) {
        }

        @Override
        public void onSucceed(int what, Response<String> response) {
        }

        @Override
        public void onFailed(int what, Response<String> response) {
        }

        @Override
        public void onFinish(int what) {
        }
 });
 ```
 3. 修复StringRequest中按照服务器ContentType指定编码集解码（原来是由于解析contentType错误，默认utf-8解码）。
 4. 优化缓存逻辑，在不使用缓存的时候可提升请求速度与性能。
 5. 下载模块增加回调下载速度，具体可以参考[Demo](https://github.com/yanzhenjie/NoHttp)或者[使用文档](http://doc.nohttp.net)
 ```java
 void onProgress(int what, int progress, long fileCount, long speed);
 ```
 6. 兼容服务器要求重定向时Location中返回不完整url的问题。
 7. 升级okhttp为3.6.0。
 8. Demo中添加6.0运行时权限管理，使用我的另一个权限管理库：[AndPermission](https://github.com/yanzhenjie/AndPermission)