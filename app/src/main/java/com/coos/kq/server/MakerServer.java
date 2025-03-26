package com.coos.kq.server;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.coos.kq.Utils;
import com.coos.kq.error.CodeException;
import com.google.gson.annotations.SerializedName;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakerServer {
    public static final String TAG = MakerServer.class.getSimpleName();
    public static final Object MAKER_LIB_LOCK = new Object();
    public static MakerLib MAKER_LIB = null;

    public static MakerLib getMakerLib() {
        if (MAKER_LIB != null) {
            return MAKER_LIB;
        }
        synchronized (MAKER_LIB_LOCK) {
            if (MAKER_LIB != null) {
                return MAKER_LIB;
            }
            MAKER_LIB = Native.load("Maker", MakerLib.class);
        }
        return MAKER_LIB;
    }


    public static void removeMakerLib() {
        if (MAKER_LIB == null) {
            return;
        }
        synchronized (MAKER_LIB_LOCK) {
            if (MAKER_LIB == null) {
                return;
            }
            Native.getNativeLibrary(MakerLib.class).close();
            MAKER_LIB = null;
        }
    }


    public static MakerServer.Response execute(MakerServer.Request request) {
        MakerServer.Response res = new MakerServer.Response();
        res.code = "0";
        try {
            Log.d(TAG, "do [" + request.toDo + "] start");
            Pointer resultPtr = getMakerLib().Execute(request.toString());
            if (resultPtr != null) {
                String result = resultPtr.getString(0);
//                MakerLib.INSTANCE.FreeString(resultPtr);
                res = MakerServer.Response.parse(result);
                Log.d(TAG, "do [" + request.toDo + "] end, code:" + res.code + ",msg:" + res.msg);
            }
        } catch (Throwable e) {
            Log.e(TAG, "do [" + request.toDo + "] error, msg:" + e.getMessage(), e);
            res.code = "1";
            res.msg = e.getMessage();
        }
        return res;
    }


    public static MakerServer.Response startServer(Context context) {
        MakerServer.Request request = MakerServer.Request.Do("startWebServer");
        request.data = getWebConfig(context);

        MakerServer.Response res = MakerServer.execute(request);
        return res.trimWebConfig();
    }

    public static MakerServer.Response stopServer(Context context) {
        MakerServer.Request request = MakerServer.Request.Do("stopWebServer");
        return MakerServer.execute(request);
    }

    public static MakerServer.Response getServer(Context context) {
        MakerServer.Request request = MakerServer.Request.Do("getWebServer");

        MakerServer.Response res = MakerServer.execute(request);
        return res.trimWebConfig();
    }

    public static MakerServer.Response restartServer(Context context) {
        stopServer(context);

        return startServer(context);
    }

    public static MakerServer.WebConfig getWebConfig(Context context) {
        MakerServer.WebConfig config = new MakerServer.WebConfig();
        config.locations = new ArrayList<>();
        MakerServer.WebLocationConfig location = new MakerServer.WebLocationConfig();
        location.path = "";
        location.to = "https://kq.linkdood.cn:10669";
        config.locations.add(location);
        config.replaces = new HashMap<>();
        MakerServer.WebReplaceConfig replace = new MakerServer.WebReplaceConfig();
        replace.append = "function getPosition() {\n" +
                "    var mLocation_X = '118.733959';\n" +
                "    mLocation_X = '118.733' + (Math.floor(Math.random() * 10)) + (Math.floor(Math.random() * 10)) + (Math.floor(Math.random() * 10));\n" +
                "    var mLocation_Y = '31.97865';\n" +
                "    mLocation_Y = '31.9786' + (Math.floor(Math.random() * 10)) + (Math.floor(Math.random() * 10)) + (Math.floor(Math.random() * 10));\n" +
                "    var res = {\n" +
                "        \"mRoad\": \"\", \"mCity\": \"南京市\", \"mProvince\": \"江苏省\", \"mStreetNum\": \"33号\", \"mDistrict\": \"建邺区\", \"mAoiName\": \"新城科技园\",\n" +
                "        \"mLocation_X\": mLocation_X, \"mCityCode\": \"025\", \"mLocation_Y\": mLocation_Y, \"mAddress\": \"江苏省南京市建邺区广聚路靠近新城科技园\",\n" +
                "        \"mStreet\": \"广聚路\", \"mPoiName\": \"新城科技园\", \"mAdCode\": \"320105\"\n" +
                "    };\n" +
                "    return res;\n" +
                "}\n" +
                "\n" +
                "vrv.jssdk.getPosition = function (config) {\n" +
                "    var res = getPosition();\n" +
                "    config.success(res);\n" +
                "};";
        config.replaces.put("attend/static/js/vrv-jssdk.js", replace);
        return config;
    }


    public static class WebConfig {
        public String host = "";
        public int port = 58080;

        public String context = "/";

        public String assetsDir = "";
        public String assetsSeparate = "";
        public String filesDir = "";
        public String filesSeparate = "";

        public long maxMultipartMemory = 0;

        public MakerServer.WebTlsConfig tls = new MakerServer.WebTlsConfig();
        public List<MakerServer.WebLocationConfig> locations;

        public Map<String, MakerServer.WebReplaceConfig> replaces;
    }

    public static class WebTlsConfig {
        public boolean open;
        public String cert;
        public String certFile;
        public String key;
        public String keyFile;
    }

    public static class WebLocationConfig {
        public String path;
        public String to;
    }

    public static class WebReplaceConfig {
        public String contentType;
        public String content;
        public String append;
    }

    public static class Request {

        @SerializedName("do")
        public String toDo;

        public Map<String, String> header;

        public Object data;

        public static MakerServer.Request Do(String toDo) {
            MakerServer.Request res = new MakerServer.Request();
            res.toDo = toDo;
            return res;
        }

        public MakerServer.Request setHeader(String key, String value) {
            if (this.header == null) {
                this.header = new HashMap<>();
            }
            this.header.put(key, value);
            return this;
        }

        public MakerServer.Request setData(Object data) {
            this.data = data;
            return this;
        }

        @NonNull
        public String toString() {
            return Utils.toJson(this);
        }

        public String dataToJson() {
            return Utils.toJson(data);
        }
    }


    public static class Response {
        public String code;
        public String msg;
        public Object data;


        public boolean isSuccess() {
            return "0".equals(code);
        }

        public CodeException toException() {
            return new CodeException(code, msg);
        }


        @NonNull
        public String toString() {
            return Utils.toJson(this);
        }

        public String toPrettyJson() {
            return Utils.toPrettyJson(this);
        }


        public static MakerServer.Response success() {
            MakerServer.Response res = new Response();
            res.code = "0";
            return res;
        }

        public static MakerServer.Response parse(String json) {
            return Utils.json2Obj(json, MakerServer.Response.class);
        }

        public MakerServer.Response trimWebConfig() {
            if (data == null) {
                return this;
            }

            if (!(data instanceof Map)) {
                return this;
            }
            String jsonStr = Utils.toJson(data);
            MakerServer.WebConfig webConfig = Utils.json2Obj(jsonStr, MakerServer.WebConfig.class);
            if (webConfig.tls != null) {
                webConfig.tls.cert = null;
                webConfig.tls.key = null;
                webConfig.tls.certFile = null;
                webConfig.tls.keyFile = null;
            }
            webConfig.locations = null;
            webConfig.replaces = null;
            webConfig.assetsDir = null;
            webConfig.assetsSeparate = null;
            webConfig.filesDir = null;
            webConfig.filesSeparate = null;
            data = webConfig;
            return this;
        }
    }
}
