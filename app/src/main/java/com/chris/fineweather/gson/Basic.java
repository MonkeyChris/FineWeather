package com.chris.fineweather.gson;

//定义basic的实体类
public class Basic {
    public String city;
    public String id;

    public Update update;
    public class Update {
        public String loc;
    }
}
