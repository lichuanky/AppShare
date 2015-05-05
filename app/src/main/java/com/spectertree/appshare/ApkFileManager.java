
package com.spectertree.appshare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.spectertree.appshare.adapter.AppListAdapter;
import com.spectertree.appshare.model.AppInfoData;
import com.spectertree.appshare.util.Utils;

public class ApkFileManager extends ListActivity {
    private static final String TAG = ApkFileManager.class.getSimpleName();
    private static final String FILE_TYPE_APK = "apk";

    private static final int MENU_DELETE = 1;
    private static final int MENU_SELECT_ALL = 2;
    private static final int MENU_SELECT_CLEAR = 3;
    private static final int MENU_INSTALL = 4;
    private static final int MENU_SETTING = 5;
    private static final int MENU_SORTING = 6;

    private static final int MSG_CREATE_LIST = 10;
    private static final int MSG_UPDATE_LIST = 11;

    private static final int DIALOG_PROCESS = 31;
    private static final int DIALOG_SORTING = 32;

    private List<File> mFileList;
    private SharedPreferences mPrefs;
    private ListView mListView;
    private AppListAdapter mAdapter;
    private List<AppInfoData> mAppInfoList;

    private Thread mThreadScanApk = new Thread() {
        public void run() {
            Log.d(TAG, "start to scan apk.");
            String sourceDir = mPrefs.getString(Utils.Setting.KEY_BAKCUP_DIR,
                    Utils.Setting.DEFAULT_BACKUP_DIR);
            try {
                mFileList = Utils.getFiles(sourceDir, FILE_TYPE_APK);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mAppInfoList == null)
                mAppInfoList = new ArrayList<AppInfoData>();
            else
                mAppInfoList.clear();

            for (int i = 0, n = mFileList.size(); i < n; i++) {
                AppInfoData ai = Utils.getApkFileInfo(ApkFileManager.this, mFileList.get(i).getPath());
                if (ai != null)
                    mAppInfoList.add(ai);
            }
            if (mAdapter == null) {
                mAdapter = new AppListAdapter(ApkFileManager.this, mAppInfoList, false);
                mHandler.sendEmptyMessage(MSG_CREATE_LIST);
            } else {
                mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CREATE_LIST:
                    setListAdapter(mAdapter);
                    dismissDialog(DIALOG_PROCESS);
                    break;
                case MSG_UPDATE_LIST:
                    if (mAppInfoList.size() != mAdapter.mMap.size()) {
                        mAdapter.mMap.clear();
                        for (int i = 0; i < mAppInfoList.size(); i++) {
                            mAdapter.mMap.put(i, false);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    dismissDialog(DIALOG_PROCESS);
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_file_manager);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mThreadScanApk.start();
        showDialog(DIALOG_PROCESS);

        mListView = getListView();
        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setFastScrollEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SORTING, 0, R.string.menu_sorting).setIcon(R.drawable.ic_menu_sort_alphabetically);
        menu.add(0, MENU_SELECT_ALL, 0, R.string.select_all).setIcon(R.drawable.ic_menu_agenda);
        menu.add(0, MENU_SELECT_CLEAR, 0, R.string.select_clear).setIcon(R.drawable.ic_menu_clear_playlist);
        menu.add(0, MENU_DELETE, 0, R.string.delete).setIcon(R.drawable.ic_menu_delete);
        menu.add(0, MENU_INSTALL, 0, R.string.install);
        menu.add(0, MENU_SETTING, 0, R.string.menu_setting).setIcon(R.drawable.ic_menu_preferences);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE:
                List<AppInfoData> removeApps = new ArrayList<AppInfoData>();
                List<File> removeFiles = new ArrayList<File>();
                for (int i : mAdapter.mMap.keySet()) {
                    if (mAdapter.mMap.get(i)) {
                        mFileList.get(i).delete();
                        removeFiles.add(mFileList.get(i));
                        removeApps.add(mAppInfoList.get(i));
                        mAdapter.mMap.put(i, false);
                    }
                }
                mFileList.remove(removeFiles);
                mAppInfoList.removeAll(removeApps);
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_SELECT_ALL:
                for (int i : mAdapter.mMap.keySet()) {
                    mAdapter.mMap.put(i, true);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_SELECT_CLEAR:
                for (int i : mAdapter.mMap.keySet()) {
                    mAdapter.mMap.put(i, false);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_INSTALL:
                Intent in = new Intent(Intent.ACTION_VIEW);
                for (int i : mAdapter.mMap.keySet()) {
                    if (mAdapter.mMap.get(i)) {
                        in.setDataAndType(Uri.fromFile(mFileList.get(i)),
                                Utils.Constants.MIME_APP);
                        startActivity(in);
                        mAdapter.mMap.put(i, false);
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_SETTING:
                startActivityForResult(new Intent(this, SettingActivity.class), Utils.Constants.REQ_SYSTEM_SETTINGS);
                break;
            case MENU_SORTING:
                showDialog(DIALOG_SORTING);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
            case DIALOG_SORTING:
                dialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_sorting_title)
                        .setSingleChoiceItems(R.array.app_sortings, 0,
                                new DialogInterface.OnClickListener() {
                                    String[] sortTypes = getResources().getStringArray(R.array.app_sortings_value);

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Utils.changeSort(mAppInfoList, sortTypes[which]);
                                        mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
                                        dialog.dismiss();
                                    }
                                })
                        .create();
                return dialog;
            case DIALOG_PROCESS:
                return Utils.createProcessDialog(this, "Loading", "Loading ...");
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.Constants.REQ_SYSTEM_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    String apkFilesDir = data.getStringExtra(Utils.Setting.KEY_BAKCUP_DIR);
                    if (!TextUtils.isEmpty(apkFilesDir)) {
                        showDialog(DIALOG_PROCESS);
                        mThreadScanApk.run();
                    }
                }
                break;
            default:
                break;
        }
    }

}
