package com.plusub.jsandroiddemo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


/**
 * @see <a href="http://www.sollyu.com/android-software-development-webview-addjavascriptinterface-cycle-of-gradual-one/">参考网页</a>
 * @see <a href="http://www.sollyu.com/android-software-development-webview-addjavascriptinterface-cycle-of-gradual-two/">参考网页2</a>
 * @see <a href="http://droidyue.com/blog/2014/09/20/interaction-between-java-and-javascript-in-android/index.html">js学习,例2的学习</a>
 */
public class MainActivity extends AppCompatActivity {

    private Button  m_testButtom1;
    private Button m_testButtom2;
    private Button m_testButtom3;
    private Button m_testButtom4;
    private WebView m_WebView;
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

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        m_testButtom1 = (Button)findViewById(R.id.button1);
        m_testButtom2 = (Button)findViewById(R.id.button2);
        m_testButtom3 = (Button)findViewById(R.id.button3);
        m_testButtom4 = (Button)findViewById(R.id.button4);
        m_WebView = (WebView)findViewById(R.id.webview);
        m_WebView.getSettings().setJavaScriptEnabled(true);
        //Alert无法弹出,应该是没有设置WebChromeClient
        m_WebView.setWebChromeClient(new WebChromeClient() {
        });
        //Uncaught ReferenceError: functionName is not defined,问题出现原因，网页的js代码没有加载完成，就调用了js方法
        m_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //在这里执行你想调用的js函数，如果不在载入结束时执行，就会找不到js方法网页的js代码没有加载完成，就调用了js方法
                System.out.println("load finish"+url);
            }
        });

        m_testButtom1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String string = "http://www.sollyu.com";
                //webView调用js的基本格式为webView.loadUrl(“javascript:methodName(parameterValues)”)
                //有js中无return的用法
//                m_WebView.loadUrl("javascript:testFunc1(\"" + string + "\")");   // 调用html中的JavaScript函数testFunc1,这里只有一个参数

                //有js中return的用法
                testEvaluateJavascript(m_WebView, string);
            }
        });
        m_testButtom2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String string = "这里有两个参数:";
                int nInt = 191067617;
                m_WebView.loadUrl("javascript:testFuncAdd(\"" + string + "\"," + String.valueOf(nInt) + ")"); // 通用这里有2个参数
//                testEvaluateJavascript(m_WebView, string, nInt);
            }
        });
        m_testButtom3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                testMethod(m_WebView);
            }
        });
        m_testButtom4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainOtherActivity.class));
            }
        });




        //例子1
        m_WebView.addJavascriptInterface(new JsInteration2(), "demo");
        //例子1,也就是说可以用“file///android_asset”访问assets下文件；可以用“file:///android_res”来访问res下文件。
        m_WebView.loadUrl("file:///android_asset/demo.html");

        //--------------------有参数有返回值，返回值是通过js调用java方法实现window.control.toastMessage(return_var);
        //例子2
//        m_WebView.addJavascriptInterface(new JsInteration(), "control");
        //例子2
//        m_WebView.loadUrl("file:///android_asset/js_java_interaction.html");
    }


    //-----------------------------------------------------------
    public class JsInteration2 {
        // 这两个函数可以在JavaScript中调用.Uncaught TypeError: Object [object Object] has no method,如果只在4.2版本以上的机器出问题，那么就是系统处于安全限制的问题了
        @JavascriptInterface
        public void testFunc1(String string) {
            Message msg = new Message();
            msg.what = 0;
            msg.obj = string;
            mHandler.sendMessage(msg);
        }

        @JavascriptInterface
        public void testFuncAdd(String val1) {
            Message msg = new Message();
            msg.what = 1;
            msg.obj = val1;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 上面限定了结果返回结果为String，对于简单的类型会尝试转换成字符串返回，对于复杂的数据类型，建议以字符串形式的json返回。
     evaluateJavascript方法必须在UI线程（主线程）调用，因此onReceiveValue也执行在主线程。
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testEvaluateJavascript(WebView webView, String str) {
        webView.evaluateJavascript("testFunc1(\""+str+"\")", new ValueCallback<String>() {

            @Override
            public void onReceiveValue(String value) {
                Log.i("Log", "onReceiveValue value=" + value);
            }
        });
    }

    /**
     * demo.html中如果testFuncAdd有返回值return才用这个
     * @param webView
     * @param value1
     * @param value2
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testEvaluateJavascript(WebView webView, String value1, int value2) {
        webView.evaluateJavascript("testFuncAdd(\"" + value1 + "\",\"" + String.valueOf(value2) + "\")", new ValueCallback<String>() {

            @Override
            public void onReceiveValue(String value) {
                Log.i("Log", "onReceiveValue value=" + value);
            }
        });
    }

    //-----------------------------------------------------------
    private void testMethod(WebView webView) {
        String call = "javascript:sayHello()";
//        call = "javascript:alertMessage(\"" + "content" + "\")";
        call = "javascript:toastMessage(\"" + "content" + "\")";
        call = "javascript:sumToJava(1,2)";
        webView.loadUrl(call);
    }

    public class JsInteration {

        @JavascriptInterface
        public void toastMessage(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void onSumResult(int result) {
            Log.i("Log", "onSumResult result=" + result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
