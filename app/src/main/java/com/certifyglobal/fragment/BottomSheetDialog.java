package com.certifyglobal.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.Button; 
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.SecurityCheckupActivity;
import com.certifyglobal.fcm.OnClearFromRecentService;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
  
public class BottomSheetDialog extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
                                                      ViewGroup container, @Nullable Bundle savedInstanceState) 
    { 
        View v = inflater.inflate(R.layout.bottom_sheet_dialog,
                                  container, false); 
  
        Button btn_later = v.findViewById(R.id.btn_later);
        Button btn_now = v.findViewById(R.id.btn_now);
  
        btn_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            {
                dismiss();
                Utils.saveToPreferences(getActivity(), PreferencesKeys.bottomSheet,false);

            } 
        }); 
  
        btn_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            { 
               startActivity(new Intent(getActivity(), SecurityCheckupActivity.class));
                Utils.saveToPreferences(getActivity(), PreferencesKeys.bottomSheet,false);
                dismiss();
            } 
        }); 
        return v; 
    } 
} 