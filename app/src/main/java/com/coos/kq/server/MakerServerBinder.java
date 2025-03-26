package com.coos.kq.server;

import android.os.Binder;

import com.coos.kq.MyForegroundService;

import java.lang.ref.WeakReference;

public class MakerServerBinder extends Binder {
    public String binderToken;
    public final WeakReference<MyForegroundService> weakReferenceService;

    public MakerServerBinder(MyForegroundService makerServer) {
        weakReferenceService = new WeakReference<>(makerServer);
    }

    public WeakReference<MyForegroundService> getService() {
        return weakReferenceService;
    }

}
