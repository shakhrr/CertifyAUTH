package com.certifyglobal.authenticator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.certifyglobal.authenticator.facedetection.TokenAdapterRecycler;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.PayloadObjectCallback;
import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.SwipeHelper;
import com.certifyglobal.utils.SwipeToDeleteCallback;
import com.certifyglobal.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;

public class UserActivity extends AppCompatActivity implements JSONObjectCallback, Communicator, PayloadObjectCallback {
    private String LAG = "UserActivity - ";
    private TokenPersistence mTokenPersistence;
    private static TokenAdapterRecycler mTokenAdapter;
    private RecyclerView.AdapterDataObserver mDataSetObserver;
    private RefreshListBroadcastReceiver receiver;
    ImageView imageAdd;
    ImageView imageMenu;
    private LinearLayout llNotifications;
    private Dialog dialog;
    private PopupMenu mPopupMenu;
    public static boolean isUserIn = true;
    public int position = -1;
    private RecyclerView recyclerLocation;
    public static final String ACTION_IMAGE_SAVED = "com.certifyglobal.certifyauth.ACTION_IMAGE_SAVED";
    public String companyId = "";
    public String userId = "";
    public String hostName = "";
    public String versionCode = "";
    private TextView tvCount;
    private CountDownTimer countDownTimer;
    public static int timeOut = 0;
    private Executor executor;

    @Override
    public void onJSONObjectListener(JSONObject report, String status, JSONObject req) {
        try {
            companyId = "";
            userId = "";
            if (dialog != null)
                dialog.dismiss();
            if (status.equals(EndPoints.getOsDetails)) {
                if (report != null) {
                    if (!report.isNull("OsVersion")) {
                        Utils.saveToPreferences(UserActivity.this, PreferencesKeys.osVersion, report.isNull("MobileOSVersion") ? "9.0" : report.getString("MobileOSVersion"));
                        String apiVersion = report.getString("OsVersion");
                        if (Utils.compareTowString(versionCode, apiVersion)) {
                            boolean isCritical = report.getBoolean("IsCritical");
                            if (isCritical || Utils.compareTowString(Utils.readFromPreferences(UserActivity.this, PreferencesKeys.appVersion, "2.4"), apiVersion))
                                showAlert(isCritical, apiVersion);
                            Utils.saveToPreferences(UserActivity.this, PreferencesKeys.appVersion, apiVersion);
                        } else
                            Utils.saveToPreferences(UserActivity.this, PreferencesKeys.appVersion, apiVersion);
                    }
                }
                return;
            } else if (status.equals("delete")) {
                deleteAccountRefresh();
            }
            //for deactivating third party
           /* else if (status.equals(String.format("%s%s", Settings.getAPI(), EndPoints.deactivateThird))) {
                if (report == null) return;
                deleteAccountRefresh();
                return;
            }*/
            if (position > -1)
                deleteAccountRefresh();
        } catch (Exception e) {
            Logger.error(LAG + "onJSONObjectListener(JSONObject report, String status)", e.getMessage());
        }
    }

    public void deleteAccountRefresh() {
        try {
            new TokenPersistence(this).delete(position);
            mTokenAdapter.notifyDataSetChanged();
            position = -1;
        } catch (Exception e) {
            Logger.error(LAG + "deleteAccountRefresh()", e.getMessage());
        }
    }

    @Override
    public void setAction(String userName, int noValue) {
        if (userName.isEmpty()) {
            this.onJSONObjectListener(null, "delete", null);
        } else {
            dialog = new Dialog(UserActivity.this);
            dialog = Utils.showDialog(dialog, UserActivity.this);
            if (dialog != null) dialog.show();
            if (noValue == -2) {
                deleteAccountRefresh();
                dialog.dismiss();
            } else Utils.deactivateUser(userId, companyId, this, this, hostName);
        }

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onPayloadObjectListener(JSONArray report, String status) {
        try {
            Logger.debug("payloaddddddddddddddddddddddddddddd", "payload");
            if (report == null || report.length() == 0)
                llNotifications.setVisibility(View.GONE);
            else {
                Utils.saveToPreferences(UserActivity.this, PreferencesKeys.notificationCount, report.length());
                tvCount.setText(String.format("%d", report.length()));
                llNotifications.setVisibility(View.VISIBLE);
                String payload = report.getJSONObject(0).getString("payload");
                startCountDownTimer(Utils.getTimerTimeStamp(Utils.notificationPayload(new JSONObject(payload), UserActivity.this), timeOut));
            }
        } catch (Exception e) {
            Logger.error(LAG + "onPayloadObjectListener(JSONObject report, String status)", e.getMessage());
        }
    }

    // TextView tvCode;
    public static class RefreshListBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mTokenAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

//            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//            Bundle bundle = new Bundle();
//            bundle.putString("Requestor", "postmethod");
//            mFirebaseAnalytics.logEvent("auth", bundle);
            setContentView(R.layout.user_layout);
            Utils.saveToPreferences(UserActivity.this, PreferencesKeys.isLogin, true);
            mTokenPersistence = new TokenPersistence(this);
            imageAdd = findViewById(R.id.image_add);
            imageMenu = findViewById(R.id.image_menu);
            llNotifications = findViewById(R.id.ll_notifications);
            tvCount = findViewById(R.id.tv_count_notification);
            llNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent notificationIntent = new Intent(UserActivity.this, PushNotificationActivity.class);
                        String strData = Utils.readFromPreferences(UserActivity.this, PreferencesKeys.notificationData, "");
                        JSONObject test = new JSONObject(strData);
                        notificationIntent = Utils.attachIntent(notificationIntent, test);
                        notificationIntent.putExtra("inUser", "yes");
                        startActivity(notificationIntent);
                        llNotifications.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Logger.error(LAG + "llNotifications -> onClick(View v)", e.getMessage());
                    }
                }
            });
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Utils.keyValidations(this, this);
                } catch (Exception ignore) {

                }
            }
            imageAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scanQR();
                }
            });
            mPopupMenu = new PopupMenu(this, imageMenu);
            mPopupMenu.getMenuInflater().inflate(R.menu.menu, mPopupMenu.getMenu());
            imageMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        // mPopupMenu.show();
                        startActivity(new Intent(UserActivity.this, Settings.class));
                    } catch (Exception e) {
                        Logger.error(LAG + "imageMenu - setOnClickListener", e.getMessage());
                    }

                }
            });
            mTokenAdapter = new TokenAdapterRecycler(this, this, mTokenPersistence);
            receiver = new RefreshListBroadcastReceiver();
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            registerReceiver(receiver, new IntentFilter(ACTION_IMAGE_SAVED));
            recyclerLocation = findViewById(R.id.lv_coupons);
            recyclerLocation.setAdapter(mTokenAdapter);
            recyclerLocation.addItemDecoration(new DividerItemDecoration(recyclerLocation.getContext(), DividerItemDecoration.VERTICAL));
            enableSwipeToDeleteAndUndo();

          /*  recyclerLocation.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    SwipeMenuItem deleteItem = new SwipeMenuItem(UserActivity.this);
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xff, 0x33, 0x33)));
                    // set item width
                    deleteItem.setWidth(Utils.dp2px(70, UserActivity.this));
                    // set item title
                    //  deleteItem.setTitle("Delete");
                    deleteItem.setIcon(R.drawable.ic_delete);
                    // set item title fontsize
                    deleteItem.setTitleSize(12);
                    // set item title font color
                    deleteItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };*/
          /*  recyclerLocation.setMenuCreator(creator);
            recyclerLocation.setAdapter(mTokenAdapter);
            recyclerLocation.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

                @Override
                public void onSwipeStart(int position) {
                    recyclerLocation.smoothOpenMenu(position);
                }

                @Override
                public void onSwipeEnd(int position) {
                    //recyclerLocation.smoothCloseMenu();
                }

            });

            recyclerLocation.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int pos, SwipeMenu menu, int index) {
                    try {
                    switch (index) {
                            case 0:
                                position = pos;
                                Token token = mTokenPersistence.get(pos);
                                String[] labelU = token.getLabel().split("\\|");
                                companyId = labelU.length >= 3 ? labelU[3] : "";
                                userId = labelU.length >= 4 ? labelU[4] : "";
                                hostName = labelU.length >= 5 ? labelU[5] : "";
                                if (labelU.length >= 2)
                                    Utils.ShowDialog(UserActivity.this, labelU[1], position, UserActivity.this);
                                else
                                    //third party account
                                    Utils.ShowDialog(UserActivity.this, token.toString(), -2, UserActivity.this);
                                break;
                        }

                    }
                    catch (Exception e){
                        Logger.error("Delete action",e.getMessage());
                    }

                    return false;
                }
            });*/
            // Don't permit screenshots since these might contain OTP codes.
            mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_settings:
                            startActivity(new Intent(UserActivity.this, Settings.class));
                            break;
                        case R.id.menu_about:
                            startActivity(new Intent(UserActivity.this, Passcode.class));
                            break;
                        case R.id.menu_help:
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://authx.com"));
                            startActivity(browserIntent);
                            break;
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            Logger.error(LAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void scanQR() {
        try {
            Intent livePreIntent = new Intent(this, ScanActivity.class);
            livePreIntent.putExtra("type", "Barcode");
            startActivity(livePreIntent);
        } catch (Exception e) {
            Logger.error(LAG + "scanQR()", e.getMessage());
        }
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeHelper swipeHelper = new SwipeHelper(this) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(UserActivity.this,
                        "Delete",
                        0,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(final int pos) {
                                //    final String item = mTokenAdapter.getData().get(pos);
                                // mTokenAdapter.removeItem(pos);

                                try {
                                    position = pos;
                                    Token token = mTokenPersistence.get(pos);
                                    String[] labelU = token.getLabel().split("\\|");
                                    companyId = labelU.length >= 3 ? labelU[3] : "";
                                    userId = labelU.length >= 4 ? labelU[4] : "";
                                    hostName = labelU.length >= 5 ? labelU[5] : "";
                                    if (labelU.length >= 2)
                                        Utils.ShowDialog(UserActivity.this, labelU[1], position, UserActivity.this);
                                    else
                                        //third party account
                                        Utils.ShowDialog(UserActivity.this, token.toString(), -2, UserActivity.this);


                                } catch (Exception e) {
                                    Logger.error("Delete action", e.getMessage());
                                }


                            }
                        }
                ));
            }
        };
        swipeHelper.attachToRecyclerView(recyclerLocation);

    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mTokenAdapter == null) return;
            mDataSetObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                }
            };
            mTokenAdapter.registerAdapterDataObserver(mDataSetObserver);
            mTokenAdapter.notifyDataSetChanged();

            Utils.getOSDetails(this, this);
            if (Utils.readFromPreferences(this, PreferencesKeys.notificationCount, 0) == 0)
                llNotifications.setVisibility(View.GONE);
            else {
                tvCount.setText(Utils.readFromPreferences(this, PreferencesKeys.notificationCount, 0) + "");
                llNotifications.setVisibility(View.VISIBLE);
                timeOut = Integer.parseInt(Utils.readFromPreferences(this, PreferencesKeys.notificationTimeOut, ""));
                timeOut = timeOut * 1000;
                startCountDownTimer(Utils.getTimerTimeStamp(Utils.readFromPreferences(this, PreferencesKeys.notificationTime, ""), timeOut));
            }

        } catch (Exception e) {
            Logger.error(LAG + "onResume()", e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mTokenAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Logger.error(LAG + "onPause()", e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //mTokenAdapter.unregisterDataSetObserver(mDataSetObserver);
            mTokenAdapter.unregisterAdapterDataObserver(mDataSetObserver);
            unregisterReceiver(receiver);
            countDownTimer.cancel();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            try {
                TokenPersistence.saveAsync(this, new Token(uri));
            } catch (Token.TokenUriInvalidException e) {
                Logger.error(LAG + "onNewIntent(Intent intent)", e.getMessage());
            }
        }
    }

    private void showAlert(boolean isCritical, String version) {
        try {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getResources().getString(R.string.new_version));
            alertDialog.setCancelable(false);
            alertDialog.setMessage(String.format("Version %s is available on the PlayStore.", version));
            alertDialog.setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    String packageName = getPackageName();
                    String url;
                    try {
                        getPackageManager().getPackageInfo(packageName, 0);
                        url = "market://details?id=" + packageName;
                    } catch (Exception ignored) {
                        url = "http://play.google.com/store/apps/details?id=" + packageName;
                    }
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        //noinspection deprecation
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            });
            if (!isCritical)
                alertDialog.setNegativeButton(getResources().getString(R.string.later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

            AlertDialog builder = alertDialog.create();
            builder.show();
        } catch (Exception e) {
            Logger.error(LAG + "showAlert(boolean isCritical, String version)", e.getMessage());
        }
    }

    private void startCountDownTimer(long timeCountInMilliSeconds) {
        try {
            countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    llNotifications.setVisibility(View.GONE);
                }

            }.start();
        } catch (Exception e) {
            Logger.error(LAG + "startCountDownTimer(long timeCountInMilliSeconds)", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
