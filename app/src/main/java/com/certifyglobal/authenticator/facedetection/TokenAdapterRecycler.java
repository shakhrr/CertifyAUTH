package com.certifyglobal.authenticator.facedetection;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.authenticator.QRUrlScanResults;
import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.Token;
import com.certifyglobal.authenticator.TokenCode;
import com.certifyglobal.authenticator.TokenLayout;
import com.certifyglobal.authenticator.TokenPersistence;
import com.certifyglobal.authenticator.UserActivity;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackReconnect;
import com.certifyglobal.fcm.FireBaseInstanceIDService;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenAdapterRecycler extends RecyclerView.Adapter<TokenAdapterRecycler.MyViewHolder> implements JSONObjectCallbackReconnect {

    private TokenPersistence mTokenPersistence;
    private  LayoutInflater mLayoutInflater;
    private  HashMap<String,byte[]> companyInfoArrayList;
    private ClipboardManager mClipMan;
    private RelativeLayout rlProgressPrevious;
    private ImageView imageViewPrevious;
    private  Map<String, TokenCode> mTokenCodes;
    private Communicator communicator;
    private TextView tvPrevious;
   // private TextView tv_reconnect_previous;
    private Context context;
    ProgressDialog dialog;

    @Override
    public void onJSONObjectListenerReconnect(String report, String status, JSONObject req) {
        try {
            if (report == null) return;
            JSONObject json1=null;
            try {
                String formatedString = report.substring(1, report.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\",""));
            }catch (Exception e){
                e.printStackTrace();
                json1 = new JSONObject(report);
            }
            if(json1.get("response_code").equals(1)){
                Toast.makeText(context, "Account Activated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                notifyDataSetChanged();
            }else{
                Toast.makeText(context, report, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                notifyDataSetChanged();
            }


        }catch (Exception e){
            Logger.error("TokenAdapterRecycler onJSONObjectListener",e.getMessage());
            dialog.dismiss();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private Context ctx;
        TokenLayout tl;
        ImageView imageView;
        TextView mCode,tv_reconnect;
        RelativeLayout rlProgress;
        View vHolder;
        public MyViewHolder(View view) {
            super(view);
             ctx = view.getContext();
              tl = (TokenLayout) view;
              imageView = view.findViewById(R.id.menu);
              mCode = view.findViewById(R.id.code);
              tv_reconnect = view.findViewById(R.id.tv_reconnect);
              rlProgress = view.findViewById(R.id.rl_code_progress);
              vHolder=view;
        }
    }

    public TokenAdapterRecycler(Context ctx, Communicator communicator, TokenPersistence tokenPersistence) {
        this.communicator = communicator;
        mTokenPersistence = tokenPersistence;
        mLayoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClipMan = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        mTokenCodes = new HashMap<>();
        this.context=ctx;
        dialog = new ProgressDialog(context);


        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
               mTokenCodes.clear();
            }

        });
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.token, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
                try {
                    final Token token = getItem(position);
                    companyInfoArrayList = ApplicationWrapper.getMdbCompanyAdapter().readCompanyData();
                    holder.tl.bind(token, companyInfoArrayList, R.menu.token, new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_delete:
                                    String[] labelU = token.getLabel().split("\\|");
                                    communicator.setAction(labelU.length == 2 ? labelU[1] : "", position);
                                    break;
                            }
                            return true;
                        }
                    });
                      String[] labelWithU = token.getLabel().split("\\|");
                       String reconnect= labelWithU[6];
                      holder.tv_reconnect.setVisibility((reconnect != null && reconnect.equals("true")? View.VISIBLE : View.GONE));

                    holder.mCode.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                    TokenPersistence tp = new TokenPersistence(ctx);
//                    // Increment the token.
//                    Token token = tp.get(position);
                            TokenCode codes = token.generateCodes();
//                    //save token. Image wasn't changed here, so just save it in sync
//                    new TokenPersistence(ctx).save(token);
                            // Copy code to clipboard.
                            mClipMan.setPrimaryClip(ClipData.newPlainText(null, codes.getCurrentCode()));
                            Toast.makeText(v.getContext().getApplicationContext(),
                                    R.string.code_copied,
                                    Toast.LENGTH_SHORT).show();
//                    mTokenCodes.put(token.getID(), codes);
//                    tl.start(token.getType(), codes, true);
                        }
                    });
                    holder.tv_reconnect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                dialog.setMessage("Please wait.....");
                                dialog.show();
                                dialog.setCancelable(false);
                                String[] labelU = token.getLabel().split("\\|");
                                String data[] = labelU[5].split("\\-");
                                String header = data[1];
                                String companyName = labelU.length >= 1 ? labelU[0] : "";
                                String userName = labelU.length >= 2 ? labelU[1] : "";
                                String role = labelU.length >= 3 ? labelU[2] : "";
                                String companyID = labelU.length >= 4 ? labelU[3] : "";
                                String userID = labelU.length >= 5 ? labelU[4] : "";
                                String hostName = labelU.length >= 6 ? labelU[5] : "";
                                String label = String.format("%s|%s|%s|0|%s|%s|%s", companyName, userName, role, userID, hostName,"false");
                                String oldtemp = String.format("otpauth://totp/%s:%s?secret=%s&digits=6&period=30", token.getIssuer(), label, token.getSecret());
                                addTokenAndFinish(oldtemp, position);

                                Utils.UpdatePublicKey(labelU[4], TokenAdapterRecycler.this::onJSONObjectListenerReconnect, context, labelU[5], header);


                            }catch (Exception e){
                                Logger.error("Token Adapter Recycler",e.getMessage());
                            }
                        }
                    });


                    holder.imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] labelWithU = token.getLabel().split("\\|");
                            String reconnect= labelWithU[6];
                            if (!reconnect.equals("true")) {
                                if (holder.mCode.getVisibility() == View.VISIBLE) {
                                    holder.mCode.setVisibility(View.GONE);
                                    holder.rlProgress.setVisibility(View.GONE);
                                    holder.imageView.setImageResource(R.drawable.ic_action_down);
                                    return;
                                } else {
                                    if (tvPrevious != null) {
                                        tvPrevious.setVisibility(View.GONE);
                                        rlProgressPrevious.setVisibility(View.GONE);
                                        imageViewPrevious.setImageResource(R.drawable.ic_action_down);
                                    }
                                    holder.mCode.setVisibility(View.VISIBLE);
                                    holder.rlProgress.setVisibility(View.VISIBLE);
                                    holder.imageView.setImageResource(R.drawable.ic_action_up);
                                    tvPrevious = holder.mCode;
                                    rlProgressPrevious = holder.rlProgress;
                                    imageViewPrevious = holder.imageView;
                                }

                                TokenPersistence tp = new TokenPersistence(holder.ctx);

                                // Increment the token.
                                Token token = tp.get(position);
                                TokenCode codes = token.generateCodes();
                                //save token. Image wasn't changed here, so just save it in sync
                                new TokenPersistence(holder.ctx).save(token);

                                // Copy code to clipboard.
//                    mClipMan.setPrimaryClip(ClipData.newPlainText(null, codes.getCurrentCode()));

//
                                mTokenCodes.put(token.getID(), codes);
                                holder.tl.start(token.getType(), codes, true);
                            }else{
                                Toast.makeText(context, "Please click Reconnect to activate your account.", Toast.LENGTH_SHORT).show();
                            }
                            // TokenCode tc = mTokenCodes.get(token.getID());
//                if (tc != null && tc.getCurrentCode() != null)
//                    tl.start(token.getType(), tc, true);
                        }
                    });

                    TokenCode tc = mTokenCodes.get(token.getID());
                    if (tc != null && tc.getCurrentCode() != null)
                        holder.tl.start(token.getType(), tc, false);
                }catch (Exception e){
                    Logger.error("bindView(View view, final int position)",e.getMessage());
                }


    }
    public Token getItem(int position) {
        return mTokenPersistence.get(position);
    }

    @Override
    public int getItemCount() {
        return mTokenPersistence.length();
    }

    public void removeItem(int position) {
        mTokenPersistence.delete(position);
        notifyItemRemoved(position);
    }

    private void restoreAccounts(String activate) {
        int position = -1;
        try {
            for(int i=0;i<mTokenPersistence.length();i++) {
                position=i;
                Token tokenTemp = mTokenPersistence.get(i);
                String[] labelU = tokenTemp.getLabel().split("\\|");
                String companyName = labelU.length >= 1 ? labelU[0] : "";
                String userName = labelU.length >= 2 ? labelU[1] : "";
                String role = labelU.length >= 3 ? labelU[2] : "";
                String companyID = labelU.length >= 4 ? labelU[3] : "";
                String userID = labelU.length >= 5 ? labelU[4] : "";
                String hostName = labelU.length >= 6 ? labelU[5] : "";
                String label = String.format("%s|%s|%s|0|%s|%s|%s", companyName, userName, role, userID, hostName,activate);
                String oldtemp = String.format("otpauth://totp/%s:%s?secret=%s&digits=6&period=30", tokenTemp.getIssuer(), label, tokenTemp.getSecret());
                addTokenAndFinish(oldtemp, position);
            }
        }catch (Exception e){
            Logger.error("Token Adapter Recycler","user version null");

        }
    }

    private void addTokenAndFinish(String temp, int pos) {
        try {
            Token  token = new Token(temp);
            if (new TokenPersistence(context).tokenExists(token)) {
                if (UserActivity.isUserIn) {
                    TokenPersistence mTokenPersistence = new TokenPersistence(context);
                    for (int i = 0; i < mTokenPersistence.length(); i++) {
                        Token tokenTemp = mTokenPersistence.get(i);
                        if (tokenTemp.getLabel().equals(token.getLabel())) {
                            new TokenPersistence(context).delete(i);
                            break;
                        }
                    }
                }
            }else{
                TokenPersistence mTokenPersistence = new TokenPersistence(context);
                for (int i = 0; i < mTokenPersistence.length(); i++) {
                    Token tokenTemp = mTokenPersistence.get(i);
                    String[] labelU = tokenTemp.getLabel().split("\\|");
                    String userId = labelU.length >= 4 ? labelU[4] : "";

                    String[] templabelU = token.getLabel().split("\\|");
                    String tempuserId = templabelU.length >= 4 ? templabelU[4] : "";

                    if (userId.equals(tempuserId)) {
                        new TokenPersistence(context).delete(i);
                        break;
                    }
                }
            }

            TokenPersistence.saveAsync(context, token);
        } catch (Exception e) {
            Logger.error(" addTokenAndFinish(String temp)", e.getMessage());
        }
    }
}
