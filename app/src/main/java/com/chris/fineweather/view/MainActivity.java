package com.chris.fineweather.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chris.fineweather.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //判断是否存在天气数据缓存，如果存在则跳过选择城市，直接加载天气显示界面
        SharedPreferences prefs = getSharedPreferences("weather",MODE_PRIVATE);
        if (prefs.getString("weatherCache",null) != null) {
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
