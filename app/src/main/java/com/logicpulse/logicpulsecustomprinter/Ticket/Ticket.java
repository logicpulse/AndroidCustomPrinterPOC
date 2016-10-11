package com.logicpulse.logicpulsecustomprinter.Ticket;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.logicpulse.logicpulsecustomprinter.CustomPrinterInterface;
import com.logicpulse.logicpulsecustomprinter.MainActivity;
import com.logicpulse.logicpulsecustomprinter.R;
import com.logicpulse.logicpulsecustomprinter.Utils;
import com.solidfire.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.PrinterFont;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class Ticket {

    private Context mContext;
    private TicketTemplate mTicketTemplate = null;
    private CustomPrinterInterface mPrinter;

    public Ticket(Context context, CustomPrinterInterface printer) {

        this.mContext = context;
        this.mPrinter = printer;

        InputStream inputStream = Utils.getInputStreamFromRawResource(context, R.raw.template_ticket);

        String  templateString = null;

        try {
            templateString = getStringFromInputStream(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            try {
                Gson gson = new Gson();
                mTicketTemplate = gson.fromJson(templateString, TicketTemplate.class);
            } catch (Throwable tx) {
                //Show Error
                String errorMessage = String.format("Error: Malformed JSON: %s\r\n\r\n%s", tx.getMessage(), templateString);
                Utils.showAlert((Activity) context, errorMessage);
                Log.e(MainActivity.TAG, errorMessage);
            }
        } catch (Exception e) {
            //Show Error
            e.printStackTrace();
            String errorMessage = String.format("Error: Malformed JSON: %s", templateString);
            Utils.showAlert((Activity) context, errorMessage);
            Log.e(MainActivity.TAG, errorMessage);
        }
    }

    public boolean print(int copies) throws CustomException {

        //open device
        if (mPrinter.openDevice() == false) return false;

        boolean result = false;
        TicketTemplateNode node;

        if (mTicketTemplate != null) {

            for (int i = 0; i < copies; i++) {
                Log.d(MainActivity.TAG, String.format("print copy: %d", i));

                for (Object property : mTicketTemplate.getProperties())
                {
                    node = (TicketTemplateNode) property;
                    Log.d(MainActivity.TAG, String.format("print: %s", node.getType()));

                    switch (node.getType().toLowerCase()) {
                        case "text":
                            PrinterFont printerFont = new PrinterFont();
                            printerFont.setCharFontType(node.getCharfont());
                            printerFont.setCharHeight(node.getCharheight());
                            printerFont.setCharWidth(node.getCharheight());
                            printerFont.setEmphasized(node.getEmphasized());
                            printerFont.setItalic(node.getItalic());
                            printerFont.setUnderline(node.getUnderline());
                            printerFont.setJustification(node.getJustification());
                            printerFont.setInternationalCharSet(node.getCharset());
                            mPrinter.printText(node.getValue(), printerFont, node.getFeeds());
                            break;
                        case "textimage":
                            break;
                        case "image":
                            InputStream inputStream = Utils.getInputStreamFromRawResource(mContext, R.raw.image);
                            mPrinter.printImage(inputStream, node.getAlign(), node.getScaletofit(), node.getWidth(), node.getFeeds());
                            break;
                        case "cut":
                            mPrinter.cut(node.getCutmode(), node.getFeeds());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return result;
    }

    private String getStringFromInputStream(InputStream stream, String charsetName) throws IOException
    {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, charsetName);
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    //Android - How to draw text on a bitmap
    //https://www.skoumal.net/en/android-how-draw-text-bitmap/
public static Bitmap drawTextToBitmap(Context context, int resid, String gText) {

        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        //Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
        Bitmap bitmap = Bitmap.createBitmap(400, 200, android.graphics.Bitmap.Config.ARGB_8888);

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);

        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
//paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}
