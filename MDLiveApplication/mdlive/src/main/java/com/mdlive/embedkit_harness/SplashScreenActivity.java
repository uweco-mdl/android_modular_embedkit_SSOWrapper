package com.mdlive.embedkit_harness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {
    private Handler mHandler;
    private Runnable mRunnable;
    private static final int SPLASH_TIME_OUT = 3000;

    /**
     *
     * The config data is shared to common module by calling MDLiveConfig.setData() method.
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mdlive_splashscreen);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Intent embedKitIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(embedKitIntent);
                finish();
            }
        };
        mHandler.postDelayed(mRunnable, SPLASH_TIME_OUT);
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.postDelayed(mRunnable, SPLASH_TIME_OUT);
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler = null;
        mRunnable = null;
    }

}


