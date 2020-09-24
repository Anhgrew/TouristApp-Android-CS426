package com.example.touristapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private GridView _gridView;
    private GridViewArrayAdapter _adapter;
    private ArrayList<Landmark> _landmarks;
    private GridView.OnItemClickListener _itemOnClick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadData();
        initComponent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadData() {
        _landmarks = new ArrayList<>();
        Landmark lndmk1 = new Landmark("Bến Nhà Rồng",
                "Nơi Bác Hồ ra đi tìm đường cứu nước năm 1911",
                R.drawable.logo_ben_nha_rong,
                10.768313, 106.706793);
        Landmark lndmk2 = new Landmark("Chợ Bến Thành",
                "Địa danh nổi tiếng qua các thời kỳ của Sài Gòn",
                R.drawable.logo_cho_ben_thanh,
                10.772535, 106.698034);
        Landmark lndmk3 = new Landmark("Nhà thờ Đức Bà",
                "Công trình kiến trúc độc đáo, nét đặc trưng của Sài Gòn",
                R.drawable.logo_nha_tho_duc_ba,
                10.779742, 106.699188);
        _landmarks.add(lndmk1);
        _landmarks.add(lndmk2);
        _landmarks.add(lndmk3);
    }

    private void initComponent() {
        _gridView = findViewById(R.id.gridview_places);
        _adapter = new GridViewArrayAdapter(this, R.layout.gridview_items, _landmarks);
        _gridView.setAdapter(_adapter);
        _itemOnClick = new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                Landmark lndmk = _landmarks.get(i);
                intent.putExtra("name", lndmk.getName());
                intent.putExtra("des", lndmk.getDescription());
                intent.putExtra("logoID", lndmk.getLogoID());
                intent.putExtra("lat", lndmk.getLatLng().latitude);
                intent.putExtra("long", lndmk.getLatLng().longitude);
                startActivity(intent);
            }
        };
        _gridView.setOnItemClickListener(_itemOnClick);
    }
}