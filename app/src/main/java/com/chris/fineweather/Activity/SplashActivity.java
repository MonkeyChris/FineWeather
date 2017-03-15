package com.chris.fineweather.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.chris.fineweather.R;
import com.chris.fineweather.service.AutoUpdateService;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    //private ImageView splashImage;
    private SharedPreferences.Editor editor;
    private LocationClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*闪屏页图片缓存机制
        SharedPreferences prefs = getSharedPreferences("weather", MODE_PRIVATE);
        String imageUrlCache = prefs.getString("imageUrlCache",null);
        splashImage = (ImageView) findViewById(R.id.splash_image);
        if (imageUrlCache != null) {
            Glide.with(this).load(imageUrlCache).into(splashImage);
        } else {
           loadSplashImage();
        }*/

        //利用handler.postDelayed方法延迟页面转换
        int delayTime = 2000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestPermission(); //请求权限
            }
        },delayTime);
    }

    //权限请求
    public void requestPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,1);
        } else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0){
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this,"定位授权失败,手动选择城市或重新授权",Toast.LENGTH_LONG).show();
                            startWeatherActivity();
                            return;
                        } else {
                            requestLocation();
                        }
                    }
                } else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    //定位请求
    public void requestLocation() {
        locationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        locationClient.start();
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                String districtName = bdLocation.getDistrict();
                if (districtName != null) {
                    editor = getSharedPreferences("weather",MODE_PRIVATE).edit();
                    editor.putString("cityName",districtName);
                    editor.apply();
                    startAutoUpdateService();//启动天气自动更新服务
                } else {
                    Toast.makeText(SplashActivity.this,"获取位置信息失败",Toast.LENGTH_SHORT).show();
                }
                startWeatherActivity();
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {
                //移动热点判断接口
            }
        });
    }

    public void startWeatherActivity() {
        Intent intent = new Intent(SplashActivity.this,WeatherActivity.class);
        startActivity(intent);
        finish();
    }

    public void startAutoUpdateService() {
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /*图片请求
    public void loadSplashImage() {
        String imageRequestUrl = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        HttpUtil.sendRequestWithOkHttp(imageRequestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String imageRequestResponse = response.body().string();
                final String url = ParserUtil.handleImageResponse(imageRequestResponse);
                final String imageUrl = "https://bing.com" + url;
                editor = getSharedPreferences("weather",MODE_PRIVATE).edit();
                editor.putString("imageUrlCache",imageUrl);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (url != null) {
                            Glide.with(SplashActivity.this).load(imageUrl).into(splashImage);
                        }
                    }
                });
            }
        });
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();//活动销毁时停止定位
    }
}
