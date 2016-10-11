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
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
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

        String templateString = null;

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

                for (Object property : mTicketTemplate.getProperties()) {

                    node = (TicketTemplateNode) property;

                    //Check Enabled State
                    if (node.getEnabled()) {

                        Log.d(MainActivity.TAG, String.format("print: %s", node.getType()));

                        //Shared : Overrides (node Align with custom values)
                        Integer align = -1;
                        Paint.Align alignPaint = Paint.Align.CENTER;
                        switch (node.getAlign()) {
                            case -1://left
                                align = -0;
                                alignPaint = Paint.Align.LEFT;
                                break;
                            case 0://center
                                align = -1;
                                alignPaint = Paint.Align.CENTER;
                                break;
                            case 1://right
                                align = -2;
                                alignPaint = Paint.Align.RIGHT;
                                break;
                            default:
                                align = -1;
                                alignPaint = Paint.Align.CENTER;
                                break;
                        }

                        switch (node.getType().toLowerCase()) {

                            case "text":
                                PrinterFont printerFont = new PrinterFont();
                                printerFont.setCharFontType(node.getCharfont());
                                printerFont.setCharHeight(node.getCharheight());
                                printerFont.setCharWidth(node.getCharwidth());
                                printerFont.setEmphasized(node.getEmphasized());
                                printerFont.setItalic(node.getItalic());
                                printerFont.setUnderline(node.getUnderline());
                                printerFont.setJustification(node.getJustification());
                                printerFont.setInternationalCharSet(node.getCharset());
                                mPrinter.printText(node.getValue(), printerFont, node.getFeeds());
                                break;

                            case "image":
                                InputStream inputStream = Utils.getInputStreamFromRawResource(mContext, R.raw.image);
                                Bitmap bitmapImage = BitmapFactory.decodeStream(inputStream);
                                mPrinter.printImage(bitmapImage, align, node.getScaletofit(), node.getWidth(), node.getFeeds());
                                break;

                            case "imagetext":
                                Typeface typeface;
                                switch (node.getTypeface().toLowerCase()) {
                                    case "default":
                                        typeface = Typeface.DEFAULT;
                                        break;
                                    case "default_bold":
                                        typeface = Typeface.DEFAULT_BOLD;
                                        break;
                                    case "monospace":
                                        typeface = Typeface.MONOSPACE;
                                        break;
                                    case "sans_serif":
                                        typeface = Typeface.SANS_SERIF;
                                        break;
                                    case "serif":
                                        typeface = Typeface.SERIF;
                                        break;
                                    default:
                                        typeface = Typeface.DEFAULT;
                                        break;
                                }
                                Bitmap bitmapImageText = Ticket.drawTextToBitmap(mContext, "A01", node.getWidth(), node.getHeight(), typeface, node.getTextSize(), alignPaint, node.getShowbackground());
                                mPrinter.printImage(bitmapImageText, align, node.getScaletofit(), node.getWidth(), node.getFeeds());
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
        }

        return result;
    }

    private String getStringFromInputStream(InputStream stream, String charsetName) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, charsetName);
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    //Android - How to draw text on a bitmap
    //https://www.skoumal.net/en/android-how-draw-text-bitmap/
    //https://www.skoumal.net/en/android-drawing-multiline-text-on-bitmap/
    public static Bitmap drawTextToBitmap(Context context, String gText, int width, int height, Typeface typeface, int textSize, Paint.Align align,  boolean showBackground) {

        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);

        //Show Background
        int colorBackground = (showBackground) ? Color.rgb(200, 200, 200) : Color.WHITE;
        canvas.drawColor(colorBackground);

        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //text align : Not used, used align in bottom with coords
        //paint.setTextAlign(align);
        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text typeface
        paint.setTypeface(typeface);
        // text size in pixels
        paint.setTextSize((int) (textSize * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.GRAY);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);

        int x = 0;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        switch(align) {
            case LEFT:
                x = 0;
                break;
            case CENTER:
                x = (bitmap.getWidth() - bounds.width()) / 2;
                break;
            case RIGHT:
                x = (bitmap.getWidth() - bounds.width());
                break;
        default:
            break;
        }

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}
