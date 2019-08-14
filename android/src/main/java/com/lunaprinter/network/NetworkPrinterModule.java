package com.lunaprinter.network;

import android.hardware.usb.UsbDevice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.lunaprinter.usb.adapter.USBAdapter;
import java.util.List;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintWriter;
import com.lunaprinter.bluetooth.escpos.command.sdk.PrinterCommand;
import com.lunaprinter.bluetooth.escpos.command.sdk.PrintPicture;
import javax.annotation.Nullable;

public class NetworkPrinterModule extends ReactContextBaseJavaModule {
    private USBAdapter usbAdapter;

    private final ReactApplicationContext reactContext;

    public NetworkPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "LunaNetworkPrinter";
    }

    @ReactMethod
    public void printText(String ip, Int port, String text, Boolean openCD, Promise promise) {
        try {
            Socket sock = new Socket(ip, port);
            PrintWriter oStream = new PrintWriter(sock.getOutputStream());
            oStream.println(text);
            if (openCD) {
                sock.getOutputStream().write(PrinterCommand.POS_Set_Cashbox(0, 25, 250));
            }
            oStream.close();
            sock.close();
            promise.resolve(true);
        } catch (Exception e) {
            // TODO: handle exception
            promise.resolve(e.getMessage());
        }
    }

    @ReactMethod
    public void print(String ip, Int port, String[] lines, String base64logo, Boolean openCD, Promise promise) {
        try {
            Socket sock = new Socket(ip, port);
            OutputStream outputStream = sock.getOutputStream();
            PrintWriter oStream = new PrintWriter(outputStream);
            // Logo
            if (base64logo) {
                byte[] bufferImage = this.picBuffer(base64logo, 200, 90);
                outputStream.write(bufferImage);
            }
            // Lines
            for (int i = 0; i < lines.length; i++) {
                oStream.println(lines[i]);
            }
            // Cashdrawer
            if (openCD) {
                sock.getOutputStream().write(PrinterCommand.POS_Set_Cashbox(0, 25, 250));
            }
            oStream.close();
            sock.close();
            promise.resolve(true);
        } catch (Exception e) {
            // TODO: handle exception
            promise.resolve(e.getMessage());
        }
    }

    private byte[] picBuffer(String base64encodeStr, @Nullable Integer optWidth, @Nullable Integer optLeft) {
        int width = 0;
        int leftPadding = 0;
        if (options != null) {
            width = optWidth ? optWidth : 0;
            leftPadding = optLeft ? optLeft : 0;
        }

        // cannot larger then devicesWith;
        if (width > deviceWidth || width == 0) {
            width = deviceWidth;
        }

        byte[] bytes = Base64.decode(base64encodeStr, Base64.DEFAULT);
        Bitmap mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int nMode = 0;
        if (mBitmap != null) {

            byte[] data = PrintPicture.POS_PrintBMP(mBitmap, width, nMode, leftPadding);

            return data;
        }
        return null;
    }
}
