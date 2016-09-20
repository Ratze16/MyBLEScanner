package com.riesenbeck.myblescanner.Data;

import android.bluetooth.BluetoothDevice;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Michael Riesenbeck on 20.09.2016.
 */
//A BLE Device
public class BLEDeviceResult{
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private byte[] scanRecord;
    private long timestampNanos;
    public BLEDeviceResult(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord, long timestampNanos){
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.timestampNanos = timestampNanos;
    }
    @Override
    public String toString(){
        return "Device: "+bluetoothDevice.toString()+ "\nRSSI: "+String.valueOf(rssi)+"\nTimestamp: "+timestampNanos;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

}