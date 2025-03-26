package com.coos.kq;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * 获取ip地址,key为网络端口名称，比如wlan0、eth0、ap0等，value为ip地址,以及节点相关的MAC地址
     *
     * @return 键值对
     */
    public static Map<String, String> getNetIPs() {
        Map<String, String> hashMap = new HashMap<>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement(); //打印的信息和 ifconfig 的大致对应
                Log.i(TAG, "----》getEtherNetIP inf = " + intf); //eth0、wifi...
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        Log.i(TAG, "----》getEtherNetIP intf.getName() = " + intf.getName());
                        Log.i(TAG, "----》getEtherNetIP inetAddress = " + inetAddress);
                        Log.i(TAG, "----》getEtherNetIP inetAddress  getHostAddress = " + inetAddress.getHostAddress());
                        byte[] hardwareAddress = intf.getHardwareAddress();

                        //节点对应的ip地址
                        hashMap.put(intf.getName(), "" + inetAddress.getHostAddress());
                        //节点对应的MAC地址，mac地址是byte数值数据，要转换成字符串
                        String mac = bytesToString(hardwareAddress);
                        hashMap.put(intf.getName() + "-MAC", "" + mac);
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, "getEtherNetIP = " + ex);
        }
        return hashMap;
    }

    //字节数据转换成字符串
    public static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    public static boolean isNotEmpty(String arg) {
        return arg != null && !arg.isEmpty();
    }

    public static boolean isEmpty(String arg) {
        return arg == null || arg.isEmpty();
    }

    public static <T> T json2Obj(String json, Class<T> type) {
        return new Gson().fromJson(json, type);
    }

    public static final Type mapObjectType = new TypeToken<Map<String, Object>>() {
    }.getType();
    public static final Type mapStringType = new TypeToken<Map<String, String>>() {
    }.getType();

    public static Map<String, Object> json2Map(String json) {
        return new Gson().fromJson(json, mapObjectType);
    }

    public static Map<String, String> json2MapString(String json) {
        return new Gson().fromJson(json, mapStringType);
    }

    public static String toJson(Object obj) {
        return new Gson().toJson(obj);
    }

    public static String toPrettyJson(Object obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(obj);
    }

    public static String obj2Json(Object obj) {
        return new Gson().toJson(obj);
    }

}
