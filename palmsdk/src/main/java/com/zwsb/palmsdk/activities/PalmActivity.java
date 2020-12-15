package com.zwsb.palmsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.redrockbiometrics.palm.PalmImage;
import com.redrockbiometrics.palm.PalmMessage;
import com.redrockbiometrics.palm.PalmModelMaskMessage;
import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.logging.Logger;

import de.hdodenhof.circleimageview.CircleImageView;


public class PalmActivity extends AppCompatActivity {
    public static final int ON_CLOSE_RESULT_CODE = 11;

    public static final String IS_RIGHT_PALM_NEEDED_KEY = "USER_PALM_KEY";
    public static final String IS_NAV_NEEDED_KEY = "IS_NAV_NEEDED_KEY";
    public static final String USER_NAME_KEY = "USER_NAME_KEY";
    public static final String INTENT_TO_START_KEY = "INTENT_TO_START_KEY";

    CircleImageView palmImageView;
    Button clearButton;
    Button bigNextButton;
    ProgressBar progressBar;
    FrameLayout toolbarLayout;
    ImageView closeButton;
    ImageView nextButton;
    TextView titleTextView;
    TextView tvClickDone;

    private boolean isRightPalm = false;
    private boolean isNavNeeded = false;
    private String userName;

    private Intent intentToStart;
    ImageLoadTask imageLoadTask;

    public static Intent getIntent(Context context, String userName, boolean isRightPalm, boolean isNavigationNeeded) {
        Intent intent = new Intent(context, PalmActivity.class);
        intent.putExtra(IS_RIGHT_PALM_NEEDED_KEY, isRightPalm);
        intent.putExtra(IS_NAV_NEEDED_KEY, isNavigationNeeded);
        intent.putExtra(USER_NAME_KEY, userName);

        return intent;
    }

    public static Intent getIntent(Context context, Intent intentToStart, String userName, boolean isRightPalm, boolean isNavigationNeeded) {
        Intent intent = new Intent(context, PalmActivity.class);
        intent.putExtra(IS_RIGHT_PALM_NEEDED_KEY, isRightPalm);
        intent.putExtra(IS_NAV_NEEDED_KEY, isNavigationNeeded);
        intent.putExtra(USER_NAME_KEY, userName);
        intent.putExtra(INTENT_TO_START_KEY, intentToStart);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_palm);

            palmImageView = findViewById(R.id.palmImageView);
            clearButton = findViewById(R.id.clearButton);
            progressBar = findViewById(R.id.progressBar);

            toolbarLayout = findViewById(R.id.toolbarLayout);
            closeButton = findViewById(R.id.closeButton);
            nextButton = findViewById(R.id.nextButton);
            bigNextButton = findViewById(R.id.bigNextButton);
            titleTextView = findViewById(R.id.titleTextView);
            tvClickDone = findViewById(R.id.click_done);
            palmImageView.setBorderColor(getResources().getColor(R.color.green_bg));
            palmImageView.setBorderWidth(getResources().getDimensionPixelSize(R.dimen.border_width));

            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClearClick();
                }
            });

            isRightPalm = getIntent().getBooleanExtra(IS_RIGHT_PALM_NEEDED_KEY, false);
            isNavNeeded = getIntent().getBooleanExtra(IS_NAV_NEEDED_KEY, false);
            userName = getIntent().getStringExtra(USER_NAME_KEY);
            intentToStart = getIntent().getParcelableExtra(INTENT_TO_START_KEY);

            View.OnClickListener onCloseListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    setResult(ON_CLOSE_RESULT_CODE, intent);
                    finish();
                }
            };
            closeButton.setOnClickListener(onCloseListener);

            bigNextButton.setVisibility(isNavNeeded ? View.VISIBLE : View.GONE);
            nextButton.setVisibility(isNavNeeded ? View.VISIBLE : View.GONE);

            tvClickDone.setVisibility(isRightPalm ? View.VISIBLE : View.GONE);

            View.OnClickListener onNextListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != intentToStart) {
                        startActivity(intentToStart);
                    } else {
                        if (SharedPreferenceHelper.isLeftPalmEnabled(PalmActivity.this, userName)) {
                            if (SharedPreferenceHelper.isRightPalmEnabled(PalmActivity.this, userName)) {
                                finish();
                            } else {
                                startActivity(AuthActivity.getIntentForRightPalm(PalmActivity.this, userName));
                            }
                        } else {
                            startActivity(AuthActivity.getIntentForLeftPalm(PalmActivity.this, userName));
                        }
                    }

                    finish();
                }
            };
            nextButton.setOnClickListener(onNextListener);
            bigNextButton.setOnClickListener(onNextListener);

            View rootView = findViewById(R.id.rootView);
            rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    float radius = v.getWidth() * 4 / 9;
                    radius = (radius - (radius * BaseUtil.decreaseCoefficient));

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) palmImageView.getLayoutParams();
                    params.width = (int) radius * 2;
                    params.height = (int) radius * 2;
                    palmImageView.setLayoutParams(params);
                }
            });
        } catch (Exception e) {
            Log.e("Palm", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != imageLoadTask) {
            imageLoadTask.cancel(true);
        }

        imageLoadTask = new ImageLoadTask();
        imageLoadTask.execute();
    }

    @Override
    protected void onDestroy() {
        imageLoadTask.cancel(true);
        super.onDestroy();
    }

    public void onClearClick() {
        if (isRightPalm) {
            SharedPreferenceHelper.setRightPalmEnabled(PalmActivity.this, false, userName);
            startActivity(AuthActivity.getIntentForRightPalm(this, userName));
        } else {
            SharedPreferenceHelper.setLeftPalmEnabled(PalmActivity.this, false, userName);
            startActivity(AuthActivity.getIntentForLeftPalm(this, userName));
        }
    }

    class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private static final int IMAGE_CHECK_DELAY = 5000;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar.setVisibility(View.VISIBLE);
            if (isRightPalm) {
                titleTextView.setText(R.string.right_palm_title);
            } else {
                titleTextView.setText(R.string.left_palm_title);
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            PalmAPI.testMatch(getApplicationContext(), userName);
            extractModelMask();

            Long startTime = System.currentTimeMillis();
            PalmMessage tempMessage;
            for (; ; ) {
                tempMessage = PalmAPI.m_PalmBiometrics.WaitMessage();
                if (isCancelled() || (tempMessage instanceof PalmModelMaskMessage)) {
                    break;
                }

                if (System.currentTimeMillis() > startTime + IMAGE_CHECK_DELAY) {
                    startTime = System.currentTimeMillis();
                    extractModelMask();
                }
            }

            return getBitmap(((PalmModelMaskMessage) tempMessage).image);
        }

        private void extractModelMask() {
            if (isRightPalm) {
                PalmAPI.m_PalmBiometrics.ExtractModelMask(SharedPreferenceHelper.getSavedPalmId(SharedPreferenceHelper.RIGHT_PALM_ID_KEY, userName));
            } else {
                PalmAPI.m_PalmBiometrics.ExtractModelMask(SharedPreferenceHelper.getSavedPalmId(SharedPreferenceHelper.LEFT_PALM_ID_KEY, userName));
            }
          Log.d("deep async",SharedPreferenceHelper.getSavedPalmId(SharedPreferenceHelper.RIGHT_PALM_ID_KEY, userName) + "right key :"+SharedPreferenceHelper.getSavedPalmId(SharedPreferenceHelper.LEFT_PALM_ID_KEY, userName));
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            palmImageView.setImageDrawable(new BitmapDrawable(getResources(), result));
            progressBar.setVisibility(View.GONE);
        }

        private Bitmap getBitmap(PalmImage image) {
            Bitmap img = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888);
            byte[] data = image.data;
            int i = 0;
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    int r = data[i++];
                    int g = data[i++];
                    int b = data[i++];
                    int rgb = 0xFF000000 | (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8;
                    img.setPixel(x, y, rgb);
                }
            }
              Log.d("deep logg","bitmap");
            return img;
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavNeeded) {
            SharedPreferenceHelper.setRightPalmEnabled(PalmActivity.this, false, userName);
            SharedPreferenceHelper.setLeftPalmEnabled(PalmActivity.this, false, userName);
            Intent intent = new Intent();
            setResult(ON_CLOSE_RESULT_CODE, intent);

            finish();
        }
    }
}
