package com.lws.radarview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RadarView radarView = (RadarView) findViewById(R.id.radar_view);
        radarView.setValues(new double[]{70, 30, 90, 40, 100, 20});
        radarView.setTitles(new String[]{"攻", "守", "爆", "御", "气", "血"});
    }
}
