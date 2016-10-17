package com.riesenbeck.myblescanner.Data;

/**
 * Created by Michael Riesenbeck on 17.10.2016.
 */

public class Beacon {
    private int id;

    public int getId() {
        return id;
    }

    public int getRoom_id() {
        return room_id;
    }

    public int getRssi() {
        return rssi;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getHeight() {
        return height;
    }

    public String getAddresse() {
        return addresse;
    }

    private int room_id;
    private int rssi;
    private double longitude, latitude, height;
    private String addresse;
    public Beacon(int id, int room_id, double longitude, double latitude, double height, String addresse, int rssi){
        this.id = id;
        this.room_id = room_id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.height = height;
        this.addresse = addresse;
        this.rssi = rssi;
    }

}
