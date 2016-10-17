package com.logicpulse.logicpulsecustomprinter.Alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.logicpulse.logicpulsecustomprinter.AlarmReceiver;
import com.logicpulse.logicpulsecustomprinter.App.Singleton;
import com.logicpulse.logicpulsecustomprinter.R;
import com.logicpulse.logicpulsecustomprinter.Utils;
import com.solidfire.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mario.monteiro on 17/10/2016.
 * Repeating Alarm for specific days of week android
 * http://stackoverflow.com/questions/36550991/repeating-alarm-for-specific-days-of-week-android
 * <p>
 * Get list of active PendingIntents in AlarmManager
 * http://stackoverflow.com/questions/6522792/get-list-of-active-pendingintents-in-alarmmanager
 * http://stackoverflow.com/questions/28742884/how-to-read-adb-shell-dumpsys-alarm-output/31600886#31600886
 * Testing that Android AlarmManager has an alarm set.
 * http://qathread.blogspot.pt/2014/04/testing-if-android-alarmmanager-has.html
 * adb shell dumpsys alarm > dump.txt
 * <p>
 * Start/Stop Alarms
 * How to check if AlarmManager already has an alarm set?
 * http://stackoverflow.com/questions/4556670/how-to-check-if-alarmmanager-already-has-an-alarm-set
 */

public class AlarmSchedule {

    private static Singleton mApp = Singleton.getInstance();
    private Context mContext;
    private Context mContextApplication;

    private AlarmScheduleTemplate mAlarmScheduleTemplate = null;

    AlarmManager mAlarmManager;
    ArrayList<PendingIntent> arrayPendingintents = new ArrayList<PendingIntent>();

    public AlarmSchedule(Context context) {
        mContext = context;
        mContextApplication = context.getApplicationContext();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        //Init AlarmScheduleConfig
        InputStream inputStream = Utils.getInputStreamFromRawResource(context, R.raw.alarm_schedule);

        String templateString = null;

        try {
            templateString = Utils.getStringFromInputStream(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            try {
                Gson gson = new Gson();
                mAlarmScheduleTemplate = gson.fromJson(templateString, AlarmScheduleTemplate.class);
            } catch (Throwable tx) {
                //Show Error
                String errorMessage = String.format("Error: Malformed JSON: %s\r\n\r\n%s", tx.getMessage(), templateString);
                Utils.showAlert((Activity) context, errorMessage);
                Log.e(mApp.getTAG(), errorMessage);
            }
        } catch (Exception e) {
            //Show Error
            e.printStackTrace();
            String errorMessage = String.format("Error: Malformed JSON: %s", templateString);
            Utils.showAlert((Activity) context, errorMessage);
            Log.e(mApp.getTAG(), errorMessage);
        }

    }

    public void setUpAlarms() {

        AlarmScheduleTemplateNode node;

        Integer requestCode = 0;
        for (Object property : mAlarmScheduleTemplate.getProperties()) {

            requestCode++;
            node = (AlarmScheduleTemplateNode) property;

            Integer dayOfWeek = getDayOfWeek(node.getDay());
            Calendar calendar = getCalendarHour(node.getHour());
            PendingIntent pendingIntent = getPendingIntent(node.getAction(), requestCode);

            Log.d(mApp.getTAG(), String.format("setUpAlarms: %s - %S - %S", dayOfWeek, calendar.getTime().toString(), pendingIntent.toString()));
            scheduleAlarm(dayOfWeek, calendar, pendingIntent);
        }

        //Calendar calendarOff = new GregorianCalendar();
        //calendarOff.setTimeInMillis(System.currentTimeMillis());
        //calendarOff.set(Calendar.HOUR_OF_DAY, 14);
        //calendarOff.set(Calendar.MINUTE, 38);
        //calendarOff.set(Calendar.SECOND, 00);
        //
        //Calendar calendarOn = new GregorianCalendar();
        //calendarOn.setTimeInMillis(System.currentTimeMillis());
        //calendarOn.set(Calendar.HOUR_OF_DAY, 14);
        //calendarOn.set(Calendar.MINUTE, 37);
        //calendarOn.set(Calendar.SECOND, 00);
        //
        //scheduleAlarm(Calendar.MONDAY, calendarOff, mPendingIntentOff);
        //scheduleAlarm(Calendar.MONDAY, calendarOn, mPendingIntentOn);
    }

    private void scheduleAlarm(int dayOfWeek, Calendar calendar, PendingIntent pendingIntent) {

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

        // Check we aren't setting it in the past which would trigger it to fire instantly
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
        //Add to arrayPendingintents, else we override it, loosing references
        arrayPendingintents.add(pendingIntent);
    }

    private Integer getDayOfWeek(String day) {

        Integer result = -1;

        switch (day.toUpperCase()) {
            case "SUNDAY":
                result = 1;
                break;
            case "MONDAY":
                result = 2;
                break;
            case "TUESDAY":
                result = 3;
                break;
            case "WEDNESDAY":
                result = 4;
                break;
            case "THURSDAY":
                result = 5;
                break;
            case "FRIDAY":
                result = 6;
                break;
            case "SATURDAY":
                result = 7;
                break;
            default:
                break;
        }

        return result;
    }

    private Calendar getCalendarHour(String hour) {

        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(System.currentTimeMillis());

        DateFormat formatter = new SimpleDateFormat("hh:mm");
        try {
            Date date = formatter.parse(hour);
            result.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    //Android Set Multiple Alarms
    //http://stackoverflow.com/questions/12785702/android-set-multiple-alarms
    //If you want to set multiple alarms (repeating or single), then you just need to create their PendingIntents with different requestCode. If requestCode is the same, then the new alarm will overwrite the old one.
    private PendingIntent getPendingIntent(String action, Integer requestCode) {

        //Pending Intents On
        Intent intentAlarmManagerOn = new Intent(mContextApplication, AlarmReceiver.class);
        intentAlarmManagerOn.putExtra("mode", "screenOn");
        PendingIntent pendingIntentOn = PendingIntent.getBroadcast(mContextApplication, requestCode, intentAlarmManagerOn, 0);

        //Pending Intents Off
        Intent intentAlarmManagerOff = new Intent(mContextApplication, AlarmReceiver.class);
        intentAlarmManagerOff.putExtra("mode", "screenOff");
        PendingIntent pendingIntentOff = PendingIntent.getBroadcast(mContextApplication, 0, intentAlarmManagerOff, 0);

        PendingIntent result = null;

        switch (action.toUpperCase()) {
            case "SCREEN_ON":
                result = pendingIntentOn;
                break;
            case "SCREEN_OFF":
                result = pendingIntentOff;
                break;
        }

        return result;
    }
}
