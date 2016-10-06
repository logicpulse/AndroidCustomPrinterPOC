package com.logicpulse.logicpulsecustomprinter;

import android.media.Ringtone;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.logicpulse.logicpulsecustomprinter.Ticket.Ticket;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "CustomPrinterPOC";
    public static final String PRINT_TEXT = "Texting CustomPrinterPOC...";
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

        //Init CustomPrinterInterface
        customPrinterInterface = new CustomPrinterInterface(
                //Fix for listViewDevices textColor
                //never use getApplicationContext(). Just use your Activity as the Context
                this /*this.getApplicationContext()*/,
                viewActivityMain,
                savedInstanceState
        );
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
