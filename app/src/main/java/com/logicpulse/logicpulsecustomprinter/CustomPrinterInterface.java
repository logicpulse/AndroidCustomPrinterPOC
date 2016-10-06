package com.logicpulse.logicpulsecustomprinter;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;
import it.custom.printer.api.android.PrinterStatus;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class CustomPrinterInterface {

    private Context context;

    private int INT_SELECT_PICTURE = 1;
    private int GETSTATUS_TIME = 1000;        //1sec

    static UsbDevice[] usbDeviceList = null;

    static CustomPrinter prnDevice = null;
    public static CustomPrinter getPrnDevice() {
        return prnDevice;
    }

    static ListView listDevicesView ;
    static ArrayAdapter<String> listAdapter;

    static int lastDeviceSelected = -1;
    static int deviceSelected = -1;

    private String lock = "lockAccess";

    static Handler hGetStatus = new Handler();

    String aPIVersion;

    //Require to use findViewById here
    private View view;

    public CustomPrinterInterface(Context context, View view, Bundle savedInstanceState) {

        //Parameters
        this.context = context;
        this.view = view;
        //Get Api Version
        this.aPIVersion = CustomAndroidAPI.getAPIVersion();

        //Start the get status thread after GETSTATUS_TIME msec
        hGetStatus.postDelayed(GetStatusRunnable, GETSTATUS_TIME);

        //Init everything
        InitEverything(view, savedInstanceState);
    }

    private void InitEverything(View view, Bundle savedInstanceState) {

////Require to get view to use view.findViewById from outside activity
//View view = LayoutInflater.from(context).inflate(R.layout.activity_main, null);

        //If is the 1st time
        if (savedInstanceState == null) {
            try {
                //Get the list of devices
                usbDeviceList = CustomAndroidAPI.EnumUsbDevices(context);

                if ((usbDeviceList == null) || (usbDeviceList.length == 0)) {
                    //Show Error
                    //Utils.showAlertMsg(context, "Error...", "No Devices Connected...");
                    Log.d(MainActivity.TAG, "Error: No Devices Connected...");
                    return;
                }
            } catch (CustomException e) {

                //Show Error
                //Utils.showAlertMsg(context, "Error...", e.getMessage());
                Log.e(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
                return;
            } catch (Exception e) {

                //Show Error
                //Utils.showAlertMsg(context, "Error...", "Enum devices error...");
                Log.e(MainActivity.TAG, String.format("Error: Enum devices error..."));
                return;
            }
        }

        // Find the ListView resource.
        listDevicesView = (ListView) view.findViewById(R.id.listViewDevices);

        // Create and populate a List of Devices
        String[] strDevices = new String[usbDeviceList.length];
        for (int i = 0; i < usbDeviceList.length; i++) {
            strDevices[i] = (i + 1) + ". USB Device VID: 0x" + Utils.intToHexString(usbDeviceList[i].getVendorId(), 4) + " PID: 0x" + Utils.intToHexString(usbDeviceList[i].getProductId(), 4);
        }

        ArrayList<String> devicesList = new ArrayList<String>();
        devicesList.addAll(Arrays.asList(strDevices));

        // Create ArrayAdapter using the list.
        listAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_single_choice, devicesList);

        // Set the ArrayAdapter as the ListView's adapter.
        listDevicesView.setAdapter(listAdapter);

        listDevicesView.setItemsCanFocus(false);
        listDevicesView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        deviceSelected = 0;
        listDevicesView.setItemChecked(deviceSelected, true); //Select the 1st
        listDevicesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //Save position Value
                deviceSelected = arg2;
            }
        });
    }

    private Runnable GetStatusRunnable = new Runnable() {
        public void run() {
            String printerName;
            int deviceShowStatus = View.INVISIBLE;
            CheckBox ckbox;
            TextView txtView;

////Require to get view to use view.findViewById from outside activity
//View view = LayoutInflater.from(context).inflate(R.layout.activity_main, null);

            //If the device is open
            if (prnDevice != null) {
                synchronized (lock) {
                    try {
                        //Get printer Status
                        PrinterStatus prnSts = prnDevice.getPrinterFullStatus();

                        //Check it: NOPAPER
                        ckbox = (CheckBox) view.findViewById(R.id.checkBoxNOPAPER);
                        ckbox.setChecked(prnSts.stsNOPAPER);

                        //Check it: PAPER ROLLING
                        ckbox = (CheckBox) view.findViewById(R.id.checkBoxROLLING);
                        ckbox.setChecked(prnSts.stsPAPERROLLING);

                        //Check it: LF KEY PRESSED
                        ckbox = (CheckBox) view.findViewById(R.id.checkBoxLF);
                        ckbox.setChecked(prnSts.stsLFPRESSED);

                        //Get printer name
                        printerName = prnDevice.getPrinterName();

                        //Show Text PrinterName
                        txtView = (TextView) view.findViewById(R.id.textPrinterName);
                        txtView.setText("Printer Name:" + printerName + " (" + prnDevice.getPrinterInfo() + ")");

                        deviceShowStatus = View.VISIBLE;

                    } catch (CustomException e) {

                    } catch (Exception e) {

                    }
                }
            }

            //Show / Hide Check NOPAPER
            ckbox = (CheckBox) view.findViewById(R.id.checkBoxNOPAPER);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Check PAPER ROLLING
            ckbox = (CheckBox) view.findViewById(R.id.checkBoxROLLING);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Check LF KEY PRESSED
            ckbox = (CheckBox) view.findViewById(R.id.checkBoxLF);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Text PrinterName
            txtView = (TextView) view.findViewById(R.id.textPrinterName);
            txtView.setVisibility(deviceShowStatus);

            //run again in GETSTATUS_TIME msec
            hGetStatus.postDelayed(GetStatusRunnable, GETSTATUS_TIME);
        }
    };
}
