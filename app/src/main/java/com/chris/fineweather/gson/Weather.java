package com.chris.fineweather.gson;

import java.util.List;

//创建对其他实体类进行引用的总实体类
public class Weather {
    public String status;
    public Basic basic;
    public Alarm alarms;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    public List<HourlyForecast> hourly_forecast;
    public List<DailyForecast> daily_forecast;
}
