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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

                        //Shared Value
                        String value = replaceTextTokens(node.getValue());

                        //Shared : Overrides (node Align with custom values)
                        Integer alignText = 0;
                        Integer alignImage = -1;
                        Paint.Align alignPaint = Paint.Align.CENTER;
                        Integer alignBarCode = -1;

                        switch (node.getAlign()) {
                            case -1://left
                                alignText = 0;
                                alignImage = 0;
                                alignPaint = Paint.Align.LEFT;
                                alignBarCode = 0;
                                break;
                            case 0://center
                                alignText = 1;
                                alignImage = -1;
                                alignPaint = Paint.Align.CENTER;
                                alignBarCode = -1;
                                break;
                            case 1://right
                                alignText = 2;
                                alignImage = -2;
                                alignPaint = Paint.Align.RIGHT;
                                alignBarCode = -2;
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
                                printerFont.setInternationalCharSet(node.getCharset());
                                printerFont.setJustification(alignText);
                                mPrinter.printText(value, printerFont, node.getFeeds());
                                break;

                            case "image":
                                InputStream inputStream = Utils.getInputStreamFromRawResource(mContext, R.raw.image);
                                Bitmap bitmapImage = BitmapFactory.decodeStream(inputStream);
                                mPrinter.printImage(bitmapImage, alignImage, node.getScaletofit(), node.getWidth(), node.getFeeds());
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
                                Bitmap bitmapImageText = Ticket.drawTextToBitmap(mContext, value, node.getWidth(), node.getHeight(), typeface, node.getTextSize(), alignPaint, node.getShowbackground());
                                mPrinter.printImage(bitmapImageText, alignImage, node.getScaletofit(), node.getWidth(), node.getFeeds());
                                break;

                            case "barcode":
                                Integer barcodetype = -4;
                                Integer barcodehritype = -0;

                                switch (node.getBarcodetype().toLowerCase()) {
                                    case "barcode_type_codabar":
                                        barcodetype = -1;
                                        break;
                                    case "barcode_type_upca":
                                        barcodetype = -2;
                                        break;
                                    case "barcode_type_upce":
                                        barcodetype = -3;
                                        break;
                                    case "barcode_type_ean13":
                                        barcodetype = -4;
                                        break;
                                    case "barcode_type_ean8":
                                        barcodetype = -5;
                                        break;
                                    case "barcode_type_code39":
                                        barcodetype = -6;
                                        break;
                                    case "barcode_type_itf":
                                        barcodetype = -7;
                                        break;
                                    case "barcode_type_code93":
                                        barcodetype = -8;
                                        break;
                                    case "barcode_type_code128":
                                        barcodetype = -9;
                                        break;
                                    case "barcode_type_code32":
                                        barcodetype = -10;
                                        break;
                                }

                                switch (node.getBarcodehritype().toLowerCase()) {
                                    case "barcode_hri_none":
                                        barcodehritype = 0;
                                        break;
                                    case "barcode_hri_top":
                                        barcodehritype = 1;
                                        break;
                                    case "barcode_hri_bottom":
                                        barcodehritype = 2;
                                        break;
                                    case "barcode_hri_topbottom":
                                        barcodehritype = 3;
                                        break;
                                }
                                mPrinter.printBarCode(node.getValue(), barcodetype, barcodehritype, alignBarCode, node.getBarCodeWidth(), node.getHeight(), node.getFeeds());
                                break;

                            case "barcode2d":
                                Integer barcode2dtype = -101;

                                switch (node.getBarcode2dtype().toLowerCase()) {
                                    case "barcode_type_qrcode":
                                        barcode2dtype = -101;
                                        break;
                                    case "barcode_type_pdf417":
                                        barcode2dtype = -102;
                                        break;
                                    case "barcode_type_datamatrix":
                                        barcode2dtype = -103;
                                        break;
                                    case "barcode_type_aztec":
                                        barcode2dtype = -104;
                                        break;
                                }
                                mPrinter.printBarCode2D(node.getValue(), barcode2dtype, alignBarCode, node.getWidth(), node.getFeeds());
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
    public static Bitmap drawTextToBitmap(Context context, String gText, int width, int height, Typeface typeface, int textSize, Paint.Align align, boolean showBackground) {

        Resources resources = context.getResources();
        //not used
        //float scale = resources.getDisplayMetrics().density;
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
        paint.setTextSize((int) (textSize /* * scale*/));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.GRAY);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);

        int x = 0;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        switch (align) {
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

    //http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#dt
    private String replaceTextTokens(String input) {

        String result = input;

        if (input != null) {

            Date currentDate = new Date();
            String replaceToken;
            Resources resources = mContext.getResources();

            List<String> tokens = new ArrayList();
            tokens.add("app_company");
            tokens.add("app_name");
            tokens.add("app_version");
            tokens.add("service_name");
            tokens.add("service_ticket_no");
            tokens.add("datetime");
            tokens.add("date");
            tokens.add("time");
            tokens.add("image1");
            tokens.add("image2");
            tokens.add("image3");
            tokens.add("image4");
            tokens.add("image5");
            tokens.add("text1");
            tokens.add("text2");
            tokens.add("text3");
            tokens.add("text4");
            tokens.add("text5");
            tokens.add("barcode");
            tokens.add("qrcode");

            for (String token : tokens) {
                Log.d(MainActivity.TAG, token);

                replaceToken = String.format("${%s}", token);

                if (result.contains(replaceToken)) {
                    switch (token) {
                        case "app_company":
                            result = result.replace(replaceToken, resources.getString(R.string.app_company));
                            break;
                        case "app_name":
                            result = result.replace(replaceToken, resources.getString(R.string.app_name));
                            break;
                        case "app_version":
                            result = result.replace(replaceToken, resources.getString(R.string.app_version));
                            break;
                        case "service_name":
                            result = result.replace(replaceToken, "SERVICE_NAME");
                            break;
                        case "service_ticket_no":
                            result = result.replace(replaceToken, "SERVICE_TICKET_NO");
                            break;
                        case "datetime":
                            result = result.replace(replaceToken, Utils.formatDate(currentDate, "yyyy-MM-dd HH:mm:ss"));
                            break;
                        case "date":
                            result = result.replace(replaceToken, Utils.formatDate(currentDate, "yyyy-MM-dd"));
                            break;
                        case "time":
                            result = result.replace(replaceToken, Utils.formatDate(currentDate, "HH:mm:ss"));
                            break;
                        case "image1":
                            result = result.replace(replaceToken, "IMAGE1");
                            break;
                        case "image2":
                            result = result.replace(replaceToken, "IMAGE2");
                            break;
                        case "image3":
                            result = result.replace(replaceToken, "IMAGE3");
                            break;
                        case "image4":
                            result = result.replace(replaceToken, "IMAGE4");
                            break;
                        case "image5":
                            result = result.replace(replaceToken, "IMAGE5");
                            break;
                        case "text1":
                            result = result.replace(replaceToken, "TEXT1");
                            break;
                        case "text2":
                            result = result.replace(replaceToken, "TEXT2");
                            break;
                        case "text3":
                            result = result.replace(replaceToken, "TEXT3");
                            break;
                        case "text4":
                            result = result.replace(replaceToken, "TEXT4");
                            break;
                        case "text5":
                            result = result.replace(replaceToken, "TEXT5");
                            break;
                        case "barcode":
                            result = result.replace(replaceToken, "BARCODE");
                            break;
                        case "qrcode":
                            result = result.replace(replaceToken, "QRCODE");
                            break;
                    }
                }
            }
        }

        return result;
    }
}
