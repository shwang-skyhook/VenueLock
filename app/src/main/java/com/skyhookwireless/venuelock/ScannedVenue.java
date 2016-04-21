package com.skyhookwireless.venuelock;

/**
 * Created by steveh on 2/25/16.
 */
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.primitives.Doubles;


public class ScannedVenue {

    public ScannedVenue() {
        vCount = 1;
    }
    public ScannedVenue(String name) {
        vName = name;
        vCount = 1;
    }

    public void IncrementCount() {
        vCount = vCount +1;
    }

    public void setName(String name) {
        vName = name;
    }

    public Integer getCount() {
        return vCount;
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
}
