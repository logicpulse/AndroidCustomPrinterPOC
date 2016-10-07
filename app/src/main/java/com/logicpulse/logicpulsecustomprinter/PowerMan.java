package com.logicpulse.logicpulsecustomprinter;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by mario.monteiro on 07/10/2016.
 */

public class PowerMan {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private PowerManager mPowerManager;
    private boolean mScreenOn = true;

    // Constructor:
    private PowerMan(Activity activity) {
        Log.d(TAG, "==TestScreenOff");

        initialize(activity);
    }

    // Initialize activity-dependent fields:
    public void initialize(Activity activity) {
        Log.d(TAG, "==initialize");

        mActivity = activity;
        mPowerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
    }

    // Gets screenOn:
    public boolean getScreenOn() {
        if(mPowerManager == null) {
            Log.e(TAG, "==getScreenOn: mPowerManager == null");
            return mScreenOn;
        }

        mScreenOn = mPowerManager.isScreenOn();

        Log.d(TAG, "==getScreenOn[" + mScreenOn + "]");

        return mScreenOn;
    }

    // Sets screenOn:
    public void setScreenOn(final boolean screenOn) {
        Log.d(TAG, "==setScreenOn[" + screenOn + "]");

        if(mPowerManager == null) {
            Log.e(TAG, "==setScreenOn: mPowerManager == null");
            return;
        }

        if(screenOn) {
            mPowerManager.wakeUp(SystemClock.uptimeMillis());
        } else {
            mPowerManager.goToSleep(SystemClock.uptimeMillis());
        }
    }
}