![NoHttp Logo](http://www.nohttp.net/image/nohttp_logo.svg)  

支持与`RxJava`完美结合、支持一句话切换底层为`OkHttp`，支持缓存数据到数据库或SD卡（缓存数据自动加密）支持请求Restful风格的接口，比Retrofit更简单易用。  

**NoHttp使用文档：[doc.nohttp.net](http://doc.nohttp.net)**  
**NoHttp测试接口：[api.nohttp.net](http://api.nohttp.net)**  
**欢迎加入QQ技术交流群：[46523908](https://jq.qq.com/?_wv=1027&k=44uOijU)**  

**[查阅NoHttp升级日志](https://github.com/yanzhenjie/NoHttp/blob/master/UPGRADE.md)**

Demo中用的6.0权限管理友链：[https://github.com/yanzhenjie/AndPermission](https://github.com/yanzhenjie/AndPermission)

----

## 导航
- [Demo效果预览](#效果预览)  
- [NoHttp特性](#框架特性)  
- [AndroidStuio、Eclipse使用方法](#使用方法)  
- [NoHttp初始化](#初始化)  
- [需要的权限](#需要的权限)  
- [友好的调试模式](#友好的调试模式)  
- [RxJava](#第三方异步框架)  
- [请求队列](#请求队列)  
- [String、Bitmap、JavaBean、Json请求](#请求类型)  
- [添加参数，可以链式调用](#添加参数)  
- [提交Json、XML、自定义Body等](#提交请求包体)  
- [同步请求](#同步请求)  
- [五大缓存模式](#五大缓存模式)  
- [文件下载](#文件下载)  
- [如何取消请求](#取消请求)  
- [停止队列](#停止队列)  
- [自定义请求](#自定义请求)  
- [代码混淆](#代码混淆)  

## 效果预览
<image src="https://github.com/yanzhenjie/NoHttp/blob/master/image/1.gif?raw=true" width="200px"/>  <image src="https://github.com/yanzhenjie/NoHttp/blob/master/image/2.gif?raw=true" width="200px"/>

## 框架特性
比Retrofit使用更简单、更易用。

* 动态配置底层框架为OkHttp、HttpURLConnection
* 与RxJava完美结合，支持异步请求、支持同步请求
* 多文件上传，支持大文件上传，表单提交数据
* 文件下载、上传下载、上传和下载的进度回调、错误回调
* 支持Json、xml、Map、List的提交
* 完美的Http缓存模式，可指定缓存到数据库、SD卡，缓存数据已安全加密
* 自定义Request，直接请求JsonObject、JavaBean等
* Cookie的自动维持，App重启、关开机后还持续维持
* http 301 302 303 304 307重定向，支持多层嵌套重定向
* Https、自签名网站Https的访问、支持双向验证
* 失败重试机制，支持请求优先级
* GET、POST、PUT、PATCH、HEAD、DELETE、OPTIONS、TRACE等请求协议
* 用队列保存请求，平均分配多线程的资源，支持多个请求并发
* 支持取消某个请求、取消指定多个请求、取消所有请求

## 使用方法
### Gradle
* 如果使用HttpURLConnection作为网络层：
```groovy
compile 'com.yanzhenjie.nohttp:nohttp:1.1.1'
```
* 如果要使用OkHttp作为网络层，请再依赖：
```groovy
compile 'com.yanzhenjie.nohttp:okhttp:1.1.1'
```

> 新版NoHttp修改了包名为`com.yanzhenjie.nohttp`，开发者从旧版升级后请使用全局替换，将`com.yolanda.nohttp`替换为`com.yanzhenjie.nohttp`即可。

### Eclipse ADT
1. 放弃治疗。  
2. 自行下载上方jar包。  

## 初始化
NoHttp初始化需要一个Context，最好在`Application`的`onCreate()`中初始化，记得在`manifest.xml`中注册`Application`。

### 一般初始化
直接初始化后，一切采用默认设置。
```java
NoHttp.initialize(this);
```

### 高级自定义初始化

* 配置超时毫秒数，默认10 * 1000ms
```java
NoHttp.initialize(this, new NoHttp.Config()
    // 设置全局连接超时时间，单位毫秒
    .setConnectTimeout(30 * 1000)
    // 设置全局服务器响应超时时间，单位毫秒
    .setReadTimeout(30 * 1000)
);
```

* 配置缓存，控制开关
```java
NoHttp.initialize(this, new NoHttp.Config()
    ...
    .setCacheStore(
        // 保存到数据库
        new DBCacheStore(this).setEnable(true) // 如果不使用缓存，设置false禁用。
        // 或者保存到SD卡：new DiskCacheStore(this)
    )
);
```

* 配置Cookie保存的位置，默认保存在数据库
```java
NoHttp.initialize(this, new NoHttp.Config()
    ...
    // 默认保存数据库DBCookieStore，开发者也可以自己实现CookieStore接口。
    .setCookieStore(
        new DBCookieStore(this).setEnable(false) // 如果不维护cookie，设置false禁用。
    )
);
```

* 配置网络层
```java
NoHttp.initialize(this, new NoHttp.Config()
    ...
    // 使用HttpURLConnection
    .setNetworkExecutor(new URLConnectionNetworkExecutor())
    // 或者使用OkHttp
    // .setNetworkExecutor(new OkHttpNetworkExecutor())
);
```

## 需要的权限
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## 友好的调试模式
```java
Logger.setDebug(true);// 开启NoHttp的调试模式, 配置后可看到请求过程、日志和错误信息。
Logger.setTag("NoHttpSample");// 设置NoHttp打印Log的tag。
```
开启NoHttp的调试模式后可看到请求过程、日志和错误信息，基本不用抓包。可以看到请求头、请求数据、响应头、Cookie等，而且打印出的Log非常整齐。

所以说，如果你使用过程中遇到什么问题了，开启调试模式，一切妖魔鬼怪都会现形的。

##第三方异步框架
**RxJava**
其实核心就是集合`RxJava`的异步功能和`NoHttp`的同步功能：
```
Request<String> request = NoHttp.createStringRequest(url, RequestMethod.DELETE);
Response<String> response = NoHttp.startRequestSync(request);
if (response.isSucceed()) {
    // 请求成功
} else {
    // 请求失败
}
```
把上面的代码利用`RxJava`封装起来就OK了，这两个项目可以作为参考：
1. [NoHttpUtil](https://git.oschina.net/ysb/NoHttpUtil)  
2. [NohttpRxUtils](https://github.com/LiqiNew/NohttpRxUtils)  

## 请求队列
```java
RequestQueue requestQueue = NoHttp.newRequestQueue();
// 如果要指定并发值，传入数字即可：NoHttp.newRequestQueue(3);

// 发起请求
requestQueue.add(what, request, responseListener);
```
* 添加请求到队列时有一个what，这个what会在`responseLisetener`响应时回调给开发者，所以开发者可以用一个`responseLisetener`接受多个请求的响应，用what来区分结果。而不用像有的框架一样，每一个请求都要new一个callback。  
* **强烈建议**把生成队列写成懒汉单例模式，因为每新建队列就会new出相应个数的线程来，同时只有线程数固定了，队列的作用才会发挥到最大。

##请求类型
## String请求
```java
Request<String> request = NoHttp.createStringRequest(url, RequestMethod.GET);
requestQueue.add(0, request, listener);
```

## Json请求
```java
// JsonObject
Request<JSONObject> objRequest = NoHttp.createJsonObjectRequest(url, RequestMethod.POST);
requestQueue.add(0, objRequest, listener);

// JsonArray
Request<JSONArray> arrayRequest = NoHttp.createJsonArrayRequest(url, RequestMethod.PUT);
requestQueue.add(0, arrayRequest, listener);
```

## Bitmap请求
```	java
Request<Bitmap> request = NoHttp.createImageRequest(url, RequestMethod.DELETE);
requestQueue.add(0, request, listener);
```

## 请求FastJson与Gson
```java
// FastJson
Request<JSONObject> request = new FastJsonRequest(url, RequestMethod.POST);
requestQueue.add(0, request, listener);
```

## 直接请求JavaBean
```java
// 内部使用Gson、FastJson解析成JavaBean
Request<UserInfo> request = new JavaBeanRequest(url, RequestMethod.GET);
requestQueue.add(0, request, listener);
```

## 添加参数
```java
Request<JSONObject> request = new JavaBeanRequest(url, RequestMethod.POST);
   .add("name", "yoldada") // String类型
   .add("age", 18) // int类型
   .add("sex", '0') // char类型
   .add("time", 16346468473154) // long类型

   // 添加Bitmap
   .add("head", new BitmapBinary(bitmap))
   // 添加File
   .add("head", new FileBinary(file))
   // 添加ByteArray
   .add("head", new ByteArrayBinary(byte[]))
   // 添加InputStream
   .add("head", new InputStreamBinary(inputStream));
```

文件上传实现了http表单的标准协议，满足了广大开发者的需求，有以下几种形式：

* 单个文件
```java
Request<String> request = ...
request.add("file", new FileBinary(file));
```

* 上传多个文件、多个Key多个文件形式  
这里可以添加各种形式的文件，File、Bitmap、InputStream、ByteArray。

```java
Request<String> request = ...
request.add("file1", new FileBinary(File));
request.add("file2", new FileBinary(File));
request.add("file3", new InputStreamBinary(InputStream));
request.add("file4", new ByteArrayBinary(byte[]));
request.add("file5", new BitmapBinary(Bitmap));
```

* 上传多个文件、一个Key多个文件形式
```java
Request<String> request = ...
fileList.add("image", new FileBinary(File));
fileList.add("image", new InputStreamBinary(InputStream));
fileList.add("image", new ByteArrayBinary(byte[]));
fileList.add("image", new BitmapBinary(Bitmap));
```

或者：  
```java
Request<String> request = ...

List<Binary> fileList = ...
fileList.add(new FileBinary(File));
fileList.add(new InputStreamBinary(InputStream));
fileList.add(new ByteArrayBinary(byte[]));
fileList.add(new BitmapStreamBinary(Bitmap));
request.add("file_list", fileList);
```

## 提交请求包体
提交Body分为提交Json、提交String、提交Xml、提交流等，具体用法如下：
```java
// 提交普通String
request.setDefineRequestBody(String, ContentType);

// 提交json字符串
request.setDefineRequestBodyForJson(JsonString)

// 提交jsonObject对象，其实还是json字符串
request.setDefineRequestBodyForJson(JSONObject)

// 提交xml字符串
request.setDefineRequestBodyForXML(XmlString)

// 提交字体Body，比如File（这跟表单上传不一样的），可以转为InputStream来提交
request.setDefineRequestBody(InputStream, ContentType)
```

## 同步请求
在当前线程发起请求，在线程这么使用。
```java
Request<String> request = NoHttp.createStringRequest(url, RequestMethod.DELETE);
Response<String> response = NoHttp.startRequestSync(request);
if (response.isSucceed()) {
    // 请求成功
} else {
    // 请求失败
}
```

## 五大缓存模式
NoHttp的缓存非常强大，支持缓存到数据库、换到SD卡等，并且不论缓存在数据库或者SD，NoHttp都把数据进行了加密，需要在初始化的时候配置缓存的位置。

需要注意的是，在6.0以上的手机中如果要缓存在SD卡，需要在请求之前，需要请求运行时权限，如果你不懂运行时权限，可以看这个项目：[AndPermission](https://github.com/yanzhenjie/AndPermission)。
```java
NoHttp.initialize(this, new NoHttp.Config()
    ...
    // 保存到数据库
    .setCacheStore(
        new DBCacheStore(this).setEnable(true) // 如果不使用缓存，设置false禁用。
    )
    // 或者保存到SD卡
    .setCacheStore(
        new DiskCacheStore(this)
    )
);
```


* 1、Default模式，实现http 304重定向缓存
NoHttp本身是实现了RFC2616，所以这里不用设置或者设置为DEFAULT。
```java
Request<JSONObject> request = NoHttp.createJsonObjectRequest(url);
request.setCacheMode(CacheMode.DEFAULT);
```

* 2、 当请求服务器失败的时候，读取缓存
请求服务器成功则返回服务器数据，如果请求服务器失败，读取缓存数据返回。
```java
Request<JSONObject> request = NoHttp.createJsonObjectRequest(url);
request.setCacheMode(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE);
```

* 3、如果发现有缓存直接成功，没有缓存才请求服务器
我们知道ImageLoader的核心除了内存优化外，剩下一个就是发现把内地有图片则直接使用，没有则请求服务器，所以NoHttp这一点非常使用做一个ImageLoader。  

请求String，缓存String：
```java
Request<JSONObject> request = NoHttp.createJsonObjectRequest(url);
// 非标准Http协议，改变缓存模式为IF_NONE_CACHE_REQUEST_NETWORK
request.setCacheMode(CacheMode.IF_NONE_CACHE_REQUEST_NETWORK);
```

请求图片，缓存图片：
```java
Request<Bitmap> request = NoHttp.createImageRequest(imageUrl);
request.setCacheMode(CacheMode.IF_NONE_CACHE_REQUEST_NETWORK);
```

* 4、仅仅请求网络
这里不会读取缓存，也不支持Http304。
```java
Request<Bitmap> request = NoHttp.createImageRequest(imageUrl);
request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
...
```

* 5、仅仅读取缓存
仅仅读取缓存，不会请求网络和其它操作。
```java
Request<Bitmap> request = NoHttp.createImageRequest(imageUrl);
request.setCacheMode(CacheMode.ONLY_READ_CACHE);
```

## 文件下载
因为下载文件代码比较多，这里贴关键部分，具体的请参考demo。  

文件下载也是队列，队列和开头所说的请求的队列是一样的。

- 发起下载请求
```java
//下载文件
downloadRequest = NoHttp.createDownloadRequest...
// what 区分下载
// downloadRequest 下载请求对象
// downloadListener 下载监听
downloadQueue.add(0, downloadRequest, downloadListener);
```

- 暂停或者停止下载
```java
downloadRequest.cancel();
```

- 监听下载过程
```java
private DownloadListener downloadListener = new DownloadListener() {
	@Override
	public void onStart(int what, boolean resume, long preLenght, Headers header, long count) {
	    // 下载开始
	}

	@Override
	public void onProgress(int what, int progress, long downCount, long speed) {
		// 更新下载进度和下载网速
	}

 	@Override
	public void onFinish(int what, String filePath) {
	    // 下载完成
 	}

	@Override
	public void onDownloadError(int what, StatusCode code, CharSequence message) {
	    // 下载发生错误
	}

	@Override
	public void onCancel(int what) {
	    // 下载被取消或者暂停
	}
};
```

## 取消请求
NoHttp支持取消某个请求、取消指定多个请求、取消所有请求。

* 取消单个请求  
直接调用请求对象的cancel方法。
```java
request.cancel();
```

* 从队列中取消指定的请求
在请求之前给请求set一个sign，取消的时候调用队列的cancelBySign就可以取消掉所有指定这个sign的请求。
```java
request1.setCancelSign(sign);
request2.setCancelSign(sign);
...

// 取消队列中多个用sign标志的请求
queue.cancelBySign(sign);
```

* 取消队列中所有请求
```java
queue.cancelAll();
```

## 停止队列
队列停止后再添加请求到队列后，请求不会被执行。
```java
RequestQueue queue = NoHttp.newRequestQueue();
...

queue.stop();
```

## 自定义请求
NoHttp的所有自带请求都是继承`RestRequest`类，所以我们自定义请求也需要继承`RestRequest`，泛型写自己想要请求的数据类型，最后在`parseResponse()`方法中解析服务器数据成自己自己想要的数据类型即可。
* FastJsonRequest
```java
public class FastJsonRequest extends RestRequestor<JSONObject> {

    public FastJsonRequest(String url) {
	    this(url, RequestMethod.GET);
    }

    public FastJsonRequest(String url, RequestMethod requestMethod) {
	    super(url, requestMethod);
    }

    @Override
    public JSONObject parseResponse(Headers header, byte[] body) throws Throwable {
	    String result = StringRequest.parseResponseString(headers, body);
	    return JSON.parseObject(result);
    }
}
```

* JavaBeanRequest，利用FastJson、Gson等把数据直接转为JavaBean
```java
public class JavaBeanRequest<T> extends RestRequest<T> {
    private Class<T> clazz;

    public JavaBeanRequest(String url, Class<T> clazz) {
        this(url, RequestMethod.GET, clazz);
    }

    public JavaBeanRequest(String url, RequestMethod requestMethod, Class<T> clazz) {
        super(url, requestMethod);
        this.clazz = clazz;
    }

    @Override
    public T parseResponse(Headers header, byte[] body) throws Throwable {
        String response = StringRequest.parseResponseString(header, body);

        // 这里如果数据格式错误，或者解析失败，会在失败的回调方法中返回 ParseError 异常。
        return JSON.parseObject(response, clazz);
    }
}
```

* 使用自定义请求
```java
// 使用FastJson自定义请求
Request<JSONObject> request = new FastJsonRequest(url, requestMethod);
queue.add(what, mRequest, listener);

...

// 直击请求JavaBean
Request<UserInfo> request = new JavaBeanRequest(url, UserInfo.class);
queue.add(what, request, listener);
```

## 代码混淆
NoHttp设计到兼容高版本系统的api采用反射调用，所以所有类都可以被混淆，如果你非要keep的话，如下配置即可。

* 原生NoHttp混淆
```text
-dontwarn com.yanzhenjie.nohttp.**
-keep class com.yanzhenjie.nohttp.**{*;}
```
* 如果使用okhttp的版本
```text
// nohttp
-dontwarn com.yanzhenjie.nohttp.**
-keep class com.yanzhenjie.nohttp.**{*;}

// nohttp-okhttp
-dontwarn com.yanzhenjie.nohttp.**
-keep class com.yanzhenjie.nohttp.**{*;}

// okhttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *;} 
-dontwarn okio.**
-keep class okio.** { *;} 
```

## 关于我
![微信二维码](http://img.blog.csdn.net/20161020083048694)

1. 关注我的[Github](https://github.com/yanzhenjie)，了解我的最新项目。

2. 关注[我的博客](http://blog.yanzhenjie.com)，阅读我的最新文章。
3. 关注[我的微博](http://weibo.com/yanzhenjieit)，有问题随时沟通。

## License
```text
Copyright 2016 Yan Zhenjie

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
