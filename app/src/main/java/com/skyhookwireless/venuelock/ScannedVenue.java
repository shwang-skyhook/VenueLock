package com.skyhookwireless.venuelock;

/**
 * Created by steveh on 2/25/16.
 */
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.HashMap;


public class ScannedVenue {

    public ScannedVenue() {
        vCount = 1;
        triggeringMacs = new ArrayList<String>();
        triggeringMacsAndRssi = new HashMap<String, Integer>();
    }
    public ScannedVenue(String vid, String name, String lat, String lng, String mac, Integer rssi) {
        vId = vid;
        vName = name;
        triggeringAlgorithm = "None";
        this.setvLatLng(lat, lng);
        triggeringMacsAndRssi = new HashMap<String, Integer>();
        this.addMacAndRssi(mac, rssi);
    }

    public void IncrementCount() {
        vCount = vCount +1;
    }

    public void setName(String name) {
        vName = name;
    }

    public void addMac(String mac) {
        triggeringMacs.add(mac);
    }

    public void addMacAndRssi(String mac, Integer rssi) {
        triggeringMacsAndRssi.put(mac, rssi);
    }

    public String getMacs() {
        return triggeringMacsAndRssi.toString();
    }

    public Integer getCount() {
        return triggeringMacsAndRssi.size();
    }

    public void setvLatLng(String lat, String lng) {
        Double vlat = Double.parseDouble(lat);
        Double vlng = Double.parseDouble(lng);
        vLatLng = new LatLng(vlat,vlng);
    }

    public void setTriggeringAlgorithm(String algorithm) {
        triggeringAlgorithm = algorithm;
    }

    public String getTriggeringAlgorithm() {
        if (triggeringAlgorithm != null) {
            return triggeringAlgorithm;
        }
        return "No Triggering Algorithm";
    }

    public LatLng getvLatLng() {
        return vLatLng;
    }

    public String getName() {
        return vName;
    }

    public void setVID(String id) {
        vId = id;
    }

    public String getVID() {
        return vId;
    }

    private String vId;
    private String vName;
    private LatLng vLatLng;
    private Integer vCount;
    private String triggeringAlgorithm;
    private ArrayList<String> triggeringMacs;
    private HashMap<String, Integer> triggeringMacsAndRssi;

}
