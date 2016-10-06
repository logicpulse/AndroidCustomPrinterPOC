package com.logicpulse.logicpulsecustomprinter;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "CustomPrinterPOC";
    public String packageName;
    public static final String PRINT_TEXT = "Texting CustomPrinterPOC...";
    private CustomPrinterInterface customPrinterInterface;

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

        //String uriLocation = String.format("android.resource://%s/%s", packageName, + R.raw.image);

        //if (URLUtil.isValidUrl(uriLocation)) {
        //    Uri uri = Uri.parse(uriLocation);
        //    customPrinterInterface.testPrintImage(uri);
        //}
        InputStream inputStream = Utils.getInputStreamFromRawResource(this, R.raw.image);
        customPrinterInterface.testPrintImage(inputStream);
    }
}
