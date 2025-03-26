package com.coos.kq;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.coos.kq.server.MakerServer;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class JavaScriptInterface {
    private final Context context;

    public final WebView webView;

    public JavaScriptInterface(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    @JavascriptInterface
    public void receiveData(String data) {
        Toast.makeText(context, "收到 WebView 数据: " + data + new Date(), Toast.LENGTH_SHORT).show();
    }

    // JS 调用的方法
    @JavascriptInterface
    public void showToast(String message) {
        Toast.makeText(context, message + new Date(), Toast.LENGTH_SHORT).show();
    }

    // Android 调用 JS 方法示例
    public void callJavaScript(String data, android.webkit.ValueCallback<String> resultCallback) {
        webView.evaluateJavascript("javascript:receiveData('" + data + "')", resultCallback);
    }

    @JavascriptInterface
    public String getAndroidData() {
        return "Data from Android" + new Date();
    }

    @JavascriptInterface
    public String getNetIPs() {
        return Utils.toPrettyJson(Utils.getNetIPs());
    }

    @JavascriptInterface
    public String startServer() {
        Intent serviceIntent = new Intent(context, MyForegroundService.class);
        context.startForegroundService(serviceIntent);
        return MakerServer.Response.success().toPrettyJson();
    }

    @JavascriptInterface
    public String stopServer() {
        Intent serviceIntent = new Intent(context, MyForegroundService.class);
        context.stopService(serviceIntent);
        return MakerServer.Response.success().toPrettyJson();
    }

    @JavascriptInterface
    public String restartServer() {
        Intent serviceIntent = new Intent(context, MyForegroundService.class);
        context.stopService(serviceIntent);
        context.startForegroundService(serviceIntent);
        return MakerServer.Response.success().toPrettyJson();
    }

    @JavascriptInterface
    public String getServerInfo() {
        Map<String, Object> info = new HashMap<>();
        MakerServer.Response res = MakerServer.getServer(context);
        info.put("serverInfo", res);
        info.put("netIps", getNetIPs());
        // 获取当前手机系统语言
        info.put("language", Locale.getDefault().getLanguage());
        // 获取当前手机系统版本号
        info.put("systemVersion", Build.VERSION.RELEASE);
        // 获取手机型号
        info.put("systemModel", Build.MODEL);
        // 获取手机厂商
        info.put("deviceBrand", Build.BRAND);
        info.put("supportedAbiS", Build.SUPPORTED_ABIS);
        info.put("filesDir", Objects.requireNonNull(context.getFilesDir()).getAbsolutePath());
        info.put("cacheDir", Objects.requireNonNull(context.getCacheDir()).getAbsolutePath());
        info.put("testDb", Objects.requireNonNull(context.getDatabasePath("test.db")).getAbsolutePath());
        return Utils.toPrettyJson(info);
    }

}