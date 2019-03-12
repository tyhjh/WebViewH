package com.yorhp.javascript;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * 原生的调用方式
 *
 * @author yorhp
 */
public class MainActivity extends AppCompatActivity {

    WebView webView;
    ValueCallback<Uri[]> mUploadMessageArray;
    int RESULT_CODE = 0;
    public static final String URL_SCHEME = "js";
    public static final String URL_AUTHORITY = "webview";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.wbView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl("file:///android_asset/test.html");
        webView.addJavascriptInterface(MainActivity.this, "activity");
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

            @Override
            public void onPageFinished(WebView view, String url) {
                String msg = "呵呵呵";
                int duration = 1000;
                //调用JavaScript的toast()方法
                //webView.loadUrl("javascript:toast('" + msg + "','" + duration + "')");
                //获取返回值
                webView.evaluateJavascript("javascript:getMsg()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
                    }
                });

                webView.loadUrl(GET_IMAGE_URL);
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
            }
        });

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

        /*webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessageArray = filePathCallback;
                Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooserIntent.setType("image/*");
                startActivityForResult(chooserIntent, RESULT_CODE);
                return true;
            }
        });*/


    }


    /**
     * 暴露给JavaScript的方法
     *
     * @param msg
     */
    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 返回信息给JavaScript
     *
     * @return
     */
    @JavascriptInterface
    public String getMsg() {
        return "Hello from Android";
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_CODE) {
            if (mUploadMessageArray != null) {
                Uri result = (data == null || resultCode != RESULT_OK ? null : data.getData());
                mUploadMessageArray.onReceiveValue(new Uri[]{result});
                mUploadMessageArray = null;
            }
        }
    }
}
