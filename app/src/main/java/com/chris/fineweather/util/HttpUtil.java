package com.chris.fineweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//发送网络请求工具
public class HttpUtil {
    public static void sendRequestWithOkHttp(String url,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
}
