

package com.certifyglobal.authenticator;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.Utils;

import java.util.HashMap;
import java.util.Objects;

public class TokenLayout extends FrameLayout implements View.OnClickListener, Runnable {

    private ProgressCircle mProgressInner;
    private ProgressCircle mProgressOuter;
    private ProgressBar progressBar;
    private ImageView mImage;
    private TextView mCode;
    private TextView mIssuer;
    private TextView mLabel;
    private TextView labelUserType;
    private TextView tvCount;
    private ImageView mMenu;
    private RelativeLayout rlProgress;
    private PopupMenu mPopupMenu;

    private TokenCode mCodes;
    private Token.TokenType mType;
    private String mPlaceholder;
    private long mStartTime;

    public TokenLayout(Context context) {
        super(context);
    }

    public TokenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TokenLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mProgressInner = findViewById(R.id.progressInner);
        mProgressOuter = findViewById(R.id.progressOuter);
        progressBar = findViewById(R.id.progressBar);
        mImage = findViewById(R.id.image);
        mCode = findViewById(R.id.code);
        mIssuer = findViewById(R.id.issuer);
        mLabel = findViewById(R.id.label);
        mMenu = findViewById(R.id.menu);
        tvCount = findViewById(R.id.tv_count);
        rlProgress = findViewById(R.id.rl_code_progress);
        labelUserType = findViewById(R.id.label_user_type);
        //   mPopupMenu = new PopupMenu(getContext(), mMenu);
        // mMenu.setOnClickListener(this);
    }

    public void bind(Token token, HashMap<String, byte[]> imageList, int menu, PopupMenu.OnMenuItemClickListener micl) {
        mCodes = null;
        try {
            // Setup menu.
//        mPopupMenu.getMenu().clear();
//        mPopupMenu.getMenuInflater().inflate(menu, mPopupMenu.getMenu());
//        mPopupMenu.setOnMenuItemClickListener(micl);

            // Cancel all active animations.
            setEnabled(true);
            removeCallbacks(this);
            ///  mImage.clearAnimation();
            // mProgressInner.clearAnimation();
            // mProgressOuter.clearAnimation();
            mProgressInner.setVisibility(View.GONE);
            mProgressOuter.setVisibility(View.GONE);
            mIssuer.setVisibility(View.VISIBLE);
            mLabel.setVisibility(View.VISIBLE);
            mMenu.setImageResource(R.drawable.ic_action_down);
            mCode.setVisibility(GONE);
            rlProgress.setVisibility(GONE);
            // Get the code placeholder.
            char[] placeholder = new char[token.getDigits()];
            for (int i = 0; i < placeholder.length; i++)
                placeholder[i] = '-';
            mPlaceholder = new String(placeholder);

            // Show the image.
//            Picasso.get()
//                    .load(token.getImage())
//                    .placeholder(R.drawable.ic_defalt_icon)
//                    .fit()
//                    .into(mImage);
            // Set the labels.
            String[] labelWithU = token.getLabel().split("\\|");
            if (labelWithU.length == 1)
                labelWithU = token.getLabel().split("\\|");

            if (labelWithU.length >= 3) {
                mLabel.setText(labelWithU[2]);
                mIssuer.setText(labelWithU[0]);
                labelUserType.setText(labelWithU[1]);
                labelUserType.setVisibility(VISIBLE);
                String id = labelWithU.length > 5 ? labelWithU[5] : "-1";
                if (imageList == null || id.equals("-1") || imageList.get(id) == null)
                    mImage.setVisibility(GONE);
                else {
                    mImage.setVisibility(VISIBLE);
                    mImage.setImageBitmap(BitmapFactory.decodeByteArray(imageList.get(id), 0, Objects.requireNonNull(imageList.get(id)).length));
                }
            } else {
                mLabel.setText(labelWithU[0]);

                mImage.setVisibility(VISIBLE);
                String issuer = Utils.getIssuer(token.toString());
                mIssuer.setText(issuer);
                switch (issuer.toLowerCase()) {
                    case "amazon":
                        mImage.setImageResource(R.drawable.ic_amazon);
                        break;
                    case "dropbox":
                        mImage.setImageResource(R.drawable.ic_dropbox);
                        break;
                    case "facebook":
                        mImage.setImageResource(R.drawable.ic_facebook);
                        break;
                    case "github":
                        mImage.setImageResource(R.drawable.ic_github);
                        break;
                    case "microsoft":
                        mImage.setImageResource(R.drawable.ic_microsoft);
                        break;
                    case "slack":
                        mImage.setImageResource(R.drawable.ic_slack);
                        break;
                    case "twitter":
                        mImage.setImageResource(R.drawable.ic_twitter);
                        break;
                    case "google":
                        mImage.setImageResource(R.drawable.ic_google);
                        break;
                    case "openvpn":
                        mImage.setImageResource(R.drawable.ic_openvpn);
                        break;
                    case "third party":
                    default:
                        mIssuer.setText(getResources().getString(R.string.third_party));
                        mImage.setVisibility(GONE);
                        break;
                }
            }
            //  mLabel.setVisibility(labelWithU.length >= 3 ? VISIBLE : GONE);
           /* if (EndPoints.deployment == EndPoints.Mode.Local && labelWithU.length > 1) {
                labelUserType.setText(labelWithU[1]);
                labelUserType.setVisibility(VISIBLE);
            }*/
            // mCode.setText(mPlaceholder);


//                if (mIssuer.getText().length() == 0) {
//                    mIssuer.setText(token.getLabel());
//                    mLabel.setVisibility(View.GONE);
//                } else {
//                    mLabel.setVisibility(View.VISIBLE);
//                }
        } catch (Exception e) {
            Logger.error("bind(Token token, int menu, PopupMenu.OnMenuItemClickListener micl)", e.getMessage());
        }
    }

    private void animate(View view, int anim, boolean animate) {
        Animation a = AnimationUtils.loadAnimation(view.getContext(), anim);
        if (!animate)
            a.setDuration(0);
        view.startAnimation(a);
    }

    public void start(Token.TokenType type, TokenCode codes, boolean animate) {
        mCodes = codes;
        mType = type;

        // Start animations.
        // mProgressInner.setVisibility(View.VISIBLE);
        // animate(mProgressInner, R.anim.fadein, animate);
        // animate(mImage, R.anim.token_image_fadeout, animate);

        // Handle type-specific UI.
        switch (type) {
            case HOTP:
                setEnabled(false);
                break;
            case TOTP:
                //   mProgressOuter.setVisibility(View.VISIBLE);
                //animate(mProgressOuter, R.anim.fadein, animate);
                break;
        }

        mStartTime = System.currentTimeMillis();
        post(this);
    }

    @Override
    public void onClick(View v) {

        //    mPopupMenu.show();
    }

    @Override
    public void run() {
        // Get the current data

        String code = mCodes == null ? null : mCodes.getCurrentCode();
        if (code != null) {
            // Determine whether to enable/disable the view.
            if (!isEnabled())
                setEnabled(System.currentTimeMillis() - mStartTime > 5000);
            // Update the fields
            mCode.setText(code.replaceFirst("...", "$0 "));

            //   mCode.setVisibility(View.VISIBLE);
            //  mIssuer.setVisibility(View.GONE);
            //  mLabel.setVisibility(View.GONE);
            // mProgressInner.setProgress(mCodes.getCurrentProgress());
            progressBar.setProgress(mCodes.getCurrentProgress());
            int currentProgress = mCodes.getCurrentProgress() / 33;
            tvCount.setText(String.valueOf(currentProgress));
            if(currentProgress <= 5)
                mCode.setTextColor(getResources().getColor(R.color.readOfX));
            else mCode.setTextColor(getResources().getColor(R.color.black));
            if (mType != Token.TokenType.HOTP)
                mProgressOuter.setProgress(mCodes.getTotalProgress());

            postDelayed(this, 100);
            return;
        }
        mCode.setVisibility(View.GONE);
        rlProgress.setVisibility(GONE);
        mMenu.setImageResource(R.drawable.ic_action_down);
        // animate(mImage, R.anim.token_image_fadein, true);
    }
}
