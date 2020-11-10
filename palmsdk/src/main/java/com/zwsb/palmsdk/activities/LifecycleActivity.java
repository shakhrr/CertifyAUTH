package com.zwsb.palmsdk.activities;


import androidx.appcompat.app.AppCompatActivity;

public class LifecycleActivity extends AppCompatActivity {
    private LifecycleObserver lifecycleObserver;
    protected void setLifecycleObserver(LifecycleObserver lifecycleObserver) {
        this.lifecycleObserver = lifecycleObserver;
    }
}
