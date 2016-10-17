package com.logicpulse.logicpulsecustomprinter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.logicpulse.logicpulsecustomprinter.App.Singleton;
import com.logicpulse.logicpulsecustomprinter.Printers.CustomPrinterDevice;
import com.logicpulse.logicpulsecustomprinter.Printers.IThermalPrinter;
import com.logicpulse.logicpulsecustomprinter.Alarm.AlarmSchedule;
import com.logicpulse.logicpulsecustomprinter.Ticket.Ticket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import it.custom.printer.api.android.CustomException;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.os.Debug.isDebuggerConnected;

public class MainActivity extends AppCompatActivity {

    private static Singleton mApp = Singleton.getInstance();

    //public static final String TAG = "CustomPrinterPOC";
    public static final String PRINT_TEXT = "Texting CustomPrinterPOC...";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    public String mPackageName;
    //private CustomPrinterDevice mCustomPrinterDevice;
    private IThermalPrinter mCustomPrinterDevice;
    //private Boolean mUseCustomPrinter = true;
    private Ringtone mRingtone;
    private Ticket mTicket;
    private View mViewActivityMain;
    //PowerManager
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    //Alarm
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntentAlarmManagerOn;
    private PendingIntent mPendingIntentAlarmManagerOff;
    //Usb
    private PendingIntent mPendingIntentUsbPermission;
    private UsbManager mUsbManager;
    private HashMap<String, UsbDevice> mDeviceList;
    private UsbDevice mUsbDevice;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Singleton
        mApp.setMainActivity(this);
        mApp.setTAG(getResources().getString(R.string.app_tag));

        //Get View
        mViewActivityMain = findViewById(R.id.content_main);

        //Get Package Name
        mPackageName = getApplicationContext().getPackageName();

        //init Network ADB only if not debugger attached, else we close adb, and debug
        if (!isDebuggerConnected()) {
            Utils.enableNetworkADB(true);
        }

        //init Permissions
        final Context finalContext = this;
        runOnUiThread(new Runnable() {
            public void run() {
                Utils.copyFileHardwareUsbHostToSystemPermission(finalContext);
            }
        });

        //KeepScreenOn WakeLock
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Force Screen On
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get System Services (USB)
        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        //PowerManager (PowerManager)
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        //Alarm
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //Pending Intents On
        Intent intentAlarmManagerOn = new Intent(MainActivity.this, AlarmReceiver.class);
        intentAlarmManagerOn.putExtra("mode", "screenOn");
        mPendingIntentAlarmManagerOn = PendingIntent.getBroadcast(MainActivity.this, 0, intentAlarmManagerOn, 0);
        //Pending Intents Off
        Intent intentAlarmManagerOff = new Intent(MainActivity.this, AlarmReceiver.class);
        intentAlarmManagerOff.putExtra("mode", "screenOff");
        mPendingIntentAlarmManagerOff = PendingIntent.getBroadcast(MainActivity.this, 0, intentAlarmManagerOn, 0);

        //Admin Mode
        mApp.setDevicePolicyManager((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE));
        mApp.setDeviceAdmin(new ComponentName(this, DeviceAdmin.class));
        mApp.setDeviceAdminActive(isActiveAdmin());

        //Register BroadcastReceivers()
        registerBroadcastReceivers();

        //init Ringtone
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(this, defaultUri);

        //If the Activity has not been created yet, it will be created and the intent arrive through the onCreate() method:
        if (ACTION_USB_ATTACHED.equalsIgnoreCase(getIntent().getAction())) {
            Log.d(mApp.getTAG(), "ACTION_USB_ATTACHED");
            //init Usb Devices
            initUsbDevices();
        } else {
            initUsbDevices();
        }
    }

    /*
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //If the activity has already been created/instantiated, the event will arrive through the 'onNewIntent()' method:
        if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            Log.d(mApp.getTAG(), "ACTION_USB_ATTACHED");
            mUsbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            requestPermission(mUsbDevice);
        } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            Log.d(mApp.getTAG(), "ACTION_USB_DETACHED");
        }
    }
    */

    @Override
    protected void onResume() {
        super.onResume();

        enableDeviceCapabilitiesArea(mApp.getDeviceAdminActive());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mUsbReceiver);
        unregisterReceiver(mPowerManagerReceiver);

        try {
            if (mCustomPrinterDevice != null) {
                mCustomPrinterDevice.close();
            }
            Utils.alarmStopPlay(mRingtone);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_get_device_list) {
            initUsbDevices();
        }
        //if (id == R.id.action_open_device) {
        //    actionOpenDevice();
        //    return true;
        //}
        if (id == R.id.action_test_device_print_text) {
            actionTestDevicePrintText();
            return true;
        }
        if (id == R.id.action_test_device_print_image) {
            actionTestDevicePrintImage();
            return true;
        }
        if (id == R.id.action_test_alarm_start_play) {
            actionTestAlarmStart();
            return true;
        }
        if (id == R.id.action_test_alarm_stop_play) {
            actionTestAlarmStop();
            return true;
        }
        if (id == R.id.action_test_ticket) {
            actionTestTicket();
            return true;
        }
        if (id == R.id.action_toggle_admin_mode) {
            actionToggleAdmin();
            return true;
        }
        if (id == R.id.action_test_screen_off) {
            actionTestScreenOff();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver

    private void registerBroadcastReceivers() {
        try {

            //Setup PendingIntent
            mPendingIntentUsbPermission = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(ACTION_USB_ATTACHED);
            filter.addAction(ACTION_USB_DETACHED);
            //Register BroadcastReceiver
            registerReceiver(mUsbReceiver, filter);

            //PowerManager
            filter = new IntentFilter(ACTION_SCREEN_ON);
            filter.addAction(ACTION_SCREEN_OFF);
            //Register BroadcastReceiver
            registerReceiver(mPowerManagerReceiver, filter);
        } catch (Exception ex) {
            Log.e(mApp.getTAG(), ex.getMessage(), ex);
        }
    }

    /**
     * When users reply to the dialog, your broadcast receiver receives the intent that contains the
     * EXTRA_PERMISSION_GRANTED extra, which is a boolean representing the answer. Check this extra for a value
     * of true before connecting to the accessory.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.e(mApp.getTAG(), "permission denied for accessory " + accessory);
                    }
                }
                //} else if (mUsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                //    synchronized (this) {
                //        Log.e(MainActivity.TAG, "ACTION_USB_DEVICE_ATTACHED");
                //        mUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                //        //init CustomPrinterDevice
                //        //mCustomPrinterDevice = new CustomPrinterDevice();
                //        mCustomPrinterDevice.init(context, mUsbDevice, mViewActivityMain, mRingtone);
                //    }
            } else if (mUsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    Log.e(mApp.getTAG(), "ACTION_USB_DEVICE_DETACHED");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                //Close Application
                                if (mCustomPrinterDevice != null) {
                                    //mCustomPrinterDevice.close();
                                    mCustomPrinterDevice.close();
                                }
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    };

    private final BroadcastReceiver mPowerManagerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_SCREEN_ON.equals(action)) {
                synchronized (this) {
                    Log.e(mApp.getTAG(), "ACTION_SCREEN_ON");
                }
            } else if (ACTION_SCREEN_OFF.equals(action)) {
                synchronized (this) {
                    Log.e(mApp.getTAG(), "ACTION_SCREEN_OFF");
                }
            }
        }
    };

    //--------------------------------------------------------------------------------------------------------------
    // Actions

    //private void actionOpenDevice() {
    //    //Open it
    //    mCustomPrinterDevice.openDevice();
    //}

    private void actionTestDevicePrintText() {
        mCustomPrinterDevice.testPrintText(PRINT_TEXT);
    }

    private void actionTestDevicePrintImage() {
        InputStream inputStream = Utils.getInputStreamFromRawResource(this, R.raw.image);
        mCustomPrinterDevice.testPrintImage(inputStream);
    }

    private void actionTestAlarmStart() {
        Utils.alarmStartPlay(this, mRingtone);
    }

    private void actionTestAlarmStop() {
        Utils.alarmStopPlay(mRingtone);
    }

    private void actionTestTicket() {

        final Context context = this;

        if (mCustomPrinterDevice != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mTicket == null) {
                                //init Ticket
                                mTicket = new Ticket(context, mCustomPrinterDevice);
                            }
                            try {
                                //Print
                                mTicket.print(1);
                            } catch (CustomException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 100);
                }
            });
        }
    }

    private boolean actionToggleAdmin() {

        if (!mApp.getDeviceAdminActive()) {
            // Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mApp.getDeviceAdminActive());
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, this.getString(R.string.add_admin_extra_app_text));
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            // return false - don't update checkbox until we're really active
            return false;

        } else {
            mApp.getDevicePolicyManager().removeActiveAdmin(mApp.getDeviceAdmin());
            enableDeviceCapabilitiesArea(false);
            mApp.setDeviceAdminActive(false);
        }

        return false;
    }

    private void actionTestScreenOff() {

        //Screen Off
        //final Activity finalContext = (Activity) this;

        //Utils.powerManagerScreenOff(this, mPackageName);

        //Turning screen on and off programmatically not working on some devices
        //http://stackoverflow.com/questions/13416563/turning-screen-on-and-off-programmatically-not-working-on-some-devices

        //http://stackoverflow.com/questions/6560426/android-devicepolicymanager-locknow
        //ComponentName devAdminReceiver; // this would have been declared in your class body
        // then in your onCreate
        //mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        //devAdminReceiver = new ComponentName(context, deviceAdminReceiver.class);
        //then in your onResume

        //mApp.getDevicePolicyManager().lockNow();

        //mAlarmManager.setInexactRepeating(
        //        AlarmManager.RTC_WAKEUP,
        //        10 * 1000,
        //        10 * 1000,
        //        mPendingIntentAlarmManagerOn);

        //Repeat alarm everyday accurately (Alarm manager)
        //http://stackoverflow.com/questions/28001154/repeat-alarm-everyday-accurately-alarm-manager

        AlarmSchedule alarmSchedule = new AlarmSchedule(this);
        alarmSchedule.setUpAlarms();


//Calendar calendar = Calendar.getInstance();
        //Calendar calendarOff = new GregorianCalendar();
        //calendarOff.set(Calendar.HOUR_OF_DAY, 12);
        //calendarOff.set(Calendar.MINUTE, 44);
        //calendarOff.set(Calendar.SECOND, 00);
//calendarOff.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

//mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mPendingIntentAlarmManagerOn);
//mAlarmManager.setWindow(AlarmManager.RTC, calendarOff.getTimeInMillis(), 5000, mPendingIntentAlarmManagerOff);
//mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarOff.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPendingIntentAlarmManagerOff);

        //Calendar calendarOn = new GregorianCalendar();
        //calendarOn.set(Calendar.HOUR_OF_DAY, 12);
        //calendarOn.set(Calendar.MINUTE, 43);
        //calendarOn.set(Calendar.SECOND, 00);
//mAlarmManager.setWindow(AlarmManager.RTC_WAKEUP, calendarOn.getTimeInMillis(), 5000, mPendingIntentAlarmManagerOn);
//mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarOn.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPendingIntentAlarmManagerOn);

        //Ends App
        //finish();

        //runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {
        //        final Handler handler = new Handler();
        //        handler.postDelayed(new Runnable() {
        //            @Override
        //            public void run() {
        //                //Utils.powerManagerScreenOn(finalContext, mPackageName);
        //                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //
        //            }
        //        }, 1000);
        //    }
        //});

        //http://www.concretepage.com/android/android-alarm-clock-tutorial-to-schedule-and-cancel-alarmmanager-pendingintent-and-wakefulbroadcastreceiver-example
        //http://www.concretepage.com/android/download/android-alarm-clock-tutorial-to-schedule-and-cancel-alarmmanager-pendingintent-and-wakefulbroadcastreceiver-example.zip

        // Set the alarm to start at approximately 2:00 p.m.
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 14);

        //Intent intent = new Intent(this, AlarmReceiver.class);
        //PendingIntent  pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        //mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        //mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);


        //mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10 * 1000, pendingIntent);

        //setAlarm();
    }

    //--------------------------------------------------------------------------------------------------------------
    // Actions

    private void requestPermission(UsbDevice pDevice) {
        mUsbManager.requestPermission(pDevice, mPendingIntentUsbPermission);
    }

    private void initUsbDevices() {
        String deviceMessage;
        mDeviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = mDeviceList.values().iterator();
        int deviceNo = 0;

        while (deviceIterator.hasNext()) {
            //Get Device to Work On
            //UsbDevice device = deviceList.get("deviceName");
            deviceNo++;
            mUsbDevice = deviceIterator.next();
            //your code
            deviceMessage = String.format(
                    "DeviceName[%d/%d]: %s, ProductId: %s, VendorId: %s",
                    deviceNo,
                    mDeviceList.size(),
                    mUsbDevice.getDeviceName(),
                    mUsbDevice.getProductId(),
                    mUsbDevice.getVendorId()
            );
            Log.d(mApp.getTAG(), deviceMessage);

            //Show Interfaces
            for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {

                mUsbInterface = mUsbDevice.getInterface(i);
                Log.d(mApp.getTAG(), String.format("Interface DescribeContents: %d", mUsbInterface.describeContents()));

                //Request Permission: Show the Request Permission USB Dialog :)
                requestPermission(mUsbDevice);

                //Show Endpoints
                for (int j = 0; j < mUsbInterface.getEndpointCount(); j++) {
                    mUsbEndpoint = mUsbInterface.getEndpoint(j);
                    Log.d(mApp.getTAG(), String.format("EndPoint DescribeContents: %d", mUsbEndpoint.describeContents()));

                    //Test EndPoint : DETECTED Custom TG2460H
                    if (mUsbDevice.getVendorId() == 3540 /*&& mUsbDevice.getProductId() == 423*/) {

                        //testEndPoint(mUsbDevice, mUsbInterface, mUsbEndpoint);
                        Log.d(mApp.getTAG(), String.format("ProductId: %d / VendorId: %d", mUsbDevice.getProductId(), mUsbDevice.getVendorId()));

                        //init CustomPrinterDevice
                        mCustomPrinterDevice = new CustomPrinterDevice();
                        mCustomPrinterDevice.init(this, mUsbDevice, mViewActivityMain, mRingtone);
                    }
                    //OtherPrinters
                    else {
                    }
                }
            }
        }
        if (mDeviceList.size() <= 0) {
            Log.d(mApp.getTAG(), "No Devices Found");
        }
    }

    private void testEndPoint(UsbDevice pUsbDevice, UsbInterface pUsbInterface, UsbEndpoint pUsbEndpoint) {

        final byte[] bytesData2 = new byte[24];
        //esc @ init
        bytesData2[0] = 0x1B;
        bytesData2[1] = 0x40;
        //Hello
        bytesData2[2] = "H".getBytes()[0];
        bytesData2[3] = "e".getBytes()[0];
        bytesData2[4] = "l".getBytes()[0];
        bytesData2[5] = "l".getBytes()[0];
        bytesData2[6] = "o".getBytes()[0];

        //LineFeed
        bytesData2[7] = 0x0A;
        bytesData2[8] = 0x0A;
        bytesData2[9] = 0x0A;
        bytesData2[10] = 0x0A;
        bytesData2[11] = 0x0A;
        //Cut the paper
        //bytesData2[12] = 0x1D;
        //bytesData2[13] = 0x56;
        //bytesData2[14] = 0x42;
        //bytesData2[15] = 0x03;
        //Custom TG2460H
        //bytesData2[12] = 0x1C;
        //bytesData2[13] = (byte) 0xC0;
        //bytesData2[14] = 0x34;

        //TODO: Move to Settings
        final int TIMEOUT = 0;
        boolean forceClaim = true;

        //Test PT String
        final ByteArrayOutputStream textToPrint = new ByteArrayOutputStream();

        try {
            textToPrint.write(0x1B);
            textToPrint.write("@".getBytes()[0]);
            String str = "Á, À, Â, Ã, ã, Ê, Í, Ó, Ô, Õ, õ, Ú - á, à, â, ã, ã, ê, í, ó, ô, õ, õ, ú, ▒, ┤, █";
            textToPrint.write(str.getBytes("CP860"));
            textToPrint.write("€, $".getBytes("CP858"));

            str = "0xD5";
            textToPrint.write(str.getBytes("CP860"));

            // LineFeed
            textToPrint.write(0x0A);
            textToPrint.write(0x0A);
            textToPrint.write(0x0A);
            // Cut the paper 1
            //textToPrint.write(0x1D);
            //textToPrint.write(0x56);
            //textToPrint.write(0x42);
            //textToPrint.write(0x03);
            // Custom TG2460H
            textToPrint.write(0x1C);
            textToPrint.write(0xC0);
            textToPrint.write(0x34);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //Variable is accessed within inner class. Needs to be declared final
            //http://stackoverflow.com/questions/14425826/variable-is-accessed-within-inner-class-needs-to-be-declared-final
            final UsbEndpoint usbEndpoint = pUsbEndpoint;
            final UsbDeviceConnection connection = mUsbManager.openDevice(pUsbDevice);
            connection.claimInterface(pUsbInterface, forceClaim);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //do in another thread
                    //connection.bulkTransfer(usbEndpoint, bytesData1, bytesData1.length, TIMEOUT);
                    //connection.bulkTransfer(usbEndpoint, bytesData2, bytesData2.length, TIMEOUT);
                    connection.bulkTransfer(usbEndpoint, textToPrint.toByteArray(), textToPrint.toByteArray().length, TIMEOUT);
                }
            }, 100);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------------------------------------------
    // DeviceAdmin

    /**
     * Helper to determine if we are an active admin
     */
    private boolean isActiveAdmin() {
        return mApp.getDevicePolicyManager().isAdminActive(mApp.getDeviceAdmin());
    }

    public void setAdminActive(Boolean value) {
        mApp.setDeviceAdminActive(value);
    }

    /**
     * Updates the device capabilities area (dis/enabling) as the admin is (de)activated
     */
    private void enableDeviceCapabilitiesArea(boolean enabled) {
        //mDisableCameraCheckbox.setEnabled(enabled);
        //mDisableKeyguardWidgetsCheckbox.setEnabled(enabled);
        //mDisableKeyguardSecureCameraCheckbox.setEnabled(enabled);
        //mDisableKeyguardNotificationCheckbox.setEnabled(enabled);
        //mDisableKeyguardUnredactedCheckbox.setEnabled(enabled);
        //mDisableKeyguardTrustAgentCheckbox.setEnabled(enabled);
        //mTrustAgentComponent.setEnabled(enabled);
        //mTrustAgentFeatures.setEnabled(enabled);
    }

    //--------------------------------------------------------------------------------------------------------------
    // Alarm

    //Wake Android Device up
    //GOOD POST: http://stackoverflow.com/questions/3621599/wake-android-device-up

    private void setAlarm() {
        //Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.HOUR_OF_DAY, timeHour);
        //calendar.set(Calendar.MINUTE, timeMinute);
        //mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mPendingIntentAlarmManagerOn);
        //mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5 * 1000, mPendingIntentAlarmManagerOn);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                5 * 1000,
                5 * 1000,
                mPendingIntentAlarmManagerOn);
    }

    private void cancelAlarm() {
        if (mAlarmManager != null) {
            mAlarmManager.cancel(mPendingIntentAlarmManagerOn);
        }
    }
}
