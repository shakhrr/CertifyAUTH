package com.certifyglobal.utils;

import android.content.Context;
import android.content.Intent;

import com.certifyglobal.authenticator.UserActivity;

public class Navigator {
    private Navigator(){}

    public static Navigator getInstance() {
        return NavigatorHolder.INSTANCE;
    }

    private static class NavigatorHolder {
      private static final Navigator INSTANCE = new Navigator();
    }

    public void navigateToUserActivity(Context context) {
      Intent intent = new Intent(context, UserActivity.class);
      context.startActivity(intent);
    }
}