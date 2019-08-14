package com.lunaprinter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;
import com.lunaprinter.bluetooth.RNBluetoothManagerModule;
import com.lunaprinter.bluetooth.escpos.RNBluetoothEscposPrinterModule;
import com.lunaprinter.bluetooth.BluetoothService;
import com.lunaprinter.usb.UsbPrinterModule;
import com.lunaprinter.network.NetworkPrinterModule;

public class LunaPrinterPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        BluetoothService service = new BluetoothService(reactContext);
        return Arrays.<NativeModule>asList(
            new UsbPrinterModule(reactContext),
            new RNBluetoothManagerModule(reactContext, service),
            new RNBluetoothEscposPrinterModule(reactContext, service),
            new NetworkPrinterModule(reactContext)
            );
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
