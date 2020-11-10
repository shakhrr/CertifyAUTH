

package com.certifyglobal.authenticator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BaseReorderableAdapter extends BaseAdapter {


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            int type = getItemViewType(position);
            convertView = createView(parent, type);

        }
        bindView(convertView, position);
        return convertView;
    }

    protected abstract void move(int fromPosition, int toPosition);

    protected abstract void bindView(View view, int position);

    protected abstract View createView(ViewGroup parent, int type);
}
