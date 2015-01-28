package com.spectertree.appshare;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.spectertree.appshare.base.BaseActivity;

/**
 * Created by lichuan on 15/1/27.
 */
public class SplashActivity extends BaseActivity {
    private static final String TAG = "WebGameActivity";

    private final int SPLASH_DISPLAY_LENGTH = 3 * 1000;

    private static final int MENU_RELOAD = Menu.FIRST;
    private static final int MENU_HELP = Menu.FIRST + 1;
    private static final int MENU_ABOUT = Menu.FIRST + 2;
    private static final int MENU_CLOSE = Menu.FIRST + 3;
    private int staus = 0;

    private static final int STOPSPLASH = 0;
    private static final long SPLASHTIME = 1000;

    private View mSplashView;
    private TextView tv;

    private Animation mAnimation_Alpha;
    private Animation mAnimationGone;

    private Handler splashHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOPSPLASH:
                    if( staus == 1 ){
                        mSplashView.startAnimation(mAnimationGone);
                        mSplashView.setVisibility(View.GONE);
                        break;
                    }
                    sendEmptyMessageDelayed(STOPSPLASH, SPLASHTIME);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main);
        mAnimationGone = AnimationUtils.loadAnimation(this,R.anim.alpha_gone);
        mAnimation_Alpha = AnimationUtils.loadAnimation(this, R.anim.alpha_action);

        mSplashView = findViewById(R.id.splash_ll);
        tv = (TextView) findViewById(R.id.info_tv);
        tv.setText("正在建立数据连接");
        mSplashView.startAnimation(mAnimation_Alpha);

        Message msg = new Message();
        msg.what = STOPSPLASH;
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);
    }
}
