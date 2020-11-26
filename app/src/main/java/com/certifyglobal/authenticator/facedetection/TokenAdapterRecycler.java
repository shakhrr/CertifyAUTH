package com.certifyglobal.authenticator.facedetection;
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
import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.Token;
import com.certifyglobal.authenticator.TokenCode;
import com.certifyglobal.authenticator.TokenLayout;
import com.certifyglobal.authenticator.TokenPersistence;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenAdapterRecycler extends RecyclerView.Adapter<TokenAdapterRecycler.MyViewHolder> {

    private TokenPersistence mTokenPersistence;
    private  LayoutInflater mLayoutInflater;
    private  HashMap<String,byte[]> companyInfoArrayList;
    private ClipboardManager mClipMan;
    private RelativeLayout rlProgressPrevious;
    private ImageView imageViewPrevious;
    private  Map<String, TokenCode> mTokenCodes;
    private Communicator communicator;
    private TextView tvPrevious;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private Context ctx;
        TokenLayout tl;
        ImageView imageView;
        TextView mCode;
        RelativeLayout rlProgress;
        View vHolder;
        public MyViewHolder(View view) {
            super(view);
             ctx = view.getContext();
              tl = (TokenLayout) view;
              imageView = view.findViewById(R.id.menu);
              mCode = view.findViewById(R.id.code);
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


                    holder.imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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

}
