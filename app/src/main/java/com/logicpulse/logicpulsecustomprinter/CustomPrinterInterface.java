package com.logicpulse.logicpulsecustomprinter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;
import it.custom.printer.api.android.PrinterFont;
import it.custom.printer.api.android.PrinterStatus;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class CustomPrinterInterface {

    private Context context;

    private int INT_SELECT_PICTURE = 1;
    private int GETSTATUS_TIME = 1000;        //1sec

    private static UsbDevice[] usbDeviceList = null;
    private static Handler hGetStatus = new Handler();

    private static CustomPrinter prnDevice = null;

    public static CustomPrinter getPrnDevice() {
        return prnDevice;
    }

    private static ListView listDevicesView;
    private static ArrayAdapter<String> listAdapter;

    private static int lastDeviceSelected = -1;
    private static int deviceSelected = -1;

    private String lock = "lockAccess";

    private String aPIVersion;

    //Require to get view to use view.findViewById from outside activity
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

            //int deviceShowStatus = View.INVISIBLE;
            int deviceShowStatus = View.VISIBLE;

            CheckBox ckbox;
            TextView txtView;

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

                        //deviceShowStatus = View.VISIBLE;

                    } catch (CustomException e) {

                    } catch (Exception e) {
                        Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
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

    //Open the device if it isn't already opened
    public boolean openDevice() {
        //Device not selected
        if (deviceSelected == -1) {
            //showAlertMsg("Error...", "No Printer Device Selected...");
            Log.d(MainActivity.TAG, "Error: No Printer Device Selected...");
            return false;
        }

        //If i changed the device
        if (lastDeviceSelected != -1) {
            if (deviceSelected != lastDeviceSelected) {
                try {
                    //Force close
                    prnDevice.close();
                } catch (CustomException e) {

                    //Show Error
                    //showAlertMsg("Error...", e.getMessage());
                    Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
                    return false;
                } catch (Exception e) {
                    //Show error
                    return false;
                }
                prnDevice = null;
            }
        }

        //If i never open it
        if (prnDevice == null) {
            try {
                //Open and connect it
                prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList[deviceSelected], context);
                //Save last device selected
                lastDeviceSelected = deviceSelected;
                return true;
            } catch (CustomException e) {

                //Show Error
                //showAlertMsg("Error...", e.getMessage());
                Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
                return false;
            } catch (Exception e) {
                //showAlertMsg("Error...", "Open Print Error...");
                Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
                //open error
                return false;
            }
        }
        //Already opened
        return true;

    }

    public void testPrintText(String text) {
        PrinterFont fntPrinterNormal = new PrinterFont();
        PrinterFont fntPrinterBold2X = new PrinterFont();
        String strTextToPrint;
        //open device
        if (openDevice() == false)
            return;

        //Get Text
        strTextToPrint = text;

        try {
            //Fill class: NORMAL
            fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X1);                    //Height x1
            fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X1);                    //Width x1
            fntPrinterNormal.setEmphasized(false);                                        //No Bold
            fntPrinterNormal.setItalic(false);                                            //No Italic
            fntPrinterNormal.setUnderline(false);                                        //No Underline
            fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);    //Center
            fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);        //Default International Chars

            //Fill class: BOLD size 2X
            fntPrinterBold2X.setCharHeight(PrinterFont.FONT_SIZE_X2);                    //Height x2
            fntPrinterBold2X.setCharWidth(PrinterFont.FONT_SIZE_X2);                    //Width x2
            fntPrinterBold2X.setEmphasized(true);                                        //Bold
            fntPrinterBold2X.setItalic(false);                                            //No Italic
            fntPrinterBold2X.setUnderline(false);                                        //No Underline
            fntPrinterBold2X.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);    //Center
            fntPrinterBold2X.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);        //Default International Chars
        } catch (CustomException e) {
            //Show Error
            //showAlertMsg("Error...", e.getMessage());
            Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
        } catch (Exception e) {
            //showAlertMsg("Error...", "Set font properties error...");
            Log.d(MainActivity.TAG, "Error: Set font properties error...");
        }

        //***************************************************************************
        // PRINT TEXT
        //***************************************************************************

        synchronized (lock) {
            try {
                //Print Text (NORMAL)
                prnDevice.printText(strTextToPrint, fntPrinterNormal);
                prnDevice.printTextLF(strTextToPrint, fntPrinterNormal);
                //Print Text (BOLD size 2X)
                prnDevice.printTextLF(strTextToPrint, fntPrinterBold2X);
            } catch (CustomException e) {
                //Show Error
                //showAlertMsg("Error...", e.getMessage());
                Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
            } catch (Exception e) {
                //showAlertMsg("Error...", "Print Text Error...");
                Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
            }
        }
    }

    public void testPrintImage(InputStream inputStream) {
        //open device
        if (openDevice() == false)
            return;

        //showAlertMsg("Error...", "Select a Picture to Print...");
        //Log.d(MainActivity.TAG, String.format("Error: Invalid Uri %s", uri.toString()));

        // load image

        //Create the Bitmap
        //Bitmap image = BitmapFactory.decodeFile(selectedImagePath);
        Bitmap image = BitmapFactory.decodeStream(inputStream);

        synchronized (lock) {
            //***************************************************************************
            // PRINT PICTURE
            //***************************************************************************

            try {
                //Print (Left Align and Fit to printer width)
                prnDevice.printImage(image, CustomPrinter.IMAGE_ALIGN_TO_LEFT, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
            } catch (CustomException e) {
                //Show Error
                //showAlertMsg("Error...", e.getMessage());
                Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
            } catch (Exception e) {
                //showAlertMsg("Error...", "Print Picture Error...");
                Log.d(MainActivity.TAG, "Error: Print Picture Error...");
            }

            //***************************************************************************
            // FEEDS and CUT
            //***************************************************************************

            try {
                //Feeds (3)
                prnDevice.feed(3);
                //Cut (Total)
                prnDevice.cut(CustomPrinter.CUT_TOTAL);
            } catch (CustomException e) {
                //Only if isn't unsupported
                if (e.GetErrorCode() != CustomException.ERR_UNSUPPORTEDFUNCTION) {
                    //Show Error
                    //showAlertMsg("Error...", e.getMessage());
                    Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
                }
            } catch (Exception e) {
                //showAlertMsg("Error...", "Print Picture Error...");
                Log.d(MainActivity.TAG, "Error: Print Picture Error...");
            }

            //***************************************************************************
            // PRESENT
            //***************************************************************************

            try {
                //Present (40mm)
                prnDevice.present(40);
            } catch (CustomException e) {
                //Only if isn't unsupported
                if (e.GetErrorCode() != CustomException.ERR_UNSUPPORTEDFUNCTION) {
                    //Show Error
                    //showAlertMsg("Error...", e.getMessage());
                    Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
                }
            } catch (Exception e) {
                //showAlertMsg("Error...", "Print Picture Error...");
                Log.d(MainActivity.TAG, "Error: Print Picture Error...");
            }
        }
    }

    public void onExit() throws Throwable {
        try {
            if (prnDevice != null) {
                //Close device
                prnDevice.close();
            }
        } catch (CustomException e) {
            //Show Error
            //showAlertMsg("Error...", e.getMessage());
            Log.d(MainActivity.TAG, String.format("Error: %s", e.getMessage()));
        } catch (Exception e) {
        }

        //Force Close
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
