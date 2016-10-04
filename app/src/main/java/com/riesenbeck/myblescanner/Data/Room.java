package com.riesenbeck.myblescanner.Data;

/**
 * Created by Michael Riesenbeck on 04.10.2016.
 */

public class Room {
    private static Room mRoomReference = null;
    private double[] q;
    private double[] c2;
    private double[] c3;
    private Room(){
        //TODO set q,c2,c3 dynamically by query/database
        q = new double[3];
        q[0] = 0.3818027;
        q[1] = 0.361573;
        q[2] = 0.4758124;
    }
    public static Room getInstance(){
        if(mRoomReference == null){
            mRoomReference = new Room();
        }
        return mRoomReference;
    }
    public double getKlQuadrateDistance(int rssi, int txPower){
        double d = q[2]*Math.pow((1.0*rssi)/txPower,q[1])+q[0];
        return d;
    }
    public double getExponential(int rssi, int txPower){
        double d = 0.00867745*Math.pow(Math.E,4.76147*((1.0*rssi)/txPower));
        return d;
    }
}
