package com.lunaprinter.network;

import android.hardware.usb.UsbDevice;

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
import java.io.PrintWriter;
import com.lunaprinter.bluetooth.escpos.command.sdk.PrinterCommand;

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
    public void printText(String text, Promise promise) {
        try {
            Socket sock = new Socket("172.16.0.87", 9100);
            PrintWriter oStream = new PrintWriter(sock.getOutputStream());
            oStream.println(text);
            oStream.close();
            sock.close();
            promise.resolve(true);
        } catch (Exception e) {
            //TODO: handle exception
            promise.resolve(e.getMessage());
        }
    }

    @ReactMethod
    public void printRaw(char[] buffer, Boolean openCD, Promise promise) {
        try {
            Socket sock = new Socket("172.16.0.87", 9100);
            PrintWriter oStream = new PrintWriter(sock.getOutputStream());
            oStream.write(buffer);
            if (openCD) {
                oStream.write(PrinterCommand.POS_Set_Cashbox(0, 25, 250));
            }
            oStream.close();
            sock.close();
            promise.resolve(true);
        } catch (Exception e) {
            //TODO: handle exception
            promise.resolve(e.getMessage());
        }
    }

}
