

package com.certifyglobal.authenticator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.certifyglobal.callback.Communicator;
import com.certifyglobal.pojo.CompanyInfo;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TokenAdapter extends BaseReorderableAdapter {
    private TokenPersistence mTokenPersistence;
    private final LayoutInflater mLayoutInflater;
    private  HashMap<String,byte[]> companyInfoArrayList;
    private final ClipboardManager mClipMan;
    private RelativeLayout rlProgressPrevious;
    private ImageView imageViewPrevious;
    private final Map<String, TokenCode> mTokenCodes;
    private Communicator communicator;
    private TextView tvPrevious;

    public TokenAdapter(Context ctx, Communicator communicator, TokenPersistence tokenPersistence) {
        this.communicator = communicator;
        mTokenPersistence = tokenPersistence;
        mLayoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClipMan = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        mTokenCodes = new HashMap<>();
        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mTokenCodes.clear();
            }

            @Override
            public void onInvalidated() {
                mTokenCodes.clear();
            }
        });
    }

    @Override
    public int getCount() {
        return mTokenPersistence.length();
    }

    @Override
    public Token getItem(int position) {
        return mTokenPersistence.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected void move(int fromPosition, int toPosition) {
        mTokenPersistence.move(fromPosition, toPosition);
        notifyDataSetChanged();
    }

    @Override
    protected void bindView(View view, final int position) {
        try {
            final Context ctx = view.getContext();
            final TokenLayout tl = (TokenLayout) view;
            final ImageView imageView = view.findViewById(R.id.menu);
            final TextView mCode = view.findViewById(R.id.code);
            final RelativeLayout rlProgress = view.findViewById(R.id.rl_code_progress);
            final Token token = getItem(position);
            companyInfoArrayList = ApplicationWrapper.getMdbCompanyAdapter().readCompanyData();
            tl.bind(token, companyInfoArrayList, R.menu.token, new PopupMenu.OnMenuItemClickListener() {
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


            mCode.setOnClickListener(new View.OnClickListener() {
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


            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCode.getVisibility() == View.VISIBLE) {
                        mCode.setVisibility(View.GONE);
                        rlProgress.setVisibility(View.GONE);
                        imageView.setImageResource(R.drawable.ic_action_down);
                        return;
                    } else {
                        if (tvPrevious != null) {
                            tvPrevious.setVisibility(View.GONE);
                            rlProgressPrevious.setVisibility(View.GONE);
                            imageViewPrevious.setImageResource(R.drawable.ic_action_down);
                        }
                        mCode.setVisibility(View.VISIBLE);
                        rlProgress.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.drawable.ic_action_up);
                        tvPrevious = mCode;
                        rlProgressPrevious = rlProgress;
                        imageViewPrevious = imageView;
                    }

                    TokenPersistence tp = new TokenPersistence(ctx);

                    // Increment the token.
                    Token token = tp.get(position);
                    TokenCode codes = token.generateCodes();
                    //save token. Image wasn't changed here, so just save it in sync
                    new TokenPersistence(ctx).save(token);

                    // Copy code to clipboard.
//                    mClipMan.setPrimaryClip(ClipData.newPlainText(null, codes.getCurrentCode()));

//
                    mTokenCodes.put(token.getID(), codes);
                    tl.start(token.getType(), codes, true);
                    // TokenCode tc = mTokenCodes.get(token.getID());
//                if (tc != null && tc.getCurrentCode() != null)
//                    tl.start(token.getType(), tc, true);
                }
            });

            TokenCode tc = mTokenCodes.get(token.getID());
            if (tc != null && tc.getCurrentCode() != null)
                tl.start(token.getType(), tc, false);
        }catch (Exception e){
            Logger.error("bindView(View view, final int position)",e.getMessage());
        }
    }

    @Override
    protected View createView(ViewGroup parent, int type) {
        return mLayoutInflater.inflate(R.layout.token, parent, false);
    }
}
