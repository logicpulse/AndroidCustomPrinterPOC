package com.logicpulse.logicpulsecustomprinter;

import android.app.Activity;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

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

    public static final String TAG = "CustomPrinterPOC";
    public static final String PRINT_TEXT = "Texting CustomPrinterPOC...";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    public String mPackageName;
    private CustomPrinterInterface mCustomPrinterInterface;
    private Boolean mUseCustomPrinter = true;
    private Ringtone mRingtone;
    private Ticket mTicket;
    private View mViewActivityMain;
    //DeviceAdmin
    private ComponentName mDevAdminReceiver;
    private DevicePolicyManager mDevicePolicyManager;
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

        mViewActivityMain = findViewById(R.id.content_main);

        //Get Package Name
        mPackageName = getApplicationContext().getPackageName();

        //Init Network ADB only if not debugger attached, else we close adb, and debug
        if (!isDebuggerConnected()) {
            Utils.enableNetworkADB(true);
        }

        //Init Permissions
        final Context finalContext = this;
        runOnUiThread(new Runnable() {
            public void run() {
                Utils.copyFileHardwareUsbHostToSystemPermission(finalContext);
            }
        });

        //KeepScreenOn WakeLock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get System Services
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        //Register BroadcastReceiver()
        registerBroadcastReceiver();

        ////Init CustomPrinterInterface
        //mCustomPrinterInterface = new CustomPrinterInterface(
        //        //Fix for listViewDevices textColor
        //        //never use getApplicationContext(). Just use your Activity as the Context
        //        this /*this.getApplicationContext()*/,
        //        viewActivityMain,
        //        savedInstanceState
        //);

        //Init Ringtone
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(this, defaultUri);

        //If the Activity has not been created yet, it will be created and the intent arrive through the onCreate() method:
        //if (ACTION_USB_ATTACHED.equalsIgnoreCase(getIntent().getAction())) {
        //    Log.d(TAG, "ACTION_USB_ATTACHED");
        //        //Init Usb Devices
        //    initUsbDevices(true);
        //}

        initUsbDevices(mUseCustomPrinter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //If the activity has already been created/instantiated, the event will arrive through the 'onNewIntent()' method:
        //if (ACTION_USB_ATTACHED.equalsIgnoreCase(getIntent().getAction())) {
        //    Log.d(TAG, "ACTION_USB_ATTACHED");
        //} else if (ACTION_USB_DETACHED.equalsIgnoreCase(getIntent().getAction())) {
        //    Log.d(TAG, "ACTION_USB_DETACHED");
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        try {
//            if (mCustomPrinterInterface != null) {
//                mCustomPrinterInterface.onExit();
//            }
//            Utils.alarmStopPlay(mRingtone);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
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
            initUsbDevices(mUseCustomPrinter);
        }
        if (id == R.id.action_open_device) {
            actionOpenDevice();
            return true;
        }
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
        if (id == R.id.action_test_screen_off) {
            actionTestScreenOff();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver

    private void registerBroadcastReceiver() {
        try {

            //Setup PendingIntent
            mPendingIntentUsbPermission = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            //filter.addAction(ACTION_USB_ATTACHED);
            filter.addAction(ACTION_USB_DETACHED);
            //Register BroadcastReceiver
            registerReceiver(mUsbReceiver, filter);

            //PowerManager
            filter = new IntentFilter(ACTION_SCREEN_ON);
            filter.addAction(ACTION_SCREEN_OFF);
            //Register BroadcastReceiver
            registerReceiver(mPowerManagerReceiver, filter);
        } catch (Exception ex) {
            Log.e(MainActivity.TAG, ex.getMessage(), ex);
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
                        Log.e(MainActivity.TAG, "permission denied for accessory " + accessory);
                    }
                }
            //} else if (mUsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            //    synchronized (this) {
            //        //UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            //        Log.e(MainActivity.TAG, "ACTION_USB_DEVICE_ATTACHED");
            //        initUsbDevices(mUseCustomPrinter);
            //    }
            } else if (mUsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    Log.e(MainActivity.TAG, "ACTION_USB_DEVICE_DETACHED");
                    try {
                        //Close Application
                        if (mUseCustomPrinter) {
                            mCustomPrinterInterface.onExit();
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mPowerManagerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_SCREEN_ON.equals(action)) {
                synchronized (this) {
                    Log.e(MainActivity.TAG, "ACTION_SCREEN_ON");
                }
            } else if (ACTION_SCREEN_OFF.equals(action)) {
                synchronized (this) {
                    Log.e(MainActivity.TAG, "ACTION_SCREEN_OFF");
                }
            }
        }
    };

//--------------------------------------------------------------------------------------------------------------
// Actions

    private void actionOpenDevice() {
        //Open it
        mCustomPrinterInterface.openDevice();
    }

    private void actionTestDevicePrintText() {
        mCustomPrinterInterface.testPrintText(PRINT_TEXT);
    }

    private void actionTestDevicePrintImage() {
        InputStream inputStream = Utils.getInputStreamFromRawResource(this, R.raw.image);
        mCustomPrinterInterface.testPrintImage(inputStream);
    }

    private void actionTestAlarmStart() {
        Utils.alarmStartPlay(this, mRingtone);
    }

    private void actionTestAlarmStop() {
        Utils.alarmStopPlay(mRingtone);
    }

    private void actionTestTicket() {

        final Context context = this;

        if (mCustomPrinterInterface != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mTicket == null) {
                                //Init Ticket
                                mTicket = new Ticket(context, mCustomPrinterInterface);
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

    private void actionTestScreenOff() {

//Screen Off
        final Activity finalContext = (Activity) this;
        Utils.powerManagerScreenOff(this, mPackageName);

        //http://stackoverflow.com/questions/6560426/android-devicepolicymanager-locknow
        //ComponentName devAdminReceiver; // this would have been declared in your class body
        // then in your onCreate
        //mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        //devAdminReceiver = new ComponentName(context, deviceAdminReceiver.class);
        //then in your onResume

        //mDevicePolicyManager.lockNow();
        //finish();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.powerManagerScreenOn(finalContext, mPackageName);
                    }
                }, 60000);
            }
        });
    }

//--------------------------------------------------------------------------------------------------------------
// Actions

    private void requestPermission(UsbDevice pDevice) {
        mUsbManager.requestPermission(pDevice, mPendingIntentUsbPermission);
    }

    private void initUsbDevices(Boolean useCustomPrinter) {
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
            Log.d(TAG, deviceMessage);

            //Show Interfaces
            for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {

                mUsbInterface = mUsbDevice.getInterface(i);
                Log.d(TAG, String.format("Interface DescribeContents: %d", mUsbInterface.describeContents()));

                //Request Permission: Show the Request Permission USB Dialog :)
                requestPermission(mUsbDevice);

                //Show Endpoints
                for (int j = 0; j < mUsbInterface.getEndpointCount(); j++) {
                    mUsbEndpoint = mUsbInterface.getEndpoint(j);
                    Log.d(TAG, String.format("EndPoint DescribeContents: %d", mUsbEndpoint.describeContents()));

                    //Test EndPoint : Custom TG2460H
                    if (mUsbDevice.getVendorId() == 3540 /*&& mUsbDevice.getProductId() == 423*/) {

                        //testEndPoint(mUsbDevice, mUsbInterface, mUsbEndpoint);
                        Log.d(TAG, String.format("ProductId: %d / VendorId: %d", mUsbDevice.getProductId(), mUsbDevice.getVendorId()));

                        //Init CustomPrinterInterface
                        if (useCustomPrinter) {
                            mCustomPrinterInterface = new CustomPrinterInterface(this, mViewActivityMain, mUsbDevice, mRingtone);
                        }
                    }
                }
            }
        }
        if (mDeviceList.size() <= 0) {
            Log.d(TAG, "No Devices Found");
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
}
