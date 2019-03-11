package com.yorhp.javascript;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;

/**
 * @author dhht
 */
public class JsBridgeActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    BridgeWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_bridge);
        webView = (BridgeWebView) findViewById(R.id.bridgeWebView);
        webView.loadUrl("file:///android_asset/test2.html");
        //注册方法供JavaScript调用
        webView.registerHandler("submitFromWeb", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Toast.makeText(JsBridgeActivity.this, "从JavaScript接收到的数据为：" + data, Toast.LENGTH_SHORT).show();
                //返回数据给JavaScript
                function.onCallBack("Hello from Android");
            }
        });


        webView.callHandler("functionInJs", "执行JavaScript方法", new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(JsBridgeActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
