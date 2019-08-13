package com.lunaprinter.USBAdapter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;


import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.lunaprinter.command.PrintPicture;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class USBAdapter {
    private static USBAdapter mInstance;


    private String LOG_TAG = "RNUSBPrinter";
    private Context mContext;
    private UsbManager mUSBManager;
    private PendingIntent mPermissionIndent;
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mEndPoint;
    private static final String ACTION_USB_PERMISSION = "com.lunaprinter.USBPrinter.USB_PERMISSION";

    public static final int WIDTH_58 = 384;
    private int deviceWidth = WIDTH_58;


    private USBAdapter() {
    }

    public static USBAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new USBAdapter();
        }
        return mInstance;
    }

    private final BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.i(LOG_TAG, "success to grant permission for device " + usbDevice.getDeviceId() + ", vendor_id: " + usbDevice.getVendorId() + " product_id: " + usbDevice.getProductId());
                        mUsbDevice = usbDevice;
                    } else {
                        Toast.makeText(context, "用户拒绝获取USB设备权限" + usbDevice.getDeviceName(), Toast.LENGTH_LONG).show();
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (mUsbDevice != null) {
                    Toast.makeText(context, "USB设备已经被关闭", Toast.LENGTH_LONG).show();
                    closeConnectionIfExists();
                }
            }
        }
    };

    public void init(Context reactContext) {
        this.mContext = reactContext;
        this.mUSBManager = (UsbManager) this.mContext.getSystemService(Context.USB_SERVICE);
        this.mPermissionIndent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbDeviceReceiver, filter);
        Log.v(LOG_TAG, "RNUSBPrinter initialized");
    }


    public void closeConnectionIfExists() {
        if (mUsbDeviceConnection != null) {
            mUsbDeviceConnection.releaseInterface(mUsbInterface);
            mUsbDeviceConnection.close();
            mUsbInterface = null;
            mEndPoint = null;
            mUsbDeviceConnection = null;
        }
    }

    public List<UsbDevice> getDeviceList() {
        if (mUSBManager == null) {
            Toast.makeText(mContext, "USBManager is not initialized while get device list", Toast.LENGTH_LONG).show();
            return Collections.emptyList();
        }
        return new ArrayList<UsbDevice>(mUSBManager.getDeviceList().values());
    }

    public boolean selectDevice(Integer vendorId, Integer productId) {

        if (mUsbDevice == null || mUsbDevice.getVendorId() != vendorId || mUsbDevice.getProductId() != productId) {
            closeConnectionIfExists();
            List<UsbDevice> usbDevices = getDeviceList();
            for (UsbDevice usbDevice : usbDevices) {
                if ((usbDevice.getVendorId() == vendorId) && (usbDevice.getProductId() == productId)) {
                    Log.v(LOG_TAG, "request for device: vendor_id: " + usbDevice.getVendorId() + ", product_id: " + usbDevice.getProductId());
                    closeConnectionIfExists();
                    mUSBManager.requestPermission(usbDevice, mPermissionIndent);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean openConnection() {
        if (mUsbDevice == null) {
            Log.e(LOG_TAG, "USB Deivce is not initialized");
            return false;
        }
        if (mUSBManager == null) {
            Log.e(LOG_TAG, "USB Manager is not initialized");
            return false;
        }

        if (mUsbDeviceConnection != null) {
            Log.i(LOG_TAG, "USB Connection already connected");
            return true;
        }

        UsbInterface usbInterface = mUsbDevice.getInterface(0);
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            final UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    UsbDeviceConnection usbDeviceConnection = mUSBManager.openDevice(mUsbDevice);
                    if (usbDeviceConnection == null) {
                        Log.e(LOG_TAG, "failed to open USB Connection");
                        return false;
                    }
                    Toast.makeText(mContext, "Device connected", Toast.LENGTH_SHORT).show();
                    if (usbDeviceConnection.claimInterface(usbInterface, true)) {
                        mEndPoint = ep;
                        mUsbInterface = usbInterface;
                        mUsbDeviceConnection = usbDeviceConnection;
                        return true;
                    } else {
                        usbDeviceConnection.close();
                        Log.e(LOG_TAG, "failed to claim usb connection");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean printText(String text) {
        final String printData = text;
        Log.v(LOG_TAG, "start to print text");
        boolean isConnected = openConnection();
        if (isConnected) {
            Log.v(LOG_TAG, "Connected to device");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bytes = printData.getBytes(Charset.forName("UTF-8"));
                    int b = mUsbDeviceConnection.bulkTransfer(mEndPoint, bytes, bytes.length, 100000);
                    Log.i(LOG_TAG, "Return Status: b-->" + b);
                }
            }).start();
            return true;
        } else {
            Log.v(LOG_TAG, "failed to connected to device");
            return false;
        }
    }

    public boolean printRawData(String data) {
        final String rawData = data;
        Log.v(LOG_TAG, "start to print raw data " + data);
        boolean isConnected = openConnection();
        if (isConnected) {
            Log.v(LOG_TAG, "Connected to device");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bytes = Base64.decode(rawData, Base64.DEFAULT);
                    int b = mUsbDeviceConnection.bulkTransfer(mEndPoint, bytes, bytes.length, 100000);
                    Log.i(LOG_TAG, "Return Status: b-->" + b);
                }
            }).start();
            return true;
        } else {
            Log.v(LOG_TAG, "failed to connected to device");
            return false;
        }
    }

    public boolean printPic(final String base64encodeStr, @Nullable final ReadableMap options) {
        final String rawData = base64encodeStr;
        Log.v(LOG_TAG, "start to print raw data " + base64encodeStr);
        boolean isConnected = openConnection();
        if (isConnected) {
            Log.v(LOG_TAG, "Connected to device");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int width = 0;
                    int leftPadding = 0;
                    if (options != null) {
                        width = options.hasKey("width") ? options.getInt("width") : 0;
                        leftPadding = options.hasKey("left") ? options.getInt("left") : 0;
                    }

                    //cannot larger then devicesWith;
                    if (width > deviceWidth || width == 0) {
                        width = deviceWidth;
                    }

                    byte[] bytes = Base64.decode(rawData, Base64.DEFAULT);
                    Bitmap mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    int nMode = 0;
                    if (mBitmap != null) {
                        /**
                         * Parameters:
                         * mBitmap  要打印的图片
                         * nWidth   打印宽度（58和80）
                         * nMode    打印模式
                         * Returns: byte[]
                         */
//                         PrintPicture.resizeImage(mBitmap, 200, 200);
                        byte[] data = PrintPicture.POS_PrintBMP(mBitmap, width, nMode, leftPadding);
                        int b = mUsbDeviceConnection.bulkTransfer(mEndPoint, data, data.length, 100000);
                        Log.i(LOG_TAG, "Return Status: b-->" + b);
                    }
                }
            }).start();
            return true;
        } else {
            Log.v(LOG_TAG, "failed to connected to device");
            return false;
        }
    }

}