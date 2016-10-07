package com.logicpulse.logicpulsecustomprinter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;
import static android.provider.Telephony.Mms.Part.FILENAME;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class Utils {

    //Used in CustomPrinterInterface
    public static String intToHexString(int iValue, int num0chars) {
        String hexString;

        //convert to Hex
        hexString = Integer.toHexString(iValue);
        int num0towrite = num0chars - hexString.length();
        //If i need to add some "0" before
        if (num0towrite > 0) {
            for (int i = 0; i < num0towrite; i++)
                hexString = "0" + hexString;
        }

        //Change to upper case
        hexString = hexString.toUpperCase();

        return hexString;
    }

    public static InputStream getInputStreamFromRawResource(Context context, int resourceId) {
        try {
            Resources res = context.getResources();
            InputStream inputStream = res.openRawResource(resourceId);
            return inputStream;
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "File Reading Error", e);
        }
        return null;
    }

    public static Drawable getDrawableFromRawResource(Context context, int resourceId) {
        try {
            Drawable drawable = Drawable.createFromStream(
                    getInputStreamFromRawResource(context, resourceId),
                    "Get Full Image Task"
            );
            return drawable;
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "File Reading Error", e);
        }
        return null;
    }

    public static void alarmStartPlay(Context context, Ringtone ringtone) {
        if (ringtone != null && ! ringtone.isPlaying()) {
            ringtone.play();
        }
    }

    public static void alarmStopPlay(Ringtone ringtone) {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    //This method called from Activity to Work ex MainActivity.showAlert
    public static void showAlert(Activity activity, String message) {

        TextView textView = new TextView(activity);
        //textView.setText(title);
        textView.setPadding(10, 10, 10, 10);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(20);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        //builder.setTitle(title);
        builder.setCustomTitle(textView);
        builder.setIcon(R.mipmap.printer_error);

        builder.setMessage(message);

        builder.setCancelable(false);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }


    public static void remountFileSystem() {
        try {
            //Runtime.getRuntime().exec("su mount -o remount,rw /system");
            Runtime.getRuntime().exec("mount -o rw,remount -t rootfs /");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //copy "android.hardware.usb.host.xml" to /system/etc/permission
    //check with "cat /system/etc/permissions/android.hardware.usb.host.xml"
    //required here CustomAndroidAPI().getPrinterDriverUSB
    //else "User has not given permission to device UsbDevice[mName=/dev/bus/usb/001/026,mVendorId=3540,mProductId=423,mClass=0,mSubclass=0,mProtocol=0,mManufacturerName=CUSTOM Engineering S.p.A.,mProductName=TG2460-H,mSerialNumber=TG2460-H Num.: 0,mConfigurations=["

    //system/etc/permissions/android.hardware.usb.host.xml contains a path separator

    //How to enable USB host API support
    //https://github.com/452/USBHIDTerminal/wiki/How-to-enable-USB-host-API-support

    public static void copyFileHardwareUsbHostToSystemPermission(Context context) {
        try {
            String permissionFile = "android.hardware.usb.host.xml";
            //https://developer.android.com/reference/android/os/Environment.html#getRootDirectory()
            File extRootFileSystem = Environment.getRootDirectory();
            File fileRootFileSystem = new File(String.format("%s/etc/permissions/%s", extRootFileSystem.getAbsolutePath(), permissionFile));
            File dataDirectory = new File(context.getApplicationInfo().dataDir);
            File fileDataDirectory = new File(String.format("%s/files/%s", dataDirectory.getAbsolutePath(), permissionFile));

            if(! fileRootFileSystem.exists()) {
                //Copy to data dir First
                copyFile(context, "android.hardware.usb.host.xml", fileDataDirectory.getAbsolutePath());

                remountFileSystem();
                //android EROFS (Read-only file system)
                //Fix: using the permission of WRITE_EXTERNAL_STORAGE use that whether there is an external card or not.
                copyFile(context, fileDataDirectory.getAbsolutePath(), "/system/etc/permissions");
                Utils.showAlert((Activity) context, "REBOOT REQUIRED");
                Runtime.getRuntime().exec("su reboot");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Using the Internal Storage
    //https://developer.android.com/guide/topics/data/data-storage.html#filesInternal
    //http://stackoverflow.com/questions/4447477/android-how-to-copy-files-from-assets-folder-to-sdcard
    public static void copyAssets(Context context, String path, String outPath) {
        AssetManager assetManager = context.getAssets();
        String assets[];
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(context, path, outPath);
            } else {
                String fullPath = outPath + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    if (!dir.mkdir()) Log.e(TAG, "No create external directory: " + dir );
                for (String asset : assets) {
                    copyAssets(context, path + "/" + asset, outPath);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "I/O Exception", ex);
        }
    }

    private static void copyFile(Context context, String filename, String outPath) {
        AssetManager assetManager = context.getAssets();

        InputStream in;
        OutputStream out;

        try {
            in = assetManager.open(filename);
            String newFileName = outPath + "/" + filename;
            out = new FileOutputStream(newFileName);

            //remountFileSystem();
            //The openFileInput method will not accept path separators.('/')
            //it accepts only the name of the file which you want to open/access. so change the statement
            //out = context.openFileOutput(filename, Context.MODE_PRIVATE);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
