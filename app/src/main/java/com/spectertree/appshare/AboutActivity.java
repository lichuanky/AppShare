package com.spectertree.appshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class AboutActivity extends Activity {

    private static final int DIALOG_ABOUT = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialog(DIALOG_ABOUT);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch(id) {
            case DIALOG_ABOUT:
                dialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.dialog_about_title)
                .setMessage(R.string.dialog_about_content)
                .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
                return dialog;
        }
        return super.onCreateDialog(id);
    }
}
