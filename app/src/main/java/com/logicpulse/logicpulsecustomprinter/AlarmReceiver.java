package com.logicpulse.logicpulsecustomprinter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.logicpulse.logicpulsecustomprinter.App.Singleton;

/**
 * Created by mario.monteiro on 13/10/2016.
 */

// WakefulBroadcastReceiver
// android.support.v4.content.WakefulBroadcastReceiver is a helper class that receives a device wakeful event.
// We override onReceive() method where we can call our service or perform our task.
// In our case we will play alarm.
// WakefulBroadcastReceiver uses wake lock, so we must provide WAKE_LOCK permission in AndroidManifest.xml.
// WakefulBroadcastReceiver is implemented as

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static Singleton mApp = Singleton.getInstance();

    @Override
    public void onReceive(final Context context, Intent intent) {

        String mode = intent.getStringExtra("mode"); //if it's a string you stored.

        if (mode.toUpperCase().equals("screenOn".toUpperCase())) {
            Log.d(mApp.getTAG(), "AlarmReceiver.onReceive: screenOn");

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "wake");
            wakeLock .acquire();
        }
        else if (mode.toUpperCase().equals("screenOff".toUpperCase())) {
            Log.d(mApp.getTAG(), "AlarmReceiver.onReceive: screenOff");

            mApp.getDevicePolicyManager().lockNow();
        }

        //Log.d(MainActivity.TAG, "AlarmReceiver.onReceive: Start Activity");
        //Intent myIntent = new Intent(context, MainActivity.class);
        //context.startActivity(myIntent);

        //init Ringtone
        //Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        //Ringtone ringtone = RingtoneManager.getRingtone(context, defaultUri);
        //Utils.alarmStartPlay(context, ringtone);

        //Play Sound
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.notification);
        mediaPlayer.start();

        //PowerManager.WakeLock screenOn = ((PowerManager)context
        //        .getSystemService(context.POWER_SERVICE))
        //        .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
        //                PowerManager.ON_AFTER_RELEASE |
        //                PowerManager.ACQUIRE_CAUSES_WAKEUP, "example");
        //screenOn.acquire();
        //screenOn.release();

        // This is the Intent to deliver to our service.
        //Intent service = new Intent(context, SimpleWakefulService.class);

        // Start the service, keeping the device awake while it is launching.
        //Log.i("SimpleWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
        //startWakefulService(context, service);
    }
}
