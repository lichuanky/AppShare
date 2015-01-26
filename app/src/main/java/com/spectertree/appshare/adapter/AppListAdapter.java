package com.spectertree.appshare.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.spectertree.appshare.R;
import com.spectertree.appshare.model.AppInfoData;
import com.spectertree.appshare.util.Utils;

public class AppListAdapter extends ArrayAdapter<AppInfoData> {

	private class ItemViewHolder1 {
		ImageView appIcon;
		ImageView appLocationIcon;
		TextView appNameText;
		TextView installTimeText;
		TextView appSizeText;
		CheckBox checkbox;
	}

    public Map<Integer, Boolean> mMap;

    private LayoutInflater mInflater;
	private List<AppInfoData> mList;
	private List<AppInfoData> mNotInstalledApps = null;
	private boolean mIsHighlightNotInstalled = false;
    private boolean mIsShowLocationIcon = true;

	public AppListAdapter(Context context, List<AppInfoData> list) {
		super(context, 0, list);
        mList = list;
        mInflater = LayoutInflater.from(context);
        if (mMap == null) {
            mMap = new HashMap<Integer, Boolean>(list.size());
            for(int i = 0; i < list.size(); i++){
                mMap.put(i, false);
            }
        }
	}

	public AppListAdapter(Context context, List<AppInfoData> list, boolean isShowLocationIcon) {
        super(context, 0, list);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mIsHighlightNotInstalled = prefs.getBoolean(Utils.Setting.KEY_HEIGHLIGHT_NOT_INSTALLED, false);
//        if (mIsHighlightNotInstalled) {
//            mNotInstalledApps = getNotInstalledApp(context, list);
//        }

        mList = list;
        mIsShowLocationIcon = isShowLocationIcon;
        mInflater = LayoutInflater.from(context);
        if (mMap == null) {
            mMap = new HashMap<Integer, Boolean>(list.size());
            for(int i = 0; i < list.size(); i++){
                mMap.put(i, false);
            }
        }
    }

    @Override
    public boolean hasStableIds() {
    	return super.hasStableIds();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	ItemViewHolder1 holder;
    	if (convertView == null) {
    		convertView = mInflater.inflate(R.layout.list_item, null);
    		holder = new ItemViewHolder1();
    		holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
    		holder.appNameText = (TextView) convertView.findViewById(R.id.app_name_text);
    		holder.installTimeText = (TextView) convertView.findViewById(R.id.install_date_text);
    		holder.appLocationIcon = (ImageView) convertView.findViewById(R.id.app_location_img);
    		holder.appSizeText = (TextView) convertView.findViewById(R.id.app_size_text);
    		holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
    		convertView.setTag(holder);
    	} else {
    		holder = (ItemViewHolder1) convertView.getTag();
    	}
    	AppInfoData appInfo = mList.get(position);
        holder.appIcon.setImageDrawable(appInfo.getAppicon());
        holder.appNameText.setText(appInfo.getAppname());
        holder.installTimeText.setText(DateFormat.format("yyyy-MM-dd", appInfo.getAppdate()));
        holder.appLocationIcon.setImageResource((appInfo.getAppflags()
                &ApplicationInfo.FLAG_EXTERNAL_STORAGE)!=0 ? R.drawable.sdcard : R.drawable.phone);
        holder.appSizeText.setText(Utils.getFormatAppSize(appInfo.getAppsize()));
        holder.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mMap.put(position, isChecked);
			}
		});
        holder.checkbox.setChecked(mMap.containsKey(position) ? mMap.get(position) : false);

        if (!mIsShowLocationIcon) {
            holder.appLocationIcon.setVisibility(View.GONE);
        }
//        if (mNotInstalledApps != null) {
//            for (AppInfoData ai : mNotInstalledApps) {
//                if (ai.getApppackagename().equals(appInfo.getApppackagename())) {
//                    holder.appNameText.setTextColor(android.graphics.Color.RED);
//                }
//            }
//        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private List<AppInfoData> getNotInstalledApp(Context ctx, List<AppInfoData> apkFilesInSdcard) {
        PackageManager pm = ctx.getPackageManager();
        List<AppInfoData> list = new ArrayList<AppInfoData>();
        List<PackageInfo> installedApps = pm.getInstalledPackages(PackageManager.GET_META_DATA);
        AppInfoData appInfoData = null;
        boolean isInstalled = false;
        for (AppInfoData ai : apkFilesInSdcard) {
            for (PackageInfo pi : installedApps) {
                appInfoData = new AppInfoData();
                appInfoData.setAppdate(new Date((new File(pi.applicationInfo.sourceDir))
                        .lastModified()));
                appInfoData.setAppdatasize((new File(pi.applicationInfo.dataDir)).length());
                appInfoData.setAppSourcedir(pi.applicationInfo.sourceDir);
                appInfoData.setAppicon(pi.applicationInfo.loadIcon(pm));
                appInfoData.setAppname(pi.applicationInfo.loadLabel(pm).toString());
                appInfoData.setApppackagename(pi.applicationInfo.packageName);
                appInfoData.setAppsize((new File(pi.applicationInfo.sourceDir)).length());
                appInfoData.setAppflags(pi.applicationInfo.flags);
                appInfoData.setAppversion(pi.versionName);
                appInfoData.setAppversioncode(pi.versionCode);

                if ((appInfoData.getAppflags()&ApplicationInfo.FLAG_SYSTEM)==0) {
                    if (appInfoData.getApppackagename().equals(ai.getApppackagename()) &&
                            appInfoData.getAppversioncode() >= ai.getAppversioncode()) {
                            isInstalled = true;
                    }
                }
            }
            if (!isInstalled) {
                list.add(ai);
            }
            isInstalled = false;
        }

        return list;
    }
}
