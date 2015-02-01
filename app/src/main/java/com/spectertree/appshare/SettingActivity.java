package com.spectertree.appshare;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.spectertree.appshare.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingActivity extends SherlockPreferenceActivity {

    private SharedPreferences mPreferences;

    EditTextPreference mEditBackupDir;
    ListPreference mListDefaultFilter;
    ListPreference mListDefaultSorting;
    ListPreference mListDefaultItemClick;
    ListPreference mListFormatExportName;
    Intent mIntent;

    private OnPreferenceChangeListener mChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference prefs, Object newValue) {
            prefs.setSummary(newValue.toString());
            mIntent.putExtra(prefs.getKey(), newValue.toString());
            setResult(Activity.RESULT_OK, mIntent);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        mIntent = new Intent();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditBackupDir = (EditTextPreference) findPreference(Utils.Setting.KEY_BAKCUP_DIR);
        mEditBackupDir.setSummary(mPreferences.getString(Utils.Setting.KEY_BAKCUP_DIR,
                Utils.Setting.DEFAULT_BACKUP_DIR));
        mEditBackupDir.setOnPreferenceChangeListener(mChangeListener);
        mEditBackupDir.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent in = new Intent(SettingActivity.this, DirectorySelectActivity.class);
                startActivityForResult(in, Utils.Constants.REQ_BACKUP_DERICTORY);
                return true;
            }
        });

        mListDefaultFilter = (ListPreference) findPreference(Utils.Setting.KEY_DEFAULT_FILTER);
        mListDefaultFilter.setOnPreferenceChangeListener(mChangeListener);
        mListDefaultFilter.setSummary(mPreferences.getString(Utils.Setting.KEY_DEFAULT_FILTER,
                Utils.Setting.DEFAULT_FILTER));

        mListDefaultSorting = (ListPreference) findPreference(Utils.Setting.KEY_DEFAULT_SORTING);
        mListDefaultSorting.setOnPreferenceChangeListener(mChangeListener);
        mListDefaultSorting.setSummary(mPreferences.getString(Utils.Setting.KEY_DEFAULT_SORTING,
                Utils.Setting.DEFAULT_SORTING));

        mListDefaultItemClick = (ListPreference) findPreference(Utils.Setting.KEY_DEFAULT_ITEM_CLICK);
        mListDefaultItemClick.setOnPreferenceChangeListener(mChangeListener);
        mListDefaultItemClick.setSummary(mPreferences.getString(Utils.Setting.KEY_DEFAULT_ITEM_CLICK,
                Utils.Setting.DEFAULT_ITEM_CLICK));

        mListFormatExportName = (ListPreference) findPreference(Utils.Setting.KEY_FORMAT_EXPORT_NAME);
        mListFormatExportName.setOnPreferenceChangeListener(mChangeListener);
        mListFormatExportName.setSummary(mPreferences.getString(Utils.Setting.KEY_FORMAT_EXPORT_NAME,
                Utils.Setting.DEFAULT_FORMAT_EXPORT_NAME));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.Constants.REQ_BACKUP_DERICTORY) {
            if (resultCode == RESULT_OK && data != null) {
                String backupDirectory = data.getStringExtra("backupDirectory");
                mEditBackupDir.setSummary(backupDirectory);
                mEditBackupDir.getEditText().setText(backupDirectory);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
