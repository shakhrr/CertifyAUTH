package com.certifyglobal.authenticator;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Passcode extends AppCompatActivity {
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passcode);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.about));
        ImageView img_ic_back=findViewById(R.id.img_ic_back);
        img_ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}