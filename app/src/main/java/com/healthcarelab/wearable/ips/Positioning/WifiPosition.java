package com.healthcarelab.wearable.ips.Positioning;

import com.healthcarelab.wearable.ips.Signal.WifiInfo;

import java.util.HashMap;
import java.util.Map;

public class WifiPosition {
    private WifiInfo wifi1;
    private WifiInfo wifi2;
    private WifiInfo wifi3;

    public WifiPosition(WifiInfo wifi1, WifiInfo wifi2, WifiInfo wifi3){
        this.wifi1 = wifi1;
        this.wifi2 = wifi2;
        this.wifi3 = wifi3;
    }

    public double[] returnPosition(WifiPosition wifiPosition){
        WifiInfo wifi1 = wifiPosition.getWifi1();
        WifiInfo wifi2 = wifiPosition.getWifi2();
        WifiInfo wifi3 = wifiPosition.getWifi3();

        double move_distance;
        double wifi;
        double sub_wifi;

        if(wifi1.getAver_dist() == 0 && wifi2.getAver_dist() == 0 && wifi3.getAver_dist() == 0){
            move_distance = 0.0;
            wifi = 0;
            sub_wifi = 0;
        } else if (wifi1.getAver_dist() == 0 && wifi2.getAver_dist() == 0 && wifi3.getAver_dist() > 0){
            move_distance = (wifi3.getAver_dist() * 100 - (wifi3.getPx()/2));
            wifi = 3;
            sub_wifi = 0;
        } else if (wifi1.getAver_dist() == 0 && wifi2.getAver_dist() > 0 && wifi3.getAver_dist() == 0){
            move_distance = (wifi2.getAver_dist() * 100 - (wifi2.getPx()/2));
            wifi = 2;
            sub_wifi = 0;
        } else if (wifi1.getAver_dist() > 0 && wifi2.getAver_dist() == 0 && wifi3.getAver_dist() == 0){
            move_distance = (wifi1.getAver_dist() * 100 - (wifi1.getPx()/2));
            wifi = 1;
            sub_wifi = 0;
        } else if (wifi1.getAver_dist() == 0 && wifi2.getAver_dist() > 0 && wifi3.getAver_dist() > 0){
            if(wifi2.getAver_dist() >= wifi3.getAver_dist()){
                move_distance = (wifi3.getAver_dist() * 100 - (wifi3.getPx()/2));
                wifi = 3;
                sub_wifi = 2;
            } else {
                move_distance = (wifi2.getAver_dist() * 100 - (wifi2.getPx()/2));
                wifi = 2;
                sub_wifi = 3;
            }
        } else if (wifi1.getAver_dist() > 0 && wifi2.getAver_dist() > 0 && wifi3.getAver_dist() == 0){
            if(wifi1.getAver_dist() >= wifi2.getAver_dist()){
                move_distance = (wifi2.getAver_dist() * 100 - (wifi2.getPx()/2));
                wifi = 2;
                sub_wifi = 1;
            } else {
                move_distance = (wifi1.getAver_dist() * 100 - (wifi1.getPx()/2));
                wifi = 1;
                sub_wifi = 2;
            }
        } else if (wifi1.getAver_dist() > 0 && wifi2.getAver_dist() == 0 && wifi3.getAver_dist() > 0){
            if(wifi1.getAver_dist() >= wifi3.getAver_dist()){
                move_distance = (wifi3.getAver_dist() * 100 - (wifi3.getPx()/2));
                wifi = 3;
                sub_wifi = 1;
            } else {
                move_distance = (wifi1.getAver_dist() * 100 - (wifi1.getPx()/2));
                wifi = 1;
                sub_wifi = 3;
            }
        } else if (wifi1.getAver_dist() > 0 && wifi2.getAver_dist() > 0 && wifi3.getAver_dist() > 0){
            if(wifi1.getAver_dist() <= wifi2.getAver_dist() && wifi1.getAver_dist() <= wifi3.getAver_dist()){
                move_distance = (wifi1.getAver_dist() * 100 - (wifi1.getPx()/2));
                wifi = 1;
                if(wifi2.getAver_dist() <= wifi3.getAver_dist()){
                    sub_wifi = 2;
                } else {
                    sub_wifi = 3;
                }
            } else if(wifi2.getAver_dist() <= wifi1.getAver_dist() && wifi2.getAver_dist() <= wifi3.getAver_dist()){
                move_distance = (wifi2.getAver_dist() * 100 - (wifi2.getPx()/2));
                wifi = 2;
                if(wifi1.getAver_dist() <= wifi3.getAver_dist()){
                    sub_wifi = 1;
                } else {
                    sub_wifi = 3;
                }
            } else {
                move_distance = (wifi3.getAver_dist() * 100 - (wifi3.getPx()/2));
                wifi = 3;
                if(wifi1.getAver_dist() <= wifi2.getAver_dist()){
                    sub_wifi = 1;
                } else {
                    sub_wifi = 2;
                }
            }
        } else {
            move_distance = 0.0;
            wifi = 0;
            sub_wifi = 0;
        }

        double[] ret_val = {move_distance, wifi, sub_wifi};
        return ret_val;
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
}
