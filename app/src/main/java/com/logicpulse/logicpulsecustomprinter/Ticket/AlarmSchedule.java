package com.logicpulse.logicpulsecustomprinter.Ticket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import java.util.Calendar;

/**
 * Created by mario.monteiro on 17/10/2016.
 * Repeating Alarm for specific days of week android
 * http://stackoverflow.com/questions/36550991/repeating-alarm-for-specific-days-of-week-android
 */

public class AlarmSchedule {

    private Context mContext;

    public AlarmSchedule(Context context) {
        this.mContext = context;
    }

    private void setUpAlarms() {
        //scheduleAlarm(Calendar.MONDAY);
        //scheduleAlarm(Calendar.FRIDAY);
    }

    private void scheduleAlarm(int dayOfWeek, Calendar calendar, PendingIntent pendingIntent) {

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

        // Check we aren't setting it in the past which would trigger it to fire instantly
        if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }
}
