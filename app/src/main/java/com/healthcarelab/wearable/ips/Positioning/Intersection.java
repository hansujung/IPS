package com.healthcarelab.wearable.ips.Positioning;

import com.healthcarelab.wearable.ips.Signal.KalmanFilter;
import com.healthcarelab.wearable.ips.Signal.WifiInfo;

import java.util.List;

public class Intersection {

    private double x1, x2, y1, y2, r1, r2, fR, fQ;
    private int period;

    public double[] Intersection(WifiInfo wifi1, WifiInfo wifi2, int period, double filterR, double filterQ) {


        this.x1 = wifi1.getPx();
        this.x2 = wifi2.getPx();
        this.y1 = wifi1.getPy();
        this.y2 = wifi2.getPy();

        this.r1 = getAvearge(wifi1.getDistance_list(), period);
        this.r2 = getAvearge(wifi2.getDistance_list(), period);

        double temp1 = (x2 - x1) * (x2 - x1);
        double temp2 = (y2 - y1) * (y2 - y1);
        double temp_D = Math.sqrt(temp1 + temp2);
        double temp_DD = temp1 + temp2;
        double temp_delta = (Math.sqrt((temp_D + r1 + r2) * (temp_D + r1 - r2) * (temp_D - r1 + r2) * (-temp_D + r1 + r2))) / 4;

        double temp3 = (x1 + x2) / 2;
        double temp4 = (x2 - x1) * (r1 * r1 - r2 * r2);
        double temp5 = 2 * (y1 - y2);

        double x_val_1 = temp3 + (temp4 / temp_DD) / 2 + temp5 * temp_delta / temp_DD;
        double x_val_2 = temp3 + (temp4 / temp_DD) / 2 - temp5 * temp_delta / temp_DD;

        double temp6 = (y1 + y2) / 2;
        double temp7 = (y2 - y1) * (r1 * r1 - r2 * r2);
        double temp8 = 2 * (x1 - x2);

        double y_val_1 = temp6 + (temp7 / temp_DD) / 2 - temp8 * temp_delta / temp_DD;
        double y_val_2 = temp6 + (temp7 / temp_DD) / 2 + temp8 * temp_delta / temp_DD;

        double[] point = {(x_val_1 + x_val_2) / 2, (y_val_1 + y_val_2) / 2};

        return point;
    }



    public double getAvearge(List<Double> list, int period) {
        double sum = 0;
        double average = 0;
        int n = list.size();
        KalmanFilter kalmanFilter = new KalmanFilter(fR, fQ);
        for (int i = 1; i < period + 1; i++) {
            double temp = kalmanFilter.filter(list.get(n - i));
            sum += temp;
        }
        average = sum / (period * 1.0);
        return average;
    }


}
