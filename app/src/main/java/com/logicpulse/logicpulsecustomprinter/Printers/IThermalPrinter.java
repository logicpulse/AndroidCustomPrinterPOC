package com.logicpulse.logicpulsecustomprinter.Printers;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.media.Ringtone;
import android.view.View;

import java.io.InputStream;

/**
 * Created by mario.monteiro on 12/10/2016.
 */

public interface IThermalPrinter {
    void init(Context context, UsbDevice usbDevice, View view, Ringtone ringtone);
    //boolean openDevice();
    void printText(String text, Object printerFont, Integer feeds);
    void printImage(Bitmap bitmap, Integer align, Integer scaletofit, Integer width, Integer feeds);
    void printBarCode(String text, Integer barcodetype, Integer barcodehritype, Integer align, Integer barcodewidth, Integer height, Integer feeds);
    void printBarCode2D(String text, Integer barcodetype, Integer align, Integer width, Integer feeds);
    void cut(Integer cutMode, Integer feeds);
    void testPrintText(String text);
    void testPrintImage(InputStream inputStream);
    void close() throws Throwable;
}
