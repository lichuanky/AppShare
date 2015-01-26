package com.spectertree.appshare.adapter;

import com.spectertree.appshare.R;
import com.spectertree.appshare.util.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppAdapter extends BaseAdapter {
    public List<ApplicationInfo> mList;
    public Map<Integer, Boolean> mMap;
    private LayoutInflater mInflater;
    private PackageManager mPackageManager;

    @Override
    public boolean hasStableIds() {
        return super.hasStableIds();
    }

    public AppAdapter(List<ApplicationInfo> list, Context context) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mPackageManager = context.getPackageManager();
        if (mMap == null) {
            mMap = new HashMap<Integer, Boolean>(list.size());
            for(int i = 0; i < list.size(); i++){
                mMap.put(i, false);
            }
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    private class ItemViewHolder {
        ImageView appIcon;
        TextView appNameText;
        TextView installTimeText;
        TextView appSizeText;
        CheckBox checkbox;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ItemViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ItemViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appNameText = (TextView) convertView.findViewById(R.id.app_name_text);
            holder.installTimeText = (TextView) convertView.findViewById(R.id.install_date_text);
            holder.appSizeText = (TextView) convertView.findViewById(R.id.app_size_text);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
        }
        ApplicationInfo appInfo = mList.get(position);
        holder.appIcon.setImageDrawable(appInfo.loadIcon(mPackageManager));
        holder.appNameText.setText(appInfo.loadLabel(mPackageManager));
        holder.installTimeText.setText(Utils.getAppInstallTime(appInfo));
        holder.appSizeText.setText(Utils.getAppSize(appInfo));
        holder.checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.put(position, ((CheckBox)v).isChecked());
            }
        });
        holder.checkbox.setChecked(mMap.get(position));

        return convertView;
    }
}

class ItemViewHolder {
    ImageView appIcon;
    TextView appNameText;
    TextView installTimeText;
    TextView appSizeText;
    CheckBox checkbox;
}
