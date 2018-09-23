package com.home.bingodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

public class LoadActivity extends AppCompatActivity {

    private TextView gameNameTextView;
    private String gameNameString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,                          // 去除状态栏
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_load);

        gameNameString = getIntent().getStringExtra("gameName");
        gameNameTextView =  findViewById(R.id.gameNameTextView);
        gameNameTextView.setText("Hello " + gameNameString);
    }
}
