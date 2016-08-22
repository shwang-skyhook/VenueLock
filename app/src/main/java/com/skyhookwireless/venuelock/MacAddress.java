package com.skyhookwireless.venuelock;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class MacAddress extends RealmObject {
    @Required
    private String mac;
    @Required
    private String ssid;
    @Required
    private String vid;
    @Required
    private String vname;
    @Required
    private String vlatitude;
    @Required
    private String vlongitude;

    public String getMac() { return mac; }

    public void setMac(String mac) { this.mac = mac; }

    public String getSsid() { return ssid; }

    public void setSsid(String ssid) { this.ssid = ssid; }

    public String getVid() { return vid; }

    public void setVid(String vid) { this.vid = vid; }

    public String getVname() { return vname; }

    public void setVname(String vname) { this.vname = vname; }

    public String getVlatitude() { return vlatitude; }

    public void setVlatitude(String vlatitude) { this.vlatitude = vlatitude; }

    public String getVlongitude() { return vlongitude; }

    public void setVlongitude(String vlongitude) { this.vlongitude = vlongitude; }

}
