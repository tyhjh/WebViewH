# Android WebView与JavaScript的交互

标签（空格分隔）： Android

---

之前有写过WebView的小demo，之后一直没有在项目中使用过网页开发，最近准备重新再看一下，记录一些基本的使用方法

> 相关链接：[Android使用JsBridge与JavaScript交互](https://www.jianshu.com/p/4ed80af1c103)

### JavaScript调用Android方法
>* 第一种是用webView的`JavascriptInterface`注解进行对象映射
>* 第二种是通过webViewClient的`shouldOverrideUrlLoading()`方法拦截URL
>* 第三种是通过WebChromeClient的`onJsAlert()`、`onJsConfirm()`、`onJsPrompt()`来拦截JS对话框`alert()`、`confirm()`;



#### 对象映射
比较简单，在Android的对象里面申明一些方法，暴露给JavaScript，传递这个对象给JavaScript，JavaScript就可以调用这些方法
```java
    webView = findViewById(R.id.wbView);
    //支持JavaScript
    webView.getSettings().setJavaScriptEnabled(true);
    //加载本地HTML文件
    webView.loadUrl("file:///android_asset/test.html");
    //传递对象给JavaScript
    webView.addJavascriptInterface(MainActivity.this, "activity");


    /**
     * 暴露给JavaScript的方法
     *
     * @param msg
     */
    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

```
HTML代码，放在assets文件下的HTML
```html
<html>
<head>
    <title>js调用android原生代码</title>
    <meta http-equiv="Content-Type" content="text/html;charset=gb2312">
    <meta id="viewport" name="viewport"
          content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,minimal-ui">
</head>
<body>
<br/>
<li><a onclick="activity.showToast('你好呀');">点击调用Toast</a></li>
<br/>
</body>
</html>
```
##### 调用方法返回数据
返回数据肯定和刚才那个是一样的，只是方法有返回值而已，只是测试的时候，因为我们不怎么会JavaScript呀，不知道HTML怎么使用这个JavaScript返回来的值，所以有些难搞；我试了两种方法证明的确拿到这个值了，其实怎么用我感觉也不用我们关心，我是找到一个类似Toast的JavaScript方法，用它展示获取到的返回值；
```java
    /**
     * 返回信息给JavaScript
     *
     * @return
     */
    @JavascriptInterface
    public String getMsg() {
        return "Hello from Android";
    }
```
同样还是暴露一个方法给JavaScript，然后第一种方法是在HTML中获取到数据，然后调用HTML中的JavaScript显示
```html
<!--一个类似Toast的JavaScript方法，不用管怎么搞的-->
<script>
    function toast(msg,duration){
        duration=isNaN(duration)?3000:duration;
        var m = document.createElement('div');
        m.innerHTML = msg;
        m.style.cssText="width: 60%;min-width: 150px;opacity: 0.7;height: 30px;color: rgb(255, 255, 255);line-height: 30px;text-align: center;border-radius: 5px;position: fixed;top: 40%;left: 20%;z-index: 999999;background: rgb(0, 0, 0);font-size: 12px;";
        document.body.appendChild(m);
        setTimeout(function() {
            var d = 0.5;
            m.style.webkitTransition = '-webkit-transform ' + d + 's ease-in, opacity ' + d + 's ease-in';
            m.style.opacity = '0';
            setTimeout(function() { document.body.removeChild(m) }, d * 1000);
        }, duration);
    }
</script>

<!--调用Android的getMsg()方法获取返回值，方法前面这个window好像可以加可以不加-->
<li><a onclick="toast(window.activity.getMsg(),100)">点击获取MSG</a></li>
```

第二种就是直接在JavaScript中获取
```html
<!--一个类似Toast的JavaScript方法，我小改了一下，调用了Android的getMsg()方法-->
<script>
    function toast(duration){
    <!--在这里获取返回值-->
        var msg=window.activity.getMsg();
        duration=isNaN(duration)?3000:duration;
        var m = document.createElement('div');
        m.innerHTML = msg;
        m.style.cssText="width: 60%;min-width: 150px;opacity: 0.7;height: 30px;color: rgb(255, 255, 255);line-height: 30px;text-align: center;border-radius: 5px;position: fixed;top: 40%;left: 20%;z-index: 999999;background: rgb(0, 0, 0);font-size: 12px;";
        document.body.appendChild(m);
        setTimeout(function() {
            var d = 0.5;
            m.style.webkitTransition = '-webkit-transform ' + d + 's ease-in, opacity ' + d + 's ease-in';
            m.style.opacity = '0';
            setTimeout(function() { document.body.removeChild(m) }, d * 1000);
        }, duration);
    }
</script>

<!--直接调用JavaScript方法就完事儿了-->
<li><a onclick="toast(100)">点击获取MSG</a></li>
```

#### 拦截URL
JavaScript简单的发送一些消息
```html
<script>
    function callAndroid(){
            <!--约定的url协议为：js://webview?name=Tyhj-->
            document.location = "js://webview?name=Tyhj";
         }
</script>
<li><a onclick="callAndroid()">点击测试拦截URL</a></li>
```
Android通过`webView.setWebViewClient()`重写shouldOverrideUrlLoading方法，拦截到URL，对协议进行解析，获取信息，从而响应JavaScript发送的数据
```java
public static final String URL_SCHEME = "js";
public static final String URL_AUTHORITY = "webview";
webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                //判断协议，约定的url协议为：js://webview?name=Tyhj
                if (URL_SCHEME.equals(uri.getScheme()) && URL_AUTHORITY.equals(uri.getAuthority())) {
                    String name = uri.getQueryParameter("name");
                    Toast.makeText(MainActivity.this, "JavaScript通过拦截调用Android代码" + name, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
```

#### 拦截JS对话框
正常网页是会弹出弹窗让我们点击操作或者输入操作返回一些值；现在我们监听到之后返回true而不是默认的方法，这时候就是拦截了这些对话框，网页就不会弹出来，但是也需要我们做响应的处理，并且返回值给JavaScript；

JavaScript简单的弹出对话窗
```html
<li><a onclick="alert('alert测试')">点击测alert</a></li>
<li><a onclick="showPrompt()">点击测prompt</a></li>
<li><a onclick="showConfirm()">点击测confirm</a></li>
<script>
    function showPrompt() {
        var person = prompt("Please enter your name", "Harry Potter");
        if (person != null) {
        document.getElementById("demo").innerHTML =
        "Hello " + person + "! How are you today?";
        }
    }
</script>
<script>
    function showConfirm(){
        var r=confirm("Press a button!");
        if(r==true){
            document.getElementById("demo").innerHTML ="you choose yes";
        }else{
             document.getElementById("demo").innerHTML ="you choose no";
        }
    }
</script>
```
Android对这些方法进行拦截，并做出数据的展示，返回响应的值
```java
    webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(MainActivity.this, "onJsAlert：" + message, Toast.LENGTH_SHORT).show();
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Toast.makeText(MainActivity.this, "onJsConfirm：" + message, Toast.LENGTH_SHORT).show();
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Toast.makeText(MainActivity.this, "onJsPrompt：" + message, Toast.LENGTH_SHORT).show();
                result.confirm("Tyhj");
                return true;
            }
        });
```

### Android调用JavaScript方法
>* 第一种是webView的`loadUrl()`方法，无法获取返回值
>* 第二种是webView的`evaluateJavascript()`方法，可以获取返回值

#### 调用loadUrl方法
调用JavaScript的方法比较简单，直接调用就可以了，遇到一个问题是我在加载网页后直接调用JavaScript的方法发现一直不行，是网页还没有加载完成所以没法调用，监听一下网页加载完成再调用就好了
```java
        webView.loadUrl("file:///android_asset/test.html");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String msg = "呵呵呵";
                int duration = 1000;
                //调用JavaScript的toast()方法
                webView.loadUrl("javascript:toast('" + msg + "','" + duration + "')");
            }
        });
```
#### 调用evaluateJavascript方法获取返回值
这个方法适用于Android4.4版本以上，以下版本其实也有一些应对的办法，可以自己找找
```java
webView.evaluateJavascript("javascript:getMsg()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Toast.makeText(MainActivity.this,value,Toast.LENGTH_SHORT).show();
                    }
                });
```

其实可以看出来，基本上只支持传递字符串而已，但是支持字符串，就意味着支持基本类型（自己强转一下）和Json数据了

### Android注入js代码
有时候网页并不是我们定制的，里面没有我们需要的JavaScript代码，我们可以注入JavaScript代码进去；比如网页上的图片，我们可以提供查看和保存图片的功能，就需要注入JavaScript
```java
    /**
     * 这段js函数的功能是，遍历所有的img节点，并添加onclick函数，
     * 函数的功能是在图片点击的时候调用本地java接口imageClick()并传递url过去
     */
    public static final String GET_IMAGE_URL = "javascript:(function(){" +
            "var objs = document.getElementsByTagName(\"img\");" +
            "for(var i=0;i<objs.length;i++)" +
            "{" +
            "objs[i].onclick=function(){window.activity.imageClick(this.getAttribute(\"src\"));}" +
            "}" +
            "})()";
            
    webView.loadUrl("file:///android_asset/test.html");
    //传递对象给JavaScript
    webView.addJavascriptInterface(MainActivity.this, "activity");
    //在这里注入JavaScript
    webView.loadUrl(GET_IMAGE_URL);
            
    /**
     * 点击图片时候调用
     *
     * @param imgUrl
     */
    @JavascriptInterface
    public void imageClick(String imgUrl) {
        //获取到图片的URL，可以在此操作图片
        Toast.makeText(MainActivity.this, imgUrl, Toast.LENGTH_SHORT).show();
    }
```


### WebView长按事件
网页中相应Android的长按事件也是经常用到的，比如刚才的点击一般是查看图片，长按保存图片或者其他操作；其实就是设置WebView的长按事件，然后通过WebView的getHitTestResult()的函数可以获取点击页面元素的类型，然后，我们再根据类型进行相应的处理，还是以图片为例
```java
webView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                        // 如果是图片类型或者是带有图片链接的类型
                        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                                hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                            String picUrl = hitTestResult.getExtra();
                            Toast.makeText(MainActivity.this, "长按获取到图片地址：" + picUrl, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
```

### 监听图片选择
这个是我随意加的，因为我们都可以互相调用了，那做什么都应该是没问题的，只是看见webView有一个`setWebChromeClient`方法里面可以监听到图片选择，可以响应一下；就是监听到图片选择以后，调用系统方法选择图片，返回给JavaScript，也比较简单
```java
    ValueCallback<Uri[]> mUploadMessageArray;
    int RESULT_CODE = 0;
    webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessageArray = filePathCallback;
                //选择图片
                Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooserIntent.setType("image/*");
                startActivityForResult(chooserIntent, RESULT_CODE);
                return true;
            }
        });
        
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_CODE) {
            if (mUploadMessageArray != null) {
                Uri result = (data == null || resultCode != RESULT_OK ? null : data.getData());
                //这里返回给JavaScript
                mUploadMessageArray.onReceiveValue(new Uri[]{result});
                mUploadMessageArray = null;
            }
        }
    }
```

然后HTML里面就是简单的选择图片，好像都没有涉及到JavaScript
```html
<p>
    <input type="file" value="打开文件" />
</p>
```

> 项目源码：https://github.com/tyhjh/WebViewH.git
