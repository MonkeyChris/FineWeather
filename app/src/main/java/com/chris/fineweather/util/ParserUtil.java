package com.chris.fineweather.util;

import android.text.TextUtils;

import com.chris.fineweather.db.ChinaCity;
import com.chris.fineweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//json数据解析工具
public class ParserUtil {
    //使用JSONObject解析api返回的城市数据并存储到数据库中
    public static boolean handleChinaCityResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ChinaCity city = new ChinaCity();
                    city.setCityId(jsonObject.getString("id"));
                    city.setCityName(jsonObject.getString("cityZh"));
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //将api返回的天气数据解析成Weather实体类
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //解析header背景图片API返回的json数组
    public static String handleImageResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imagesObject = (JSONObject) jsonArray.get(i);
                return imagesObject.getString("url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
