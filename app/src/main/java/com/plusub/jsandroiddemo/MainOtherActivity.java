package com.plusub.jsandroiddemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Copyright (C) quhao All Rights Reserved <blakequ@gmail.com>
 * <p/>
 * Licensed under the Plusub License, Version 1.0 (the "License");
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * author  : quhao <blakequ@gmail.com>
 * date     : 2016/4/12 20:52
 * last modify author :
 * version : 1.0
 * description:
 */
public class MainOtherActivity extends AppCompatActivity {

    private WebView mWebView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0){
                Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //Alert无法弹出,应该是没有设置WebChromeClient
        mWebView.setWebChromeClient(new WebChromeClient() {
        });
        //Uncaught ReferenceError: functionName is not defined,问题出现原因，网页的js代码没有加载完成，就调用了js方法
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //在这里执行你想调用的js函数，如果不在载入结束时执行，就会找不到js方法网页的js代码没有加载完成，就调用了js方法
                System.out.println("load finish" + url);
                testOpenImage();
                testGetUrl();
            }
        });
        mWebView.addJavascriptInterface(new JsInteration(this), "demo");
        //例子1,也就是说可以用“file///android_asset”访问assets下文件；可以用“file:///android_res”来访问res下文件。
        mWebView.loadUrl("file:///android_asset/image.html");
    }

    /**
     * 采用直接注入js函数，给图片点击增加事件
     */
    private void testOpenImage(){
        // 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，函数的功能是在图片点击的时候调用本地java接口并传递url过去
        mWebView.loadUrl(
                "javascript:(function(){" +
                        "var objs = document.getElementsByTagName(\"img\"); " +
                        "for(var i=0;i<objs.length;i++)  " +
                        "{" +
                        "    var var1 = objs[i].src;"+//必须定义在内部function之外
                        "    objs[i].onclick=function()  " +
                        "    {  " +
                        "        console.log('this:'+this.src);"+
                        "        console.log('var:'+var1);"+
//                        "        window.demo.openImage(objs[i].src);  " + //注意：在function()内部，使用objs[i].src是无效的，必须是this.src或者使用变量var v = objs[i].src
                        "        window.demo.openImage(this.src);  " + //正确的，使用this
//                        "        window.demo.openImage(var1);  " + //正确的,使用var
                        "    }  " +
                        "}" +
                        "})()");
    }

    /**
     * 采用直接注入js函数,拦截a标签的超链接，跳转到外部浏览器
     */
    private void testGetUrl(){
        //如果标签有id的版本
        /*mWebView.loadUrl(
                "javascript:(function(){"
                        + " var obj = document.getElementById(\"saleAre\");"
                        + " var url = document.getElementById(\"saleUrl\");"
                        + " var href = url.href;"
                        + " url.href='';"
                        + " obj.onclick=function()"
                        + " {"
                        + "   window.demo.openUrl(href);"
                        + " }"
                        + "})()");*/
        mWebView.loadUrl(
                "javascript:(function(){"
                + " var objs = document.getElementsByTagName(\"a\");"
                + " for(var i=0;i<objs.length;i++)  "
                + " {"
                + "    if(objs[i].href){"
                + "     var href = objs[i].href;"
                + "     console.log(href);"
                + "     objs[i].href='';"
                + "     objs[i].onclick=function()  "
                + "     {  "
                + "        window.demo.openUrl(href);  "
                + "     }  "
                + "  }"
                + " }"
                + "})()");
    }

    public class JsInteration {
        private Context context;

        public JsInteration(Context context) {
            this.context = context;
        }

        // 这两个函数可以在JavaScript中调用.Uncaught TypeError: Object [object Object] has no method,如果只在4.2版本以上的机器出问题，那么就是系统处于安全限制的问题了
        @JavascriptInterface
        public void openImage(String url) {
            try{
                System.out.println("===image:"+url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void openUrl(String url) {
            try{
                System.out.println("===url:"+url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
