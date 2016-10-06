package com.logicpulse.logicpulsecustomprinter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class Utils {

    //Used in CustomPrinterInterface
    public static void showAlertMsg(Context context, String title, String msg)
    {
        AlertDialog.Builder dialogBuilder;
        dialogBuilder = new AlertDialog.Builder(context);

        dialogBuilder.setNeutralButton( "OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(msg);
        dialogBuilder.show();
    }

    //Used in CustomPrinterInterface
    public static String intToHexString(int iValue, int num0chars)
    {
        String hexString;

        //convert to Hex
        hexString = Integer.toHexString(iValue);
        int num0towrite = num0chars - hexString.length();
        //If i need to add some "0" before
        if (num0towrite > 0)
        {
            for (int i=0;i<num0towrite;i++)
                hexString = "0" + hexString;
        }

        //Change to upper case
        hexString = hexString.toUpperCase();

        return hexString;
    }
}
