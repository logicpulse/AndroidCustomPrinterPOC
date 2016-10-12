package com.logicpulse.logicpulsecustomprinter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.media.Ringtone;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.logicpulse.logicpulsecustomprinter.Printers.IThermalPrinter;

import java.io.InputStream;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;
import it.custom.printer.api.android.PrinterFont;
import it.custom.printer.api.android.PrinterStatus;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class CustomPrinterInterface implements IThermalPrinter {

    private int GETSTATUS_TIME = 1000;
    private Context mContext;
    private static Handler mHandelerGetStatus = new Handler();
    private String mLock = "lockAccess";
    private String mAPIVersion;
    //Require to get mView to use mView.findViewById from outside activity
    private View mView;
    private Ringtone mRingtone;
    private UsbDevice mUsbDevice;
    //Public
    private static CustomPrinter prnDevice = null;
    //Custom UI : Removed
    //private static UsbDevice[] mUsbDeviceList = null;
    //private static ListView mListDevicesView;
    //private static ArrayAdapter<String> mListAdapter;
    //private static int mLastDeviceSelected = -1;
    //private static int mDeviceSelected = -1;
    //public static CustomPrinter getPrnDevice() {
    //    return prnDevice;
    //}

    //Parametless Constructor
    public CustomPrinterInterface() { }

    //Used with detected UsbDevice
    public void init(Context context, View view, UsbDevice usbDevice, Ringtone ringtone) {

        //Parameters
        this.mContext = context;
        this.mView = view;
        this.mUsbDevice = usbDevice;
        this.mRingtone = ringtone;
        //Get Api Version
        this.mAPIVersion = CustomAndroidAPI.getAPIVersion();

        //Start the get status thread after GETSTATUS_TIME msec
        mHandelerGetStatus.postDelayed(GetStatusRunnable, GETSTATUS_TIME);

        //User has not given permission to device UsbDevice[mName=/dev/bus/usb/001/026,mVendorId=3540,mProductId=423,mClass=0,mSubclass=0,mProtocol=0,mManufacturerName=CUSTOM Engineering S.p.A.,mProductName=TG2460-H,mSerialNumber=TG2460-H Num.: 0,mConfigurations=[
        //http://stackoverflow.com/questions/11817192/android-copy-res-raw-resource-to-sd-correctly
        //InputStream inputStream = mContext.getResources().openRawResource(R.raw.android_hardware_usb_host);
        //File > New > folder > assets Folder
        //Note : App must be selected before creating folder.

        //init everything
        //init(view, savedInstanceState);
        //With Detected Usb
        //init();

        //Start Open
        //openDevice();


        //Force Detected mUsbDevice
        try {
            if (prnDevice == null) {
                CustomAndroidAPI customAndroidAPI = new CustomAndroidAPI();
                prnDevice = customAndroidAPI.getPrinterDriverUSB(mUsbDevice, mContext);
            }
        } catch (CustomException e) {
            //Show Error
            e.printStackTrace();
            String errorMessage = String.format("Error init Printer: init");
            Utils.showAlert((Activity) mContext, errorMessage);
            Log.e(MainActivity.TAG, errorMessage);
        }
    }

    /* USE Custom Search Devices : Working but disabled
    private void init(View view, Bundle savedInstanceState) {

        //init Ringtone
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);

        //If is the 1st time
        if (savedInstanceState == null) {
            try {
                //Get the list of devices
                mUsbDeviceList = CustomAndroidAPI.EnumUsbDevices(mContext);

                if ((mUsbDeviceList == null) || (mUsbDeviceList.length == 0)) {
                    //Show Error
                    String errorMessage = String.format("Printer Error: No Devices Connected...");
                    //Utils.showAlert((Activity) mContext, errorMessage);
                    Log.e(MainActivity.TAG, errorMessage);
                    return;
                }
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
                return;
            } catch (Exception e) {
                //Show Error
                String errorMessage = String.format("Printer Error: Enum devices error...");
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
                return;
            }
        }

        // Find the ListView resource.
        mListDevicesView = (ListView) view.findViewById(R.id.listViewDevices);

        // Create and populate a List of Devices
        String[] strDevices = new String[mUsbDeviceList.length];
        for (int i = 0; i < mUsbDeviceList.length; i++) {
            strDevices[i] = (i + 1) + ". USB Device VID: 0x" + Utils.intToHexString(mUsbDeviceList[i].getVendorId(), 4) + " PID: 0x" + Utils.intToHexString(mUsbDeviceList[i].getProductId(), 4);
        }

        ArrayList<String> devicesList = new ArrayList<String>();
        devicesList.addAll(Arrays.asList(strDevices));

        // Create ArrayAdapter using the list.
        mListAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_single_choice, devicesList);

        // Set the ArrayAdapter as the ListView's adapter.
        mListDevicesView.setAdapter(mListAdapter);

        mListDevicesView.setItemsCanFocus(false);
        mListDevicesView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDeviceSelected = 0;
        mListDevicesView.setItemChecked(mDeviceSelected, true); //Select the 1st
        mListDevicesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //Save position Value
                mDeviceSelected = arg2;
            }
        });
    }
    */

    private Runnable GetStatusRunnable = new Runnable() {
        public void run() {
            String printerName;

            //int deviceShowStatus = View.INVISIBLE;
            int deviceShowStatus = View.VISIBLE;
            Boolean connectionError = true;

            CheckBox ckbox;
            TextView txtView;

            //If the device is open
            if (prnDevice != null) {
                synchronized (mLock) {
                    try {
                        //Get printer Status
                        PrinterStatus prnSts = prnDevice.getPrinterFullStatus();

                        //if can getPrinterFullStatus, disable connection error
                        connectionError = false;

                        //Check it: CONNERROR)
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxCONNERROR);
                        ckbox.setChecked(connectionError);

                        //Check it: NOPAPER
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxNOPAPER);
                        ckbox.setChecked(prnSts.stsNOPAPER);

                        //Check it: PAPER JAM
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxPAPERJAM);
                        ckbox.setChecked(prnSts.stsPAPERJAM);

                        //Check it: PAPER ROLLING
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxROLLING);
                        ckbox.setChecked(prnSts.stsPAPERROLLING);

                        //Check it: LF KEY PRESSED
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxLF);
                        ckbox.setChecked(prnSts.stsLFPRESSED);

                        //Get printer name
                        printerName = prnDevice.getPrinterName();

                        //Show Text PrinterName
                        txtView = (TextView) mView.findViewById(R.id.textPrinterName);
                        txtView.setText("Printer Name:" + printerName + " (" + prnDevice.getPrinterInfo() + ")");

                        //Alarm Work
                        if (connectionError || prnSts.stsNOPAPER == true || prnSts.stsPAPERJAM == true) {
                            Utils.alarmStartPlay(mContext, mRingtone);
                        }
                        else {
                            Utils.alarmStopPlay(mRingtone);
                        }

                        //deviceShowStatus = View.VISIBLE;
                    } catch (CustomException e) {
                        //Function failed: Error 4 (Printer communication error)
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxCONNERROR);
                        ckbox.setChecked(connectionError);
                        //Show Error
                        String errorMessage = String.format("Printer Error: %s", e.getMessage());
                        //Utils.showAlert((Activity) mContext, errorMessage);
                        Log.e(MainActivity.TAG, errorMessage);
                        //Alarm Work
                        Utils.alarmStartPlay(mContext, mRingtone);
                    } catch (Exception e) {
                        ckbox = (CheckBox) mView.findViewById(R.id.checkBoxCONNERROR);
                        ckbox.setChecked(connectionError);
                        //Show Error
                        String errorMessage = String.format("Printer Error: %s", e.getMessage());
                        //Utils.showAlert((Activity) mContext, errorMessage);
                        Log.e(MainActivity.TAG, errorMessage);
                        //Alarm Work
                        Utils.alarmStartPlay(mContext, mRingtone);
                    }
                }
            }
            //else {
            //    ckbox = (CheckBox) mView.findViewById(R.id.checkBoxCONNERROR);
            //    ckbox.setChecked(connectionError);
            //    //Alarm Work
            //    Utils.alarmStartPlay(mContext, mRingtone);
            //}

            //Show / Hide Check CONNERROR
            ckbox = (CheckBox) mView.findViewById(R.id.checkBoxCONNERROR);
            ckbox.setVisibility(deviceShowStatus);
            //Alarm Work
            if (connectionError) {
                ckbox.setChecked(connectionError);
                Utils.alarmStartPlay(mContext, mRingtone);
            }

            //Show / Hide Check NOPAPER
            ckbox = (CheckBox) mView.findViewById(R.id.checkBoxNOPAPER);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Check PAPERJAM
            ckbox = (CheckBox) mView.findViewById(R.id.checkBoxPAPERJAM);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Check PAPER ROLLING
            ckbox = (CheckBox) mView.findViewById(R.id.checkBoxROLLING);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Check LF KEY PRESSED
            ckbox = (CheckBox) mView.findViewById(R.id.checkBoxLF);
            ckbox.setVisibility(deviceShowStatus);

            //Show / Hide Text PrinterName
            txtView = (TextView) mView.findViewById(R.id.textPrinterName);
            txtView.setVisibility(deviceShowStatus);

            //run again in GETSTATUS_TIME msec
            mHandelerGetStatus.postDelayed(GetStatusRunnable, GETSTATUS_TIME);
        }
    };

    //Open the device if it isn't already opened
    //public boolean openDevice() {

        ////Required to use the new InitUdb
        //if (prnDevice == null) {
        //    init();
        //    return true;
        //}
        //else {
        //    return true;
        //}

        /*
        //Device not selected
        if (mDeviceSelected == -1) {
            //Show Error
            String errorMessage = String.format("Printer Error: No Printer Device Selected...");
            //Utils.showAlert((Activity) mContext, errorMessage);
            Log.e(MainActivity.TAG, errorMessage);
            return false;
        }

        //If i changed the device
        if (mLastDeviceSelected != -1) {
            if (mDeviceSelected != mLastDeviceSelected) {
                try {
                    //Force close
                    prnDevice.close();
                } catch (CustomException e) {
                    //Show Error
                    String errorMessage = String.format("Printer Error: %s", e.getMessage());
                    Utils.showAlert((Activity) mContext, errorMessage);
                    Log.e(MainActivity.TAG, errorMessage);
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
                prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(mUsbDeviceList[mDeviceSelected], mContext);
                //Save last device selected
                mLastDeviceSelected = mDeviceSelected;
                return true;
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
                return false;
            } catch (Exception e) {
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
                //open error
                return false;
            }
        }
        //Already opened
        return true;
        */
    //}

    public void printText(String text, Object printerFont, Integer feeds) {

        //open device
        //if (openDevice() == false) return;

        synchronized (mLock) {
            try {
                prnDevice.printTextLF(text, (PrinterFont) printerFont);
                if (feeds > 0) prnDevice.feed(feeds);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }
    }

    public void printImage(Bitmap bitmap, Integer align, Integer scaletofit, Integer width, Integer feeds) {

        //open device
        //if (openDevice() == false) return;

        synchronized (mLock) {
            //***************************************************************************
            // PRINT PICTURE
            //***************************************************************************

            try {
                //Print (Left Align and Fit to printer width)
                prnDevice.printImage(bitmap, align, scaletofit, width);
                if (feeds > 0) prnDevice.feed(feeds);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = "Printer Error: Print Picture Error...";
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }

    }

    public void printBarCode(String text, Integer barcodetype, Integer barcodehritype, Integer align, Integer barcodewidth, Integer height, Integer feeds) {
        //open device
        //if (openDevice() == false) return;

        synchronized (mLock) {
            try {
                prnDevice.printBarcode(text, barcodetype, barcodehritype, align, barcodewidth, height);
                if (feeds > 0) prnDevice.feed(feeds);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }
    }

    public void printBarCode2D(String text, Integer barcodetype, Integer align, Integer width, Integer feeds) {
        //open device
        //if (openDevice() == false) return;

        synchronized (mLock) {

            try {
                prnDevice.printBarcode2D(text, barcodetype, align, width);
                if (feeds > 0) prnDevice.feed(feeds);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }
    }

    public void cut(Integer cutMode, Integer feeds) {

        //open device
        //if (openDevice() == false) return;

        synchronized (mLock) {

            try {
                if (feeds > 0) prnDevice.feed(feeds);
                prnDevice.cut(cutMode);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }
    }

    public void testPrintText(String text) {
        PrinterFont fntPrinterNormal = new PrinterFont();
        PrinterFont fntPrinterBold2X = new PrinterFont();
        String strTextToPrint;

        //open device
        //if (openDevice() == false) return;

        //Get Text
        strTextToPrint = text;

        try {
            //Fill class: NORMAL
            fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X1);                   //Height x1
            fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X1);                    //Width x1
            fntPrinterNormal.setEmphasized(false);                                      //No Bold
            fntPrinterNormal.setItalic(false);                                          //No Italic
            fntPrinterNormal.setUnderline(false);                                       //No Underline
            fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);   //Center
            fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);      //Default International Chars

            //Fill class: BOLD size 2X
            fntPrinterBold2X.setCharHeight(PrinterFont.FONT_SIZE_X2);                    //Height x2
            fntPrinterBold2X.setCharWidth(PrinterFont.FONT_SIZE_X2);                     //Width x2
            fntPrinterBold2X.setEmphasized(true);                                        //Bold
            fntPrinterBold2X.setItalic(false);                                           //No Italic
            fntPrinterBold2X.setUnderline(false);                                        //No Underline
            fntPrinterBold2X.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);    //Center
            fntPrinterBold2X.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);       //Default International Chars
        } catch (CustomException e) {
            //Show Error
            String errorMessage = String.format("Printer Error: %s", e.getMessage());
            Utils.showAlert((Activity) mContext, errorMessage);
            Log.e(MainActivity.TAG, errorMessage);
        } catch (Exception e) {
            //Show Error
            String errorMessage = String.format("Printer Error: Set font properties error...");
            Utils.showAlert((Activity) mContext, errorMessage);
            Log.e(MainActivity.TAG, errorMessage);
        }

        //***************************************************************************
        // PRINT TEXT
        //***************************************************************************

        synchronized (mLock) {
            try {
                //Print Text (NORMAL)
                prnDevice.printText(strTextToPrint, fntPrinterNormal);
                prnDevice.printTextLF(strTextToPrint, fntPrinterNormal);
                //Print Text (BOLD size 2X)
                prnDevice.printTextLF(strTextToPrint, fntPrinterBold2X);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }
    }

    public void testPrintImage(InputStream inputStream) {
        //open device
        //if (openDevice() == false) return;

        // load image

        //Create the Bitmap
        //Bitmap image = BitmapFactory.decodeFile(selectedImagePath);
        Bitmap image = BitmapFactory.decodeStream(inputStream);

        synchronized (mLock) {
            //***************************************************************************
            // PRINT PICTURE
            //***************************************************************************

            try {
                //Print (Left Align and Fit to printer width)
                prnDevice.printImage(image, CustomPrinter.IMAGE_ALIGN_TO_LEFT, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
            } catch (CustomException e) {
                //Show Error
                String errorMessage = String.format("Printer Error: %s", e.getMessage());
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            } catch (Exception e) {
                //Show Error
                String errorMessage = "Printer Error: Print Picture Error...";
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
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
                    String errorMessage = String.format("Printer Error: %s", e.getMessage());
                    Utils.showAlert((Activity) mContext, errorMessage);
                    Log.e(MainActivity.TAG, errorMessage);
                }
            } catch (Exception e) {
                //Show Error
                String errorMessage = "Printer Error: Print Picture Error...";
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
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
                    String errorMessage = String.format("Printer Error: %s", e.getMessage());
                    Utils.showAlert((Activity) mContext, errorMessage);
                    Log.e(MainActivity.TAG, errorMessage);
                }
            } catch (Exception e) {
                //Show Error
                String errorMessage = "Printer Error: Print Picture Error...";
                Utils.showAlert((Activity) mContext, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        }
    }

    public void destroy() throws Throwable {
        try {
            if (prnDevice != null) {
                //Close device
                prnDevice.close();
            }
            if (mRingtone.isPlaying()) {
                Utils.alarmStopPlay(mRingtone);
            }
        } catch (CustomException e) {
            //Show Error
            String errorMessage = String.format("Printer Error: %s", e.getMessage());
            Utils.showAlert((Activity) mContext, errorMessage);
            Log.e(MainActivity.TAG, errorMessage);
        } catch (Exception e) {
        }

        //Force Close Activity
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
