package com.skyhookwireless.venuelock;

/**
 * Created by steveh on 2/25/16.
 */
import com.google.android.gms.maps.model.LatLng;


public class ScannedVenue {
    public ScannedVenue(String id, Integer count) {
        vId = id;
        vCount = count;
    }

    private String vId;
    private String vName;
    private LatLng vLatLng;
    private Integer vCount;
}
