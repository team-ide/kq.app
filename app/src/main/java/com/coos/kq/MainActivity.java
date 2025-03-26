package com.coos.kq;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.coos.kq.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    WebView webView;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                System.out.println("没有通知权限");
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_ASSISTANT_SETTINGS);
                startActivity(intent);
            } else {
                System.out.println("有通知权限");
            }
        }

        // 启动前台服务
        Intent serviceIntent = new Intent(this.getApplicationContext(), MyForegroundService.class);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        webView = findViewById(R.id.webView_main);
        if (webView == null) {
            throw new RuntimeException("webView_main is null");
        }
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);        // 启用 JS
        settings.setDomStorageEnabled(true);        // 启用 DOM Storage
        settings.setLoadWithOverviewMode(true);     // 自适应屏幕
        settings.setUseWideViewPort(true);         // 支持 viewport
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 缓存模式 使用可用的缓存资源，即使它们已经过期。否则从网络加载资源。
        // 解决中文乱码
        settings.setDefaultTextEncodingName("UTF-8");
        //是否允许访问文件
        settings.setAllowFileAccess(true);
        // 支持缩放(适配到当前屏幕)
        settings.setSupportZoom(true);
        webView.clearCache(true);
        webView.clearHistory();
        // 确保链接在 WebView 内打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // 处理URL跳转
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
        webView.addJavascriptInterface(new JavaScriptInterface(this.getApplicationContext(), webView), "Android");


        // assets 文件使用 "file:///android_asset/"
        // raw 文件使用 "file:///android_res/raw/"
//        Android -> JS：使用 evaluateJavascript()
//        JS -> Android：通过 JavascriptInterface
        webView.loadUrl("file:///android_asset/test.html"); // 加载本地HTML文件或URL
    }

    // 处理返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 清理资源
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}