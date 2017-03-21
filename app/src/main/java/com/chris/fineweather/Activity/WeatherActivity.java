package com.chris.fineweather.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chris.fineweather.R;
import com.chris.fineweather.gson.Weather;
import com.chris.fineweather.service.AutoUpdateService;
import com.chris.fineweather.util.HttpUtil;
import com.chris.fineweather.util.ParserUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefresh;
    private DrawerLayout drawerLayout;
    private ImageView ctbImage;
    private ImageView nvHeaderImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);//在toolbar上为NV设置菜单按钮
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_home);//替换按钮图标
        }

        //为navigationView的menu设置点击事件
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View nvHeader = navigationView.inflateHeaderView(R.layout.nv_header);
        navigationView.setCheckedItem(R.id.location);
        navigationView.setNavigationItemSelectedListener(new NavigationView.
                OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.location:
                        drawerLayout.closeDrawers();
                        Intent cityIntent = new Intent(WeatherActivity.this,ChooseCityActivity.class);
                        startActivity(cityIntent);
                        break;
                    case R.id.setting:
                        //设置待实现
                        Toast.makeText(WeatherActivity.this,"这个功能很快就会有哒",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.about:
                        drawerLayout.closeDrawers();
                        Intent aboutIntent = new Intent(WeatherActivity.this,AboutActivity.class);
                        startActivity(aboutIntent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //为悬浮按钮设置点击事件--弹出对话框
        FloatingActionButton faButton = (FloatingActionButton) findViewById(R.id.fab_favourite);
        faButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder fabDialog = new AlertDialog.Builder(WeatherActivity.this);
                fabDialog.setTitle("支持");
                fabDialog.setMessage("去项目主页给作者个Star支持一下吧，亲~");
                fabDialog.setCancelable(true);
                fabDialog.setPositiveButton("好哒", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://github.com/MonkeyChris/FineWeather"));
                        startActivity(intent);
                        /*try {
                            String marketUrl = "market://details?id=" + getPackageName();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(marketUrl));
                            startActivity(intent);                  //打开应用商店的方法
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(WeatherActivity.this, "打开应用商店失败", Toast.LENGTH_SHORT).show();
                        }*/
                    }
                });
                fabDialog.show();
            }
        });

        //天气数据缓存机制
        final SharedPreferences prefs = getSharedPreferences("weather", MODE_PRIVATE);
        String weatherCache = prefs.getString("weatherCache", null);
        String cityName = prefs.getString("cityName", null);
        if (weatherCache != null) {
            //有缓存时直接读取缓存数据进行解析
            Weather weather = ParserUtil.handleWeatherResponse(weatherCache);
            assert weather != null;
            assert cityName != null;
            if (cityName.equals(weather.basic.city + "市")) {
                showWeatherInfo(weather);
            } else {
                requestWeather(cityName);
            }
        } else {
            //无缓存时从服务器查询
            requestWeather(cityName);
        }

        //header背景图缓存机制
        ctbImage = (ImageView) findViewById(R.id.collapsing_toolbar_image);
        nvHeaderImage = (ImageView) nvHeader.findViewById(R.id.nv_header_image);
        String imageUrlCache = prefs.getString("imageUrlCache",null);
        if (imageUrlCache != null) {
            Glide.with(this).load(imageUrlCache).into(ctbImage);
            Glide.with(this).load(imageUrlCache).into(nvHeaderImage);
        } else {
            loadHeaderImage();
        }

        //下拉刷新天气和背景图
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String cityName = prefs.getString("cityName",null);
                requestWeather(cityName);
                loadHeaderImage();
            }
        });
    }

    //从服务器查询天气数据
    public void requestWeather(String cityName) {
        String weatherUrl = "https://api.heweather.com/v5/weather?city=" + cityName +
                "&key=ae45a4738bde4a3d9039cda85f4b42a3";
        HttpUtil.sendRequestWithOkHttp(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"你可能没联网哦",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherResponse = response.body().string();
                final Weather weather = ParserUtil.handleWeatherResponse(weatherResponse);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = getSharedPreferences("weather",MODE_PRIVATE).edit();
                            editor.putString("weatherCache",weatherResponse);
                            editor.apply();
                            showWeatherInfo(weather);
                            Toast.makeText(WeatherActivity.this,"天气更新成功",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WeatherActivity.this,"天气更新失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    //从服务器加载header背景图片
    public void loadHeaderImage() {
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
                if (url != null) {
                    final String imageUrl = "https://bing.com" + url;
                    SharedPreferences.Editor editor = getSharedPreferences("weather", MODE_PRIVATE).edit();
                    editor.putString("imageUrlCache", imageUrl);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(imageUrl).into(ctbImage);
                            Glide.with(WeatherActivity.this).load(imageUrl).into(nvHeaderImage);
                        }
                    });
                }
            }
        });
    }

    public void startAutoUpdateService() {
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    //将天气数据显示到UI上
    public void showWeatherInfo(Weather weather) {

        startAutoUpdateService();//启动天气自动更新服务

        //basic
        TextView basicUpdateLoc = (TextView) findViewById(R.id.update_loc);
        //now
        TextView nowTemp = (TextView) findViewById(R.id.now_temp);
        TextView nowCondTxt = (TextView) findViewById(R.id.now_cond_txt);
        ImageView nowIcon = (ImageView) findViewById(R.id.now_icon);
        TextView nowFl = (TextView) findViewById(R.id.now_fl);
        TextView nowHum = (TextView) findViewById(R.id.now_hum);
        TextView nowPcpn = (TextView) findViewById(R.id.now_pcpn);
        TextView nowPres = (TextView) findViewById(R.id.now_pres);
        TextView nowVis = (TextView) findViewById(R.id.now_vis);
        TextView nowWindDeg = (TextView) findViewById(R.id.now_wind_deg);
        TextView nowWindDir = (TextView) findViewById(R.id.now_wind_dir);
        TextView nowWindSc = (TextView) findViewById(R.id.now_wind_sc);
        TextView nowWindSpd = (TextView) findViewById(R.id.now_win_spd);
        //alarm
        TextView alarmLevel = (TextView) findViewById(R.id.alarm_level);
        TextView alarmStat = (TextView) findViewById(R.id.alarm_stat);
        TextView alarmTitle = (TextView) findViewById(R.id.alarm);
        TextView alarmTxt = (TextView) findViewById(R.id.alarm_txt);
        TextView alarmType = (TextView) findViewById(R.id.alarm_type);
        ImageView alarmNa = (ImageView) findViewById(R.id.alarm_na);
        //aqi
        TextView aqi = (TextView) findViewById(R.id.aqi);
        TextView aqiQlty = (TextView) findViewById(R.id.aqi_qlty);
        TextView aqiCo = (TextView) findViewById(R.id.aqi_co);
        TextView aqiNo2 = (TextView) findViewById(R.id.aqi_no2);
        TextView aqiO3 = (TextView) findViewById(R.id.aqi_o3);
        TextView aqiSo2 = (TextView) findViewById(R.id.aqi_so2);
        TextView aqiPm10 = (TextView) findViewById(R.id.aqi_pm10);
        TextView aqiPm25 = (TextView) findViewById(R.id.aqi_pm25);
        //suggestion
        TextView suggestionComfBrf = (TextView) findViewById(R.id.suggestion_comfort_brf);
        TextView suggestionComfTxt = (TextView) findViewById(R.id.suggestion_comf_txt);
        TextView suggestionDrsgBrf = (TextView) findViewById(R.id.drsg_brf);
        TextView suggestionDrsgTxt = (TextView) findViewById(R.id.drsg_txt);
        TextView suggestionUVBrf = (TextView) findViewById(R.id.uv_brf);
        TextView suggestionUVTxt = (TextView) findViewById(R.id.uv_txt);
        TextView suggestionFluBrf = (TextView) findViewById(R.id.flu_brf);
        TextView suggestionFluTxt = (TextView) findViewById(R.id.flu_txt);
        TextView suggestionSportBrf = (TextView) findViewById(R.id.sport_brf);
        TextView suggestionSportTxt = (TextView) findViewById(R.id.sport_txt);
        TextView suggestionTravBrf = (TextView) findViewById(R.id.trav_brf);
        TextView suggestionTravTxt = (TextView) findViewById(R.id.trav_txt);
        TextView suggestionCWBrf = (TextView) findViewById(R.id.cw_brf);
        TextView suggestionCWTxt = (TextView) findViewById(R.id.cw_txt);

        //basic
        String cityName = weather.basic.city;
        String basicUpdateLocS = weather.basic.update.loc;
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cityName);//在collapsingToolbar上显示城市名
        basicUpdateLoc.setText(basicUpdateLocS);
        //now
        String nowTempString = weather.now.tmp + "℃";
        String nowCondTxtString = weather.now.cond.txt;
        String nowCondCode = weather.now.cond.code;
        String nowFlString = weather.now.fl + "℃";
        String nowHumString = weather.now.hum + "％";
        String nowPcpnString = weather.now.pcpn + "mm";
        String nowPresString = weather.now.pres + "hpa";
        String mowVisString = weather.now.vis + "km";
        String nowWindDegString = weather.now.wind.deg + "°";
        String nowWindDirString = weather.now.wind.dir;
        String nowWindScString = weather.now.wind.sc + "级";
        String nowWindSpdString = weather.now.wind.spd + "km/h";
        Glide.with(this).load("http://files.heweather.com/cond_icon/"
                + nowCondCode + ".png").into(nowIcon);//加载天气图标
        nowTemp.setText(nowTempString);
        nowCondTxt.setText(nowCondTxtString);
        nowFl.setText(nowFlString);
        nowHum.setText(nowHumString);
        nowPcpn.setText(nowPcpnString);
        nowPres.setText(nowPresString);
        nowVis.setText(mowVisString);
        nowWindDeg.setText(nowWindDegString);
        nowWindDir.setText(nowWindDirString);
        nowWindSc.setText(nowWindScString);
        nowWindSpd.setText(nowWindSpdString);
        //alarm
        if (weather.alarms != null) {
            String alarmLevelString = weather.alarms.level;
            String alarmStatString = weather.alarms.stat;
            String alarmTitleString = weather.alarms.title;
            String alarmTxtString = weather.alarms.txt;
            String alarmTypeString = weather.alarms.type;
            alarmTitle.setText(alarmTitleString);
            alarmLevel.setText(alarmLevelString);
            alarmStat.setText(alarmStatString);
            alarmType.setText(alarmTypeString);
            alarmTxt.setText(alarmTxtString);
        } else {//无预警则加载N/A图标
            Glide.with(this).load("http://files.heweather.com/cond_icon/999.png").into(alarmNa);
        }
        //aqi
        String aqiString = weather.aqi.city.aqi;
        String aqiQltyString = weather.aqi.city.qlty;
        String aqiCoString = weather.aqi.city.co;
        String aqiNo2String = weather.aqi.city.no2;
        String aqiO3String = weather.aqi.city.o3;
        String aqiSo2String = weather.aqi.city.so2;
        String aqiPm10String = weather.aqi.city.pm10;
        String aqiPm25String = weather.aqi.city.pm25;
        aqi.setText(aqiString);
        aqiQlty.setText(aqiQltyString);
        aqiCo.setText(aqiCoString);
        aqiNo2.setText(aqiNo2String);
        aqiO3.setText(aqiO3String);
        aqiSo2.setText(aqiSo2String);
        aqiPm10.setText(aqiPm10String);
        aqiPm25.setText(aqiPm25String);
        //suggestion
        String suggestionComfBrfS = weather.suggestion.comf.brf;
        String suggestionComfTxtS = weather.suggestion.comf.txt;
        String suggestionDrsgBrfS = weather.suggestion.drsg.brf;
        String suggestionDrsgTxtS = weather.suggestion.drsg.txt;
        String suggestionUVBrfS= weather.suggestion.uv.brf;
        String suggestionUVTxtS = weather.suggestion.uv.txt;
        String suggestionFluBrfS = weather.suggestion.flu.brf;
        String suggestionFluTxtS = weather.suggestion.flu.txt;
        String suggestionSportBrfS = weather.suggestion.sport.brf;
        String suggestionSportTxtS = weather.suggestion.sport.txt;
        String suggestionTravBrfS = weather.suggestion.trav.brf;
        String suggestionTravTxtS = weather.suggestion.trav.txt;
        String suggestionCWBrfS = weather.suggestion.cw.brf;
        String suggestionCWTxtS = weather.suggestion.cw.txt;
        suggestionComfBrf.setText(suggestionComfBrfS);
        suggestionComfTxt.setText(suggestionComfTxtS);
        suggestionDrsgBrf.setText(suggestionDrsgBrfS);
        suggestionDrsgTxt.setText(suggestionDrsgTxtS);
        suggestionUVBrf.setText(suggestionUVBrfS);
        suggestionUVTxt.setText(suggestionUVTxtS);
        suggestionFluBrf.setText(suggestionFluBrfS);
        suggestionFluTxt.setText(suggestionFluTxtS);
        suggestionSportBrf.setText(suggestionSportBrfS);
        suggestionSportTxt.setText(suggestionSportTxtS);
        suggestionTravBrf.setText(suggestionTravBrfS);
        suggestionTravTxt.setText(suggestionTravTxtS);
        suggestionCWBrf.setText(suggestionCWBrfS);
        suggestionCWTxt.setText(suggestionCWTxtS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);//点击菜单按钮打开NV
                break;
            default:
                break;
        }
        return true;
    }
}
