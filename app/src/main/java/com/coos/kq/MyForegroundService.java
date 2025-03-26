package com.coos.kq;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.coos.kq.server.MakerServer;
import com.coos.kq.server.MakerServerBinder;

public class MyForegroundService extends Service {
    private static final String TAG = MyForegroundService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "com.coos.kq.MyForegroundService";
    public static final String CHANNEL_NAME = "com.coos.kq";

    private boolean isRunningForeground;

    private MakerServerBinder makerServerBinder;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        makerServerBinder = new MakerServerBinder(this);

    }

    private void createNotificationChannel() {
        Log.i(TAG, "createNotificationChannel start");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
        if (notificationChannel == null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            //是否在桌面icon右上角展示小红点
            channel.enableLights(true);
            //小红点颜色
            channel.setLightColor(Color.RED);
            //通知显示
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            //是否在久按桌面图标时显示此渠道的通知
            //channel.setShowBadge(true);
            mNotificationManager.createNotificationChannel(channel);
        }
        Log.i(TAG, "createNotificationChannel end");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        makerServerBinder.binderToken = intent.getStringExtra("activityToken");
        Log.i(TAG, "onBind binderToken:" + makerServerBinder.binderToken);
        return makerServerBinder; // 绑定服务
    }

    public static int requestCode = 100;

    public void startForegroundService() {
        isRunningForeground = true;
        // 创建通知渠道（Android 8.0及以上需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        requestCode++;
        Intent resultIntent = new Intent(this.getApplicationContext(), MainActivity.class);

        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), requestCode, resultIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Kq App 正在运行")
                .setContentText("点击返回 Kq App")
                .setSmallIcon(R.mipmap.logo)
                .setContentIntent(pendingIntent)

                //设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //设定通知显示的时间
                .setWhen(System.currentTimeMillis())
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
        Log.i(TAG, "startForeground id:" + NOTIFICATION_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 及以上版本的处理逻辑
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            // Android 10 以下版本的处理逻辑
            startForeground(NOTIFICATION_ID, notification);
        }

        Log.i(TAG, "startForeground success id:" + NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        MakerServer.startServer(this.getApplicationContext());

        // 如果服务已经在前台，不需要再次调用 startForeground()
        if (!isRunningForeground) {
            startForegroundService(); // 方法用于启动前台服务并显示通知
        }
        // If we get killed, after returning from here, restart
        return START_STICKY; // 服务在停止后重新启动
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        MakerServer.stopServer(this.getApplicationContext());
//        MakerServer.removeMakerLib();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

}
