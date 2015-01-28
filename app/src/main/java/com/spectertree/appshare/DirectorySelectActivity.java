package com.spectertree.appshare;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DirectorySelectActivity extends ListActivity {

    private String mCurrentDirectory;
    private ArrayAdapter<String> mAdapter;
    private List<String> mDirectoryList;

    private ListView mListView;
    private Button mBtnOk;
    private Button mBtnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_directory_select);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.putExtra("backupDirectory", mCurrentDirectory);
                setResult(Activity.RESULT_OK, in);
                finish();
            }
        });
        mBtnCancel = (Button) findViewById(R.id.btn_cancle);
        mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        mListView = (ListView) findViewById(android.R.id.list);

        mCurrentDirectory = Environment.getExternalStorageDirectory().getPath();
        mDirectoryList = new ArrayList<String>();
        updateDirectoryList(mDirectoryList, mCurrentDirectory);
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDirectoryList);

        mListView.setAdapter(mAdapter);
        setTitle(mCurrentDirectory);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mCurrentDirectory = mCurrentDirectory + "/" + mDirectoryList.get(position);
        File[] files = (new File(mCurrentDirectory)).listFiles();
        if (files.length > 0)
        updateDirectoryList(mDirectoryList, mCurrentDirectory);
        mAdapter.notifyDataSetChanged();
        setTitle(mCurrentDirectory);
    }

    private void updateDirectoryList(List<String> directoryList, String path) {
        directoryList.clear();

        File[] files = (new File(path)).listFiles();

        for (File f : files) {
            if (f.isDirectory())
                directoryList.add(f.getName());
        }

        Collections.sort(directoryList, new Comparator<String>() {
            @Override
            public int compare(String l, String r) {
                Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);
                return myCollator.compare(l, r);
            }
        });
    }
}
