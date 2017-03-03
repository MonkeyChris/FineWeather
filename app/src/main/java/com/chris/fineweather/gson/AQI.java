package com.chris.fineweather.gson;

//定义aqi的实体类
public class AQI {
    public AQICity city;
    public class AQICity {
        public String aqi;
        public String co;
        public String no2;
        public String o3;
        public String so2;
        public String pm10;
        public String pm25;
        public String qlty;
    }
}
