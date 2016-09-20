package com.riesenbeck.myblescanner.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Riesenbeck on 20.09.2016.
 */
public class BLEResults {
    private static BLEResults bleResultsRef = null;
    private List<BLEDeviceResult> bleDeviceResults = null;
    private BLEResults(){
        bleDeviceResults = new ArrayList<BLEDeviceResult>();
    }
    public static BLEResults getInstance(){
        if (bleResultsRef==null){
            bleResultsRef = new BLEResults();
        }
        return bleResultsRef;
    }
    public void addBLEResult(BLEDeviceResult bleDeviceResult){
        bleDeviceResults.add(bleDeviceResult);
    }
    public BLEDeviceResult getBleDeviceResult(int i){
        return bleDeviceResults.get(i);
    }
    public List<BLEDeviceResult> getBleDeviceResults(){
        return bleDeviceResults;
    }
    public void clearBLEResults(){
        bleDeviceResults.clear();
    }
}
