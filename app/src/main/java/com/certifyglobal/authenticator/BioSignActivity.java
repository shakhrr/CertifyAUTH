package com.certifyglobal.authenticator;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.utils.Logger;

public class BioSignActivity extends AppCompatActivity {
    private static final String TAG = "BioSignActivity - ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_bio_sign);
            TextView tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText(getResources().getString(R.string.bio_sign));
            EditText et = findViewById(R.id.et_text);
            et.setText(getIntent().getStringExtra("Url"));
        } catch (Exception e) {
            Logger.error(TAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }
}
