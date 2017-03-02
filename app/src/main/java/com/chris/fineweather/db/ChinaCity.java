package com.chris.fineweather.db;

import org.litepal.crud.DataSupport;

//创建数据库和表
public class ChinaCity extends DataSupport {
    private int id;
    private String cityId;
    private String cityName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
