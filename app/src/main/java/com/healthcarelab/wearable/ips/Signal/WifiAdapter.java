package com.healthcarelab.wearable.ips.Signal;

import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.healthcarelab.wearable.ips.R;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.ViewHolder> {

    private List<ScanResult> results = new ArrayList<>();
    private double coff1;
    private double coff2;

    public WifiAdapter(double coff1, double coff2){
        this.coff1 = coff1;
        this.coff2 = coff2;
    }

    @Override
    public WifiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi, parent, false);
        WifiAdapter.ViewHolder viewHolder = new WifiAdapter.ViewHolder(linearLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.name.setText(results.get(position).SSID + "");
        viewHolder.bssid.setText(results.get(position).BSSID + "");
        viewHolder.level.setText(results.get(position).level + "dB");
        viewHolder.distance.setText(getDistance(results.get(position).frequency, results.get(position).level) + "m");
    }

    public double getDistance(double frequency, double level) {
        double exp = (coff1 - (coff2 * Math.log10(frequency)) + Math.abs(level)) / coff2;
        double distance = pow(10.0, exp);
//        if(distance > 50.0){
//            distance = 50.0;
//        }
        return distance;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name, bssid, level, distance;

        public ViewHolder(LinearLayout v) {
            super(v);
            name = v.findViewById(R.id.tv_wifi_name);
            bssid = v.findViewById(R.id.tv_wifi_bssid);
            level = v.findViewById(R.id.tv_wifi_level);
            distance = v.findViewById(R.id.tv_wifi_distance);
        }
    }

    public List<ScanResult> getResults() {
        return results;
    }

    public void setResults(List<ScanResult> results) {
        this.results = results;
    }

//    public void setCoff1(double coff1) {
//        this.coff1 = coff1;
//    }
//
//    public void setCoff2(double coff2) {
//        this.coff2 = coff2;
//    }
}
