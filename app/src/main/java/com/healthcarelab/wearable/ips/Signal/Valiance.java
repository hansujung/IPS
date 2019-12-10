package com.healthcarelab.wearable.ips.Signal;

import java.util.ArrayList;
import java.util.List;

public class Valiance {


    public double Intersection(double[] list_m){

        double sum = 0.0;

        for(int i = 0; i < list_m.length; i++){
            sum += list_m[i];
        }

        //평균
        double average = (double) sum / list_m.length;

        //분산
        double sum2 = 0, v;
        for(int i = 0; i < list_m.length; i++){
            sum2 += Math.pow((list_m[i] - average), 2);
        }

        v = sum2 / list_m.length;

        return 0;

    }
}