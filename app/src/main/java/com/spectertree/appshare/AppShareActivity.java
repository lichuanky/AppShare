package com.spectertree.appshare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.spectertree.appshare.adapter.AppListAdapter;
import com.spectertree.appshare.model.AppInfoData;
import com.spectertree.appshare.util.Utils;

public class AppShareActivity extends ListActivity {
    private static final String TAG = AppShareActivity.class.getSimpleName();

    private static final int LIST_ALL_APP = 0;
    private static final int LIST_USER_APP = 1;
    private static final int LIST_SYSTEM_APP = 2;

    private static final int MENU_FILTER = 10;
    private static final int MENU_SORTING = 11;
    private static final int MENU_SELECT_ALL = 12;
    private static final int MENU_SELECT_CLEAR = 13;
    private static final int MENU_SETTING = 15;
    private static final int MENU_UNINSTALL = 16;
    private static final int MENU_REFRESH = 17;

    private static final int CONTEXT_MENU_SHARE = 20;
    private static final int CONTEXT_MENU_OPEN = 21;
    private static final int CONTEXT_MENU_UNINSTALL = 22;
    private static final int CONTEXT_MENU_SEARCH = 23;
    private static final int CONTEXT_MENU_EXPORT = 24;
    private static final int CONTEXT_MENU_DETAIL = 25;

    private static final int DIALOG_FILTER = 30;
    private static final int DIALOG_SORTING = 31;

    private static final int MSG_CREATE_LIST = 40;
    private static final int MSG_UPDATE_LIST = 41;
    private static final int MSG_UPDATE_PROCESS_DIALOG_MSG = 43;

    private AppListAdapter mAdapter;
    private List<AppInfoData> mAppList;
    private PackageManager mPackageManager;
    private SharedPreferences mPrefs;
    private ListView mListView;
    private String mFilterType;
    private String mSortingType;
    private boolean mIsNeedUpdateList;
    private Toast mToast;
    private ProgressDialog mProgressDialog;

    private Thread mThreadLoadApps = new Thread() {
        public void run() {
            mFilterType = mPrefs.getString(Utils.Setting.KEY_DEFAULT_FILTER, Utils.Setting.DEFAULT_FILTER);
            mSortingType = mPrefs.getString(Utils.Setting.KEY_DEFAULT_SORTING, Utils.Setting.DEFAULT_SORTING);

            if (mFilterType.equals(Utils.Setting.FILTER_TYPE_ALL))
                mAppList = getAppList(LIST_ALL_APP);
            else if (mFilterType.equals(Utils.Setting.FILTER_TYPE_SYSTEM))
                mAppList = getAppList(LIST_SYSTEM_APP);
            else if (mFilterType.equals(Utils.Setting.FILTER_TYPE_USER))
                mAppList = getAppList(LIST_USER_APP);

            Utils.changeSort(mAppList, mSortingType);
            if (mAdapter == null) { // CREATE LIST
                mHandler.sendEmptyMessage(MSG_CREATE_LIST);
            } else { // UPDATE LIST
                mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CREATE_LIST:
                	mAdapter = new AppListAdapter(AppShareActivity.this, mAppList);
                    setListAdapter(mAdapter);
                    setTitle(getString(R.string.title, mAppList.size()));
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    break;
                case MSG_UPDATE_LIST:
                    mAdapter.notifyDataSetChanged();
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    break;
                case MSG_UPDATE_PROCESS_DIALOG_MSG:
                    mProgressDialog.setMessage(msg.obj.toString());
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText)findViewById(R.id.search_text)).setText("");
            }
        });
        findViewById(R.id.setting_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //openOptionsMenu();
                startActivity(new Intent(AppShareActivity.this, ApkFileManager.class));
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AppInfoData> list = new ArrayList<AppInfoData>();
                if (mAdapter.mMap.containsValue(true)) {
                    for(Integer i : mAdapter.mMap.keySet()) {
                        if (mAdapter.mMap.get(i)) {
                            list.add(mAppList.get(i));
                            mAdapter.mMap.put(i, false);
                        }
                    }
                } else {

                }
                shareApp(list);
                mAdapter.notifyDataSetChanged();
            }
        });
        findViewById(R.id.export_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.mMap.containsValue(true)) {
                    Thread exportThread = new Thread() {
                        public void run() {
                            List<AppInfoData> list = new ArrayList<AppInfoData>();
                            for (Integer i : mAdapter.mMap.keySet()) {
                                if (mAdapter.mMap.get(i)) {
                                    list.add(mAppList.get(i));
                                    mAdapter.mMap.put(i, false);
                                }
                            }
                            for (int i = 0, n = list.size(); i < n; i++) {
                                exportApp(list.get(i));
                                Message msg = mHandler.obtainMessage(MSG_UPDATE_PROCESS_DIALOG_MSG);
                                msg.obj = getString(R.string.dialog_progress_export_content,
                                        list.get(i).getAppname(), i + 1, n);
                                msg.sendToTarget();
                            }

                            mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
                        }

                    };

                    mProgressDialog.setTitle(getString(R.string.dialog_progress_export_title));
                    exportThread.start();
                }
            }
        });

        mPackageManager = getPackageManager();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mListView = getListView();
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setTextFilterEnabled(true);
        registerForContextMenu(mListView);

        mThreadLoadApps.start();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        mProgressDialog.setTitle(getString(R.string.dialog_progress_load_title));
        mProgressDialog.setMessage(getString(R.string.dialog_progress_load_content));
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsNeedUpdateList) {
            mProgressDialog.setTitle(getString(R.string.dialog_progress_load_title));
            mProgressDialog.setMessage(getString(R.string.dialog_progress_load_content));
            mThreadLoadApps.run();
            mIsNeedUpdateList = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, MENU_FILTER, 0, R.string.menu_filter);
        menu.add(0, MENU_SORTING, 0, R.string.menu_sorting).setIcon(R.drawable.ic_menu_sort_alphabetically);
        menu.add(0, MENU_SELECT_ALL, 0, R.string.menu_select_all).setIcon(R.drawable.ic_menu_agenda);
        menu.add(0, MENU_SETTING, 4, R.string.menu_setting).setIcon(R.drawable.ic_menu_preferences);
//        menu.add(0, MENU_ABOUT, 5, R.string.menu_about).setIcon(R.drawable.ic_menu_info_details);
        menu.add(0, MENU_SELECT_CLEAR, 1, R.string.menu_select_clear).setIcon(R.drawable.ic_menu_clear_playlist);
        menu.add(0, MENU_UNINSTALL, 2, R.string.uninstall).setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, MENU_REFRESH, 3, R.string.refresh).setIcon(R.drawable.ic_menu_refresh);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_FILTER:
                showDialog(DIALOG_FILTER);
                break;
            case MENU_SORTING:
                showDialog(DIALOG_SORTING);
                break;
            case MENU_SELECT_ALL:
                for (int i = 0; i < mAppList.size(); i++) {
                    mAdapter.mMap.put(i, true);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_SETTING:
                startActivityForResult(new Intent(this, SettingActivity.class),
                        Utils.Constants.REQ_SYSTEM_SETTINGS);
                break;
            case MENU_SELECT_CLEAR:
                for (int i = 0; i < mAppList.size(); i++) {
                    mAdapter.mMap.put(i, false);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_UNINSTALL:
                if (mAdapter.mMap.containsValue(true)) {
                    for(Integer i : mAdapter.mMap.keySet()) {
                        if (mAdapter.mMap.get(i)) {
                            uninstallApp(mAppList.get(i));
                            mAdapter.mMap.put(i, false);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case MENU_REFRESH:
                mThreadLoadApps.run();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_SHARE, 0, R.string.context_menu_share);
        menu.add(0, CONTEXT_MENU_OPEN, 0, R.string.context_menu_display);
        menu.add(0, CONTEXT_MENU_UNINSTALL, 0, R.string.context_menu_uninstall);
        menu.add(0, CONTEXT_MENU_SEARCH, 0, R.string.context_menu_search);
        menu.add(0, CONTEXT_MENU_EXPORT, 0, R.string.context_menu_export);
        menu.add(0, CONTEXT_MENU_DETAIL, 0, R.string.context_menu_detail);

        AppInfoData ai = mAppList.get(((AdapterView.AdapterContextMenuInfo)menuInfo).position);
        menu.setHeaderTitle(getString(R.string.menu_header_app, ai.getAppname()));
        menu.setHeaderIcon(ai.getAppicon());
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        AppInfoData ai = mAppList.get(menuInfo.position);
        switch(item.getItemId()) {
            case CONTEXT_MENU_SHARE:
                shareApp(ai);
                break;
            case CONTEXT_MENU_OPEN:
                displayApp(ai);
                break;
            case CONTEXT_MENU_UNINSTALL:
                uninstallApp(ai);
                break;
            case CONTEXT_MENU_SEARCH:
                break;
            case CONTEXT_MENU_EXPORT:
                if (!TextUtils.isEmpty(exportApp(ai))) {
                    mToast.setText(getString(R.string.export_app_success, ai.getAppname()));
                    mToast.show();
                } else {
                    mToast.setText(getString(R.string.export_app_failed, ai.getAppname()));
                    mToast.show();
                }
                break;
            case CONTEXT_MENU_DETAIL:
                Intent intent = new Intent();
                final int apiLevel = Build.VERSION.SDK_INT;
                if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    Uri uri = Uri.fromParts("package", ai.getApppackagename(), null);
                    intent.setData(uri);
                } else { // Blow 2.3 ，use not public interface
                         // 2.2 and 2.1，InstalledAppDetails use the differen APP_PKG_NAME。
                    final String appPkgName = (apiLevel == 8 ? "pkg" :
                        "com.android.settings.ApplicationPkgName");
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra(appPkgName, ai.getApppackagename());
                }
                startActivity(intent);
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.Constants.REQ_SYSTEM_SETTINGS) {
            Log.d(TAG, "resultCode : " + resultCode);
            Log.d(TAG, "data : " + data);
            if (data != null) {
                String filter = data.getStringExtra(Utils.Setting.KEY_DEFAULT_FILTER);
                String sorting = data.getStringExtra(Utils.Setting.KEY_DEFAULT_SORTING);
                if (!TextUtils.isEmpty(sorting) && !TextUtils.isEmpty(filter)) {
                    mFilterType = filter;
                    mSortingType = sorting;
                    filterByInstallType(filter);
                    Utils.changeSort(mAppList, mSortingType);
                }
                if (!TextUtils.isEmpty(filter)) {
                    mFilterType = filter;
                    filterByInstallType(filter);
                }
                if (!TextUtils.isEmpty(sorting)){
                    mSortingType = sorting;
                    Utils.changeSort(mAppList, mSortingType);
                }
                mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        AppInfoData ai = mAppList.get(position);
        String itemClickEvent = mPrefs.getString(Utils.Setting.KEY_DEFAULT_ITEM_CLICK,
                Utils.Setting.DEFAULT_ITEM_CLICK);
        if (itemClickEvent.equals("share")) {
            shareApp(ai);
        } else if (itemClickEvent.equals("display")) {
            displayApp(ai);
        } else if (itemClickEvent.equals("uninstall")) {
            uninstallApp(ai);
        } else if (itemClickEvent.equals("export")) {
            exportApp(ai);
        } else if (itemClickEvent.equals("select")) {
            mAdapter.mMap.put(position, true);
            mAdapter.notifyDataSetChanged();
        }

        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
            case DIALOG_FILTER:
                dialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_filter_title).setSingleChoiceItems(
                                R.array.app_filters, 0,
                                new DialogInterface.OnClickListener() {
                                    String[] filterTypes = getResources().getStringArray(R.array.app_filters_value);
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mFilterType = filterTypes[which];
                                        filterByInstallType(mFilterType);
                                        dialog.dismiss();
                                    }
                                }).create();
                return dialog;
            case DIALOG_SORTING:
                dialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(R.string.dialog_sorting_title)
                        .setSingleChoiceItems(R.array.app_sortings, 0,
                                new DialogInterface.OnClickListener() {
                                    String[] sortTypes = getResources().getStringArray(R.array.app_sortings_value);
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSortingType = sortTypes[which];
                                        Utils.changeSort(mAppList, mSortingType);
                                        mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
                                        dialog.dismiss();
                                    }
                                })
                        .create();
                return dialog;
            default:
                break;
        }
        return super.onCreateDialog(id);
    }

    private List<AppInfoData> getAppList(int type) {
        List<PackageInfo> list = mPackageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        List<AppInfoData> subList = new ArrayList<AppInfoData>();

        List<AppInfoData> appList = new ArrayList<AppInfoData>();
        AppInfoData appInfoData = null;
        for (PackageInfo pi : list) {
        	appInfoData = new AppInfoData();
        	appInfoData.setAppdate(new Date((new File(pi.applicationInfo.sourceDir)).lastModified()));
        	appInfoData.setAppdatasize((new File(pi.applicationInfo.dataDir)).length());
        	appInfoData.setAppSourcedir(pi.applicationInfo.sourceDir);
        	appInfoData.setAppicon(pi.applicationInfo.loadIcon(mPackageManager));
        	appInfoData.setAppname(pi.applicationInfo.loadLabel(mPackageManager).toString());
        	appInfoData.setApppackagename(pi.applicationInfo.packageName);
        	appInfoData.setAppsize((new File(pi.applicationInfo.sourceDir)).length());
        	appInfoData.setAppflags(pi.applicationInfo.flags);
        	appInfoData.setAppversion(pi.versionName);
			appInfoData.setAppversioncode(pi.versionCode);
			appList.add(appInfoData);
        }
        if (type == LIST_USER_APP) {
            for (AppInfoData ai : appList) {
                if ((ai.getAppflags()&ApplicationInfo.FLAG_SYSTEM)==0)
                    subList.add(ai);
            }
        } else if (type == LIST_SYSTEM_APP) {
            for (AppInfoData ai : appList) {
                if ((ai.getAppflags()&ApplicationInfo.FLAG_SYSTEM)!=0)
                    subList.add(ai);
            }
        } else if (type == LIST_ALL_APP) {
            subList = appList;
        }
        //return subList;
        return subList;
    }

    private void filterByInstallType(String filterType) {
        if (filterType.equals("all")) {
            mAppList = getAppList(LIST_ALL_APP);
        } else if (filterType.equals("user")) {
            mAppList = getAppList(LIST_USER_APP);
        } else if (filterType.equals("system")) {
            mAppList = getAppList(LIST_SYSTEM_APP);
        }
    }

    private void shareApp(List<AppInfoData> list) {
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (AppInfoData ai : list) {
            uris.add(Uri.parse("file://" + exportApp(ai)));
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app));
//        intent.putExtra(Intent.EXTRA_SUBJECT, ai.loadLabel(mPackageManager));
//        intent.putExtra(Intent.EXTRA_TEXT, text);
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(ai.sourceDir));
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setType(Utils.Constants.MIME_APP);
        startActivity(Intent.createChooser(intent, "Choose way to share"));
    }

    private void shareApp(AppInfoData ai) {
        Log.d(TAG, "shareApp(), url : " + ai);
        String exportAppPath = exportApp(ai);
        exportAppPath = "file://" + exportAppPath;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_SUBJECT, ai.getAppname());
//        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(exportAppPath));
        intent.setType(Utils.Constants.MIME_APP);
        startActivity(Intent.createChooser(intent, "Choose way to share"));
    }

    private String exportApp(AppInfoData ai) {
        if (!Utils.isSdcardExist()) {
            return "";
        }

        String backupDir = mPrefs.getString(Utils.Setting.KEY_BAKCUP_DIR,
                Utils.Setting.DEFAULT_BACKUP_DIR);
        backupDir = (backupDir.endsWith("/")) ? backupDir : (backupDir + "/");
        File file = new File("mnt" + backupDir);
        String apkName = "";

        if (!file.isDirectory())
            if (!file.mkdirs())
                return "";
        String formatName = mPrefs.getString(Utils.Setting.KEY_FORMAT_EXPORT_NAME,
                Utils.Setting.DEFAULT_FORMAT_EXPORT_NAME);
        try {
            if (Utils.Setting.FORMAT_EXPORT_NAME_APPNAME.equals(formatName))
                apkName = ai.getAppname() + "_" + ai.getAppversion() + ".apk";
            else
                apkName = ai.getApppackagename() + ".apk";

            if (!Utils.isFileExist(backupDir, apkName)) {
                Utils.copyFile(new File(ai.getAppSourcedir()), new File("mnt" + backupDir + apkName));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return backupDir + apkName;
    }

    private void uninstallApp(AppInfoData ai) {
        String uri = "package:" + ai.getApppackagename();
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, Uri.parse(uri));
        startActivity(uninstallIntent);

        mIsNeedUpdateList = true;
    }

    private void displayApp(AppInfoData ai) {
        Intent intent = mPackageManager.getLaunchIntentForPackage(ai.getApppackagename());
        startActivity(intent);
    }

}