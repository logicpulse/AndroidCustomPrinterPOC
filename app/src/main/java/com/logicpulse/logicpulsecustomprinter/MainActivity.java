package com.logicpulse.logicpulsecustomprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.media.Ringtone;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.logicpulse.logicpulsecustomprinter.Ticket.Ticket;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "CustomPrinterPOC";
    public static final String PRINT_TEXT = "Texting CustomPrinterPOC...";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent pendingIntent;
    public String packageName;
    private CustomPrinterInterface customPrinterInterface;
    private Ringtone ringtone;

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

        View viewActivityMain = findViewById(R.id.content_main);

        //Get Package Name
        packageName = getApplicationContext().getPackageName();

        //Init Permissions
        Utils.copyFileHardwareUsbHostToSystemPermission(this);

        //Init CustomPrinterInterface
        customPrinterInterface = new CustomPrinterInterface(
                //Fix for listViewDevices textColor
                //never use getApplicationContext(). Just use your Activity as the Context
                this /*this.getApplicationContext()*/,
                viewActivityMain,
                savedInstanceState
        );

        //Register BroadcastReceiver()
        registerBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            customPrinterInterface.onExit();
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

        if (id == R.id.action_settings) {
            return true;
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

        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver

    private void registerBroadcastReceiver() {
        try {
            pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            //Register BroadcastRecevive
            registerReceiver(mUsbReceiver, filter);
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
            }
        }
    };

    //--------------------------------------------------------------------------------------------------------------
    // Actions

    private void actionOpenDevice() {
        //Open it
        customPrinterInterface.openDevice();
    }

    private void actionTestDevicePrintText() {
        customPrinterInterface.testPrintText(PRINT_TEXT);
    }

    private void actionTestDevicePrintImage() {
        InputStream inputStream = Utils.getInputStreamFromRawResource(this, R.raw.image);
        customPrinterInterface.testPrintImage(inputStream);
    }

    private void actionTestAlarmStart() {
        Utils.alarmStartPlay(this, ringtone);
    }

    private void actionTestAlarmStop() {
        Utils.alarmStopPlay(ringtone);
    }

    private void actionTestTicket() {
        Ticket ticket = new Ticket(this);
    }
}
