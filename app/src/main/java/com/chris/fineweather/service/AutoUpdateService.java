package com.chris.fineweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import com.chris.fineweather.gson.Weather;
import com.chris.fineweather.util.HttpUtil;
import com.chris.fineweather.util.ParserUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateHeaderImage();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int cycleTime = 15 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + cycleTime;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = getSharedPreferences("weather",MODE_PRIVATE);
        String cityId = prefs.getString("selectedCityId",null);
        String weatherUrl = "https://api.heweather.com/v5/weather?city=" + cityId +
                "&key=ae45a4738bde4a3d9039cda85f4b42a3";
        HttpUtil.sendRequestWithOkHttp(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //解析返回的天气数据，存储为缓存
                String responseText = response.body().string();
                Weather weather = ParserUtil.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = getSharedPreferences("weather",MODE_PRIVATE).edit();
                    editor.putString("weatherCache",responseText);
                    editor.apply();
                }
            }
        });
    }

    private void updateHeaderImage() {
        String imageRequestUrl = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        HttpUtil.sendRequestWithOkHttp(imageRequestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //解析服务器返回的数据，获取图片链接，存储为缓存
                String responseText = response.body().string();
                String url = ParserUtil.handleImageResponse(responseText);
                if (url != null) {
                    String imageUrl = "https://bing.com" + url;
                    SharedPreferences.Editor editor = getSharedPreferences("weather",MODE_PRIVATE).edit();
                    editor.putString("imageUrlCache",imageUrl);
                    editor.apply();
                }
            }
        });
    }
}
