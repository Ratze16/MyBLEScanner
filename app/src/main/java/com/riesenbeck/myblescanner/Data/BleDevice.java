package com.riesenbeck.myblescanner.Data;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

/**
 * Created by Michael Riesenbeck on 20.09.2016.
 */
//A BLE Device
public class BleDevice {
    private BluetoothDevice mBluetoothDevice;
    private String mAddress, mName;
    private BluetoothClass mBluetoothClass;
    private ParcelUuid[] mUuids;
    private int mBondState, mType, mRssi;
    private byte[] mScanRecord;
    private long mTimestampNanos;

    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord, long timestampNanos){
        this.mBluetoothDevice = bluetoothDevice;
        this.mAddress = bluetoothDevice.getAddress();
        this.mBluetoothClass = bluetoothDevice.getBluetoothClass();
        this.mBondState = bluetoothDevice.getBondState();
        this.mName = bluetoothDevice.getName();
        this.mType = bluetoothDevice.getType();
        this.mUuids = bluetoothDevice.getUuids();
        this.mRssi = rssi;
        this.mScanRecord = scanRecord;
        this.mTimestampNanos = timestampNanos;
        //this.distance = 0.42093*Math.pow(rssi*1.0/-60,6.9476)+0.54992;
    }
    @Override
    public String toString(){
        return "Device: "+mBluetoothDevice.toString()+ "\nRSSI: "+String.valueOf(mRssi)+"\nTimestamp: "+mTimestampNanos+"\nDistance: "+Room.getInstance().getKlQuadrateDistance(mRssi,-65)+"\nDistance Exp: "+Room.getInstance().getExponential(mRssi,-65);
    }

    public BluetoothDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    public String getmAddress() {
        return mAddress;
    }

    public String getmName() {
        return mName;
    }

    public BluetoothClass getmBluetoothClass() {
        return mBluetoothClass;
    }

    public ParcelUuid[] getmUuids() {
        return mUuids;
    }

    public int getmBondState() {
        return mBondState;
    }

    public int getmType() {
        return mType;
    }

    public int getmRssi() {
        return mRssi;
    }

    public byte[] getmScanRecord() {
        return mScanRecord;
    }

    public long getmTimestampNanos() {
        return mTimestampNanos;
    }


}