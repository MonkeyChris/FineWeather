package com.chris.fineweather.gson;

//定义hourly_forecast的实体类
public class HourlyForecast {
    public String date;
    public String hum;
    public String pop;
    public String pres;
    public String tmp;

    public Condition cond;
    public class Condition {
        public String code;
        public String txt;
    }

    public Wind wind;
    public class Wind {
        public String deg;
        public String dir;
        public String sc;
        public String spd;
    }
}
