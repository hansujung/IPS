package com.healthcarelab.wearable.ips.Signal;

import java.util.ArrayList;
import java.util.List;

public class BluetoothInfo {

    private String ssid;
    private String bssid;
    private double px;
    private double py;
    private double dist;
    private List<Double> distance_list = new ArrayList<Double>();

    public BluetoothInfo(String ssid, String bssid, double px, double py) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.px = px;
        this.py = py;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setPx(double px) {
        this.px = px;
    }

    public void setPy(double py) {
        this.py = py;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public double getDist() {
        return dist;
    }

    public void setDistance_list(List<Double> distance_list) {
        this.distance_list = distance_list;
    }

    public List<Double> getDistance_list() {
        return distance_list;
    }
}
