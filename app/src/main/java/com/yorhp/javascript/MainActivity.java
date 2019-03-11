package com.yorhp.javascript;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
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
        webView.loadUrl("file:///android_asset/test.html");
        webView.addJavascriptInterface(MainActivity.this, "activity");
        webView.setWebViewClient(new WebViewClient() {
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

}
