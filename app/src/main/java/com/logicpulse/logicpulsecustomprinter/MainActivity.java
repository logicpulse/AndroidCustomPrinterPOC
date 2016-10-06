package com.logicpulse.logicpulsecustomprinter;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomPrinter;

public class MainActivity extends AppCompatActivity {

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

        //Init CustomPrinterInterface
        CustomPrinterInterface customPrinterInterface = new CustomPrinterInterface(this.getApplicationContext(), savedInstanceState);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open_printer) {
            actionOpenPrinter();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_test_printer) {
            actionTestPrinter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void actionOpenPrinter() {
        //Open it
        //CustomPrinter prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(usbDevice, this);
    }

    private void actionTestPrinter() {

    }
}
