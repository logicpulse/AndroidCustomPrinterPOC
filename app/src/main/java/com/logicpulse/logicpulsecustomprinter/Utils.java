package com.logicpulse.logicpulsecustomprinter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class Utils {

    //Used in CustomPrinterDevice
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

    public static String formatDate(Date date, String format) {
        String result = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            result = dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
        if (ringtone != null && !ringtone.isPlaying()) {
            Log.d(MainActivity.TAG, "Alarm Started");
            ringtone.play();
        }
    }

    public static void alarmStopPlay(Ringtone ringtone) {
        if (ringtone != null && ringtone.isPlaying()) {
            Log.d(MainActivity.TAG, "Alarm Stopped");
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

    public static void showConfirmReboot(Context context, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Context finalContext = context;

        builder.setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Utils.rebootDevice(finalContext);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle Cancel
                    }
                })
                .create()
                .show();
    }

    public static void remountFileSystem() {
        try {
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
            File dirRootFileSystem = Environment.getRootDirectory();
            File fileRootFileSystemPermission = new File(String.format("%s/etc/permissions/%s", dirRootFileSystem.getAbsolutePath(), permissionFile));
            File dirAppData = new File(context.getApplicationInfo().dataDir);
            File fileAppDataPermission = new File(String.format("%s/files/%s", dirAppData.getAbsolutePath(), permissionFile));

            if (!fileRootFileSystemPermission.exists()) {
                //Copy to data dir First
                copyFileFromAssets(context, permissionFile, String.format("%s/%s", dirAppData.getAbsolutePath(), "/files"));

                //TRICK IS: -c =  which tells su to execute the command that directly follows it on the same line
                String cmd = String.format("cp %s %s", fileAppDataPermission, fileRootFileSystemPermission);
                executeHasSu("mount -o remount,rw /system");
                executeHasSu(cmd);
                executeHasSu("mount -o remount,ro /system");

                if (fileRootFileSystemPermission.exists()) {
                    //Delete Temp File
                    fileAppDataPermission.delete();
                    //Show Message
                    String errorMessage = String.format("Successfully Copied USB Permission File to:\r\n%s\r\n\r\nWarning: Do you want reboot device to apply permissions?", fileRootFileSystemPermission);
                    Log.d(MainActivity.TAG, errorMessage);
                    showConfirmReboot(context, errorMessage);
                } else {
                    //Show Error
                    String errorMessage = String.format("Error copy USB Permission File to: %s", fileRootFileSystemPermission);
                    Utils.showAlert((Activity) context, errorMessage);
                    Log.e(MainActivity.TAG, errorMessage);
                }
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
                copyFileFromAssets(context, path, outPath);
            } else {
                String fullPath = outPath + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    if (!dir.mkdir()) Log.e(TAG, "No create external directory: " + dir);
                for (String asset : assets) {
                    copyAssets(context, path + "/" + asset, outPath);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "I/O Exception", ex);
        }
    }

    public static void copyFileFromAssets(Context context, String filename, String outPath) {
        AssetManager assetManager = context.getAssets();

        InputStream in;
        OutputStream out;

        try {
            in = assetManager.open(filename);
            String newFileName = outPath + "/" + filename;
            out = new FileOutputStream(newFileName);

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

    //The trick is -c to execute command has su ex "su -c reboot now"
    public static void executeHasSu(String command) {
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", command});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rebootDevice(Context context) {
        executeHasSu("reboot now");
    }

    public static boolean enableNetworkADB(Boolean enable) {
        boolean result = false;

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            if (enable) {
                os.writeBytes("setprop service.adb.tcp.port 5555\n");
            }
            else {
                os.writeBytes("setprop service.adb.tcp.port -1\n");
            }
            os.writeBytes("stop adbd\n");
            os.writeBytes("start adbd\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            result = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Turning screen on and off programmatically not working on some devices
    //http://stackoverflow.com/questions/13416563/turning-screen-on-and-off-programmatically-not-working-on-some-devices
    public static void powerManagerScreenOff(Context context, String packageName) {
        //PowerManager pm = (PowerManager)content.getSystemService(Service.POWER_SERVICE);
        //Intent intent = new Intent(mPackageName);
        //intent.setAction(Intent.ACTION_SCREEN_OFF);
        //content.startActivity(intent);

        //PowerManager pm = (PowerManager) content.getSystemService(Context.POWER_SERVICE);
        //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MainActivity.TAG);
        //wl.acquire();
        //..screen will stay on during this section..
        //wl.release();

        Utils.executeHasSu("reboot");

        //Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);

        //http://stackoverflow.com/questions/6560426/android-devicepolicymanager-locknow
        //DevicePolicyManager devicePolicyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //devAdminReceiver = new ComponentName(context, deviceAdminReceiver.class);
        //devicePolicyManager.lockNow();
    }

    //http://stackoverflow.com/questions/6756768/turn-off-screen-on-android
    //Needed permission:
    //<uses-permission android:name="android.permission.WRITE_SETTINGS" />
    public static void powerManagerScreenOn(Context context, String packageName) {
        //PowerManager pm = (PowerManager)content.getSystemService(Service.POWER_SERVICE);
        //Intent intent = new Intent(mPackageName);
        //intent.setAction(Intent.ACTION_SCREEN_ON);
        //content.startActivity(intent);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, MainActivity.TAG);
        //wl.acquire();
        //..screen will stay on during this section..
        //wl.release();
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MainActivity.TAG);
        wl.acquire();
        wl.release();
    }
}
