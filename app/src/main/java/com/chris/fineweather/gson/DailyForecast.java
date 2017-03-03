package com.chris.fineweather.gson;

//定义daily_forecast的实体类
public class DailyForecast {
    public String date;
    public String hum;
    public String pcpn;
    public String pop;
    public String pres;
    public String vis;

    public Astronomical astro;
    public class Astronomical {
        public String mr;
        public String ms;
        public String sr;
        public String ss;
    }

    public Temperature tmp;
    public class Temperature {
        public String max;
        public String min;
    }

    public Condition cond;
    public class Condition {
        public String code_d;
        public String code_n;
        public String txt_d;
        public String txt_n;
    }

    public Wind wind;
    public class Wind {
        public String deg;
        public String dir;
        public String sc;
        public String spd;
    }
}
