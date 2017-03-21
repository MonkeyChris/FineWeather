package com.chris.fineweather.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chris.fineweather.R;
import com.chris.fineweather.db.ChinaCity;
import com.chris.fineweather.util.HttpUtil;
import com.chris.fineweather.util.ParserUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseCityActivity extends AppCompatActivity {

    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList;
    private List<ChinaCity> cityList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chocity);

        titleText = (TextView) findViewById(R.id.title_text);

        //为ListView设置适配器
        listView = (ListView) findViewById(R.id.city_list);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        //设置ListView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName = cityList.get(position).getCityName();
                titleText.setText(cityName);
                SharedPreferences.Editor editor = getSharedPreferences("weather", MODE_PRIVATE).edit();
                editor.putString("cityName",cityName);
                editor.apply();
                Intent intent = new Intent(ChooseCityActivity.this,WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        });
        queryCity();
    }

    //查询城市，优先从数据库查询，若没有则从服务器上查询
    private void queryCity() {
        cityList = DataSupport.findAll(ChinaCity.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (ChinaCity city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
        } else {
            String chinaCityUrl = "http://files.heweather.com/china-city-list.json";
            queryCityFromServer(chinaCityUrl);
        }
    }

    //从服务器上查询城市
    private void queryCityFromServer(String chinaCityUrl) {
        showProgressDialog();
        HttpUtil.sendRequestWithOkHttp(chinaCityUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean parserResult;
                parserResult = ParserUtil.handleChinaCityResponse(responseText);
                if (parserResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            queryCity();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseCityActivity.this, "城市数据加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //显示联网查询进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("城市列表");
            progressDialog.setMessage("服务器加载中...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    //关闭联网查询进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
