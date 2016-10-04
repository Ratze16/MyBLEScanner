package com.riesenbeck.myblescanner.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Riesenbeck on 27.09.2016.
 */
public class LDPLResults {
    private static LDPLResults ldplResultsRef = null;
    private List<BleDevice[]> ldplResults = null;
    private LDPLResults(){
        ldplResults = new ArrayList<BleDevice[]>();
    }
    public static LDPLResults getInstance(){
        if (ldplResultsRef==null){
            ldplResultsRef = new LDPLResults();
        }
        return ldplResultsRef;
    }
    public void addLDPLMeasurementResult(BleDevice[] LDPLMeasurementResults){
        ldplResults.add(LDPLMeasurementResults);
    }
    public BleDevice[] getBleDeviceResult(int i){
        return ldplResults.get(i);
    }
    public List<BleDevice[]> getBleDeviceResults(){
        return ldplResults;
    }
    public void clearBLEResults(){
        ldplResults.clear();
    }
}
