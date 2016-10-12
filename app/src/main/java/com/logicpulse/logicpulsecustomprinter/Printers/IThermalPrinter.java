package com.logicpulse.logicpulsecustomprinter.Printers;

import android.graphics.Bitmap;

import java.io.InputStream;

/**
 * Created by mario.monteiro on 12/10/2016.
 */

public interface IThermalPrinter {
    void Init();
    boolean openDevice();
    void printText(String text, Object printerFont, Integer feeds);
    void printImage(Bitmap bitmap, Integer align, Integer scaletofit, Integer width, Integer feeds);
    void printBarCode(String text, Integer barcodetype, Integer barcodehritype, Integer align, Integer barcodewidth, Integer height, Integer feeds);
    void printBarCode2D(String text, Integer barcodetype, Integer align, Integer width, Integer feeds);
    void cut(Integer cutMode, Integer feeds);
    void testPrintText(String text);
    void testPrintImage(InputStream inputStream);
    void destroy() throws Throwable;
}
