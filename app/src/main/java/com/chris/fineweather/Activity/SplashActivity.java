package com.chris.fineweather.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chris.fineweather.R;
import com.chris.fineweather.util.HttpUtil;
import com.chris.fineweather.util.ParserUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //闪屏页缓存机制
        final SharedPreferences prefs = getSharedPreferences("weather",MODE_PRIVATE);
        String imageUrlCache = prefs.getString("imageUrlCache",null);
        splashImage = (ImageView) findViewById(R.id.splash_image);
        if (imageUrlCache != null) {
            Glide.with(this).load(imageUrlCache).into(splashImage);
        } else {
            loadSplashImage();
        }

        //闪屏页加载完成，利用handler延迟页面转换
        int SPLASH_TIME = 2 * 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //判断是否存在天气缓存，存在则加载天气Activity，否则加载选择城市Activity
                if (prefs.getString("weatherCache",null) != null) {
                    Intent intent = new Intent(SplashActivity.this, WeatherActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this,ChooseCityActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_TIME);
    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (url != null) {
                            String imageUrl = "https://bing.com" + url;
                            SharedPreferences.Editor editor = getSharedPreferences("weather",MODE_PRIVATE).edit();
                            editor.putString("imageUrlCache",imageUrl);
                            editor.apply();
                            Glide.with(SplashActivity.this).load(imageUrl).into(splashImage);
                        }
                    }
                });
            }
        });
    }
}
