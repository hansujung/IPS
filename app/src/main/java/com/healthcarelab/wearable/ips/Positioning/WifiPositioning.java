package com.healthcarelab.wearable.ips.Positioning;


import com.healthcarelab.wearable.ips.Signal.WifiInfo;

public class WifiPositioning {
    private WifiInfo wifi1;
    private WifiInfo wifi2;
    private WifiInfo wifi3;

    private static final double[] zero = {0.0, 0.0};

    public double[] positioning(WifiInfo wifi1, WifiInfo wifi2, WifiInfo wifi3, int period, double filterR, double filterQ){
        this.wifi1 = wifi1;
        this.wifi2 = wifi2;
        this.wifi3 = wifi3;

        double[] point = {0.0, 0.0};

        Intersection inter = new Intersection();

        double[] point1 = inter.Intersection(wifi1, wifi2, period, filterR, filterQ);
        double[] point2 = inter.Intersection(wifi2, wifi3, period, filterR, filterQ);
        double[] point3 = inter.Intersection(wifi1, wifi3, period, filterR, filterQ);


        if(point1.equals(zero)){
            point[0] = (point2[0] + point3[0])/2;
            point[1] = (point2[1] + point3[1])/2;
        }else if(point2.equals(zero)){
            point[0] = (point1[0] + point3[0])/2;
            point[1] = (point1[1] + point3[1])/2;
        }else if(point3.equals(zero)){
            point[0] = (point1[0] + point2[0])/2;
            point[1] = (point1[1] + point2[1])/2;
        }else if(point1.equals(zero) && point2.equals(zero)) {
            point[0] = point3[0];
            point[1] = point3[1];
        }else if(point2.equals(zero) && point3.equals(zero)) {
            point[0] = point1[0];
            point[1] = point1[1];
        }else if(point1.equals(zero) && point3.equals(zero)) {
            point[0] = point2[0];
            point[1] = point2[1];
        }else{
            point[0] = (point1[0] + point2[0] + point3[0])/3;
            point[1] = (point1[1] + point2[1] + point3[1])/3;
        }

        return point;
    }

}
