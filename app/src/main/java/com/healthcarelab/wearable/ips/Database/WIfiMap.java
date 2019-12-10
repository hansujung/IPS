package com.healthcarelab.wearable.ips.Database;

import com.healthcarelab.wearable.ips.Signal.WifiInfo;

public class WIfiMap {

    private int x;
    private int y;
    private WifiInfo wifi1;
    private WifiInfo wifi2;
    private WifiInfo wifi3;
    private WifiInfo wifi4;

    public WIfiMap(int x, int y, WifiInfo wifi1, WifiInfo wifi2, WifiInfo wifi3, WifiInfo wifi4){
        this.x = x;
        this.y = y;
        this.wifi1 = wifi1;
        this.wifi2 = wifi2;
        this.wifi3 = wifi3;
        this.wifi4 = wifi4;
    }

    public void setWifi1(WifiInfo wifi1) {
        this.wifi1 = wifi1;
    }

    public void setWifi2(WifiInfo wifi2) {
        this.wifi2 = wifi2;
    }

    public void setWifi3(WifiInfo wifi3) {
        this.wifi3 = wifi3;
    }

    public void setWifi4(WifiInfo wifi4) {
        this.wifi4 = wifi4;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public WifiInfo getWifi1() {
        return wifi1;
    }

    public WifiInfo getWifi2() {
        return wifi2;
    }

    public WifiInfo getWifi3() {
        return wifi3;
    }

    public WifiInfo getWifi4() {
        return wifi4;
    }
}
