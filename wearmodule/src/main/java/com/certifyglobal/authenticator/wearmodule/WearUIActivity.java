package com.certifyglobal.authenticator.wearmodule;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;


import com.certifyglobal.authenticator.R;


public class WearUIActivity extends WearableActivity {

    //private TokenAdapter mTokenAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_list);

            // WearableListView listView = findViewById(R.id.wearable_list);
            String[] dataset = {"Company1", "Company2", "Company3", "Company4"};
        // list.setLayoutManager(new WearableLinearLayoutManager(this));
           // list.setAdapter(new Adapter(this, dataset));
            // Enables Always-on
            //  setAmbientEnabled();
        } catch (Exception e) {
            Log.e("onCreate", e.toString());
        }
    }
}
