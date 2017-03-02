package com.chris.fineweather.util;

import android.text.TextUtils;

import com.chris.fineweather.db.ChinaCity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//json数据解析工具
public class ParserUtil {
    //使用JSONObject解析服务器返回的城市数据并存储到数据库中
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
}
