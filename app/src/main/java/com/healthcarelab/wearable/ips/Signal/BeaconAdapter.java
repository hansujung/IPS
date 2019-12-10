package com.healthcarelab.wearable.ips.Signal;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.healthcarelab.wearable.ips.R;

import java.text.DecimalFormat;
import java.util.Vector;

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {

    private Vector<BeaconItem> items;
    private Context context;
    public static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public BeaconAdapter(Vector<BeaconItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public BeaconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth, parent, false);
        BeaconAdapter.ViewHolder vh = new BeaconAdapter.ViewHolder(linearLayout);
        return vh;
    }

    @Override
    public void onBindViewHolder(BeaconAdapter.ViewHolder holder, int position) {
        try {
            holder.beacon_rssi.setText("" + items.get(position).getRssi() + "dB");
            holder.beacon_distance.setText("" + items.get(position).getDistance() + "m");
            holder.beacon_address.setText("" + items.get(position).getAddress() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView beacon_rssi, beacon_distance, beacon_address;

        public ViewHolder(LinearLayout v) {
            super(v);
            beacon_rssi = v.findViewById(R.id.tv_blue_rssi);
            beacon_distance = v.findViewById(R.id.tv_blue_distance);
            beacon_address = v.findViewById(R.id.tv_blue_address);
        }
    }

}
