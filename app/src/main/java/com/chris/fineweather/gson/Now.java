package com.chris.fineweather.gson;

//定义now的实体类
public class Now {
    public String fl;
    public String hum;
    public String pcpn;
    public String pres;
    public String tmp;
    public String vis;

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
