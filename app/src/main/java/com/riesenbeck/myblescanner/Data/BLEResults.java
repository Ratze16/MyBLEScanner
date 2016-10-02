package com.riesenbeck.myblescanner.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Riesenbeck on 20.09.2016.
 */
public class BLEResults {
    private static BLEResults bleResultsRef = null;
    private List<BleDevice> bleDevices = new ArrayList<BleDevice>();
    private BLEResults(){
        //bleDevices = new ArrayList<BleDevice>();
    }
    public static BLEResults getInstance(){
        if (bleResultsRef==null){
            bleResultsRef = new BLEResults();
        }
        return bleResultsRef;
    }
    public void addBLEResult(BleDevice bleDevice){
        bleDevices.add(bleDevice);
    }
    public BleDevice getBleDeviceResult(int i){
        return bleDevices.get(i);
    }
    public List<BleDevice> getBleDevices(){
        return bleDevices;
    }
    public void clearBLEResults(){
        bleDevices.clear();
    }


}
