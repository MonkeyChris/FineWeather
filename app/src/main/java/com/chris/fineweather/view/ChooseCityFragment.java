package com.chris.fineweather.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

//创建碎片显示城市列表
public class ChooseCityFragment extends Fragment {
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList;
    private List<ChinaCity> cityList;
    private ProgressDialog progressDialog;

    //为ListView设置适配器
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_city, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        listView = (ListView) view.findViewById(R.id.city_list);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }
    //设置ListView的点击事件
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCityId = cityList.get(position).getCityId();
                String selectedCityName = cityList.get(position).getCityName();
                titleText.setText(selectedCityName);
                SharedPreferences.Editor editor = getActivity().
                        getSharedPreferences("weather", Context.MODE_PRIVATE).edit();
                editor.putString("selectedCityId",selectedCityId);
                editor.apply();
                Intent intent = new Intent(getActivity(),WeatherActivity.class);
                startActivity(intent);
                getActivity().finish();
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
                    getActivity().runOnUiThread(new Runnable() {
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "城市数据加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //显示联网查询进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("服务器加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
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
