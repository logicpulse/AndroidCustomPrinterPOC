package com.logicpulse.logicpulsecustomprinter;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by mario.monteiro on 07/10/2016.
 * PowerManager
 * https://developer.android.com/reference/android/os/PowerManager.html
 * https://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
 *
 *
 *
 * This is what i think you are looking for. Don't forget to include following permission.
 * android.permission.DEVICE_POWER
 * http://developer.android.com/reference/android/os/PowerManager.html#goToSleep%28long%29
 */

public class PowerMan {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private PowerManager mPowerManager;
    private boolean mScreenOn = true;
    private PowerManager.WakeLock mWakeLock;

    // Constructor:
    private PowerMan(Activity activity) {
        mPowerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
    }

    public boolean getScreenOn() {
        if(mPowerManager == null) {
            return mScreenOn;
        }

        mScreenOn = mPowerManager.isInteractive();

        return mScreenOn;
    }

    public void setScreenOn(final boolean screenOn) {
        if(mPowerManager == null) {
            return;
        }

        if(screenOn) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        }
    }
}