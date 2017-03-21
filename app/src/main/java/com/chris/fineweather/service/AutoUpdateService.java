package com.chris.fineweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.chris.fineweather.Activity.WeatherActivity;
import com.chris.fineweather.R;
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
        int updateTime = 30 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + updateTime;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = getSharedPreferences("weather",MODE_PRIVATE);
        String cityName = prefs.getString("cityName",null);
        String weatherUrl = "https://api.heweather.com/v5/weather?city=" + cityName +
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

                    //天气通知栏
                    String cityName = weather.basic.city;
                    String nowCondTxt = weather.now.cond.txt;
                    String nowTemp = weather.now.tmp + "℃";
                    Intent intent = new Intent(AutoUpdateService.this,WeatherActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(AutoUpdateService.this,0,intent,0);
                    Notification weatherNotification = new NotificationCompat.Builder(AutoUpdateService.this)
                            .setContentTitle(cityName)
                            .setContentText(nowTemp + " • " + nowCondTxt)
                            .setSmallIcon(R.mipmap.ic_notification)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                            .setContentIntent(pi)
                            .build();
                    startForeground(1,weatherNotification);
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
