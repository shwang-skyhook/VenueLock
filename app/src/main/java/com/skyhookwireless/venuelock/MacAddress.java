package com.skyhookwireless.venuelock;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class MacAddress extends RealmObject {
    @Required
    private String _id;
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

    public String get_Id() { return _id; }

    public void set_Id(String _id) { this._id = _id; }

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
