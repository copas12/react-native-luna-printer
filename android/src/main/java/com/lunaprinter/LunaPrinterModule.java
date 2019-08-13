package com.lunaprinter;

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
import com.lunaprinter.adapter.USBAdapter;
import java.util.List;

public class LunaPrinterModule extends ReactContextBaseJavaModule {
    private USBAdapter usbAdapter;

    private final ReactApplicationContext reactContext;

    public LunaPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.usbAdapter = USBAdapter.getInstance();
        this.usbAdapter.init(reactContext);
    }

    @Override
    public String getName() {
        return "LunaPrinter";
    }

    @ReactMethod
    public void usbPrinterList(Promise promise) {
        List<UsbDevice> usbDevices = usbAdapter.getDeviceList();
        WritableArray pairedDeviceList = Arguments.createArray();
        for (UsbDevice usbDevice : usbDevices) {
            WritableMap deviceMap = Arguments.createMap();
            deviceMap.putString("device_name", usbDevice.getDeviceName());
            deviceMap.putInt("device_id", usbDevice.getDeviceId());
            deviceMap.putInt("vendor_id", usbDevice.getVendorId());
            deviceMap.putInt("product_id", usbDevice.getProductId());
            pairedDeviceList.pushMap(deviceMap);
        }
        promise.resolve(pairedDeviceList);
    }

    @ReactMethod
    public void usbPrinterConnect(Integer vendorId, Integer productId, Promise promise) {
        if (!usbAdapter.selectDevice(vendorId, productId)) {
            promise.resolve(false);
        } else {
            promise.resolve(true);
        }
    }

    @ReactMethod
    public void usbPrintRawData(String base64Data, Promise promise) {
        boolean result = usbAdapter.printRawData(base64Data);
        promise.resolve(result);
    }

    @ReactMethod
    public void usbPrintPicture(String base64Data, ReadableMap options, Promise promise) {
        boolean result = usbAdapter.printPic(base64Data, options);
        promise.resolve(result);
    }

    @ReactMethod
    public void usbCloseConnection(Promise promise) {
        usbAdapter.closeConnectionIfExists();
        promise.resolve(null);
    }

    
}
