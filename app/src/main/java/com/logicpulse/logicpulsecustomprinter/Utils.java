package com.logicpulse.logicpulsecustomprinter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class Utils {

    //Used in CustomPrinterInterface
    public static void showAlertMsg(Context context, String title, String msg) {
        AlertDialog.Builder dialogBuilder;
        dialogBuilder = new AlertDialog.Builder(context);

        dialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(msg);
        dialogBuilder.show();
    }

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
}
