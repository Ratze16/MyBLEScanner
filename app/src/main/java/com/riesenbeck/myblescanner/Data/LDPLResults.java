package com.riesenbeck.myblescanner.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Riesenbeck on 27.09.2016.
 */
public class LDPLResults {
    private static LDPLResults ldplResultsRef = null;
    private List<BLEDeviceResult[]> ldplResults = null;
    private LDPLResults(){
        ldplResults = new ArrayList<BLEDeviceResult[]>();
    }
    public static LDPLResults getInstance(){
        if (ldplResultsRef==null){
            ldplResultsRef = new LDPLResults();
        }
        return ldplResultsRef;
    }
    public void addLDPLMeasurementResult(BLEDeviceResult[] LDPLMeasurementResults){
        ldplResults.add(LDPLMeasurementResults);
    }
    public BLEDeviceResult[] getBleDeviceResult(int i){
        return ldplResults.get(i);
    }
    public List<BLEDeviceResult[]> getBleDeviceResults(){
        return ldplResults;
    }
    public void clearBLEResults(){
        ldplResults.clear();
    }
}
