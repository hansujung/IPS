package com.healthcarelab.wearable.ips;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.healthcarelab.wearable.ips.Signal.BeaconAdapter;
import com.healthcarelab.wearable.ips.Signal.BeaconItem;
import com.healthcarelab.wearable.ips.Signal.WifiAdapter;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {


    private EditText ed_coff1, ed_coff2, ed_scan_period, ed_filterQ, ed_filterR, ed_height;
    private Button btn_scan_start, btn_move;

    private final static String floor15wifi[] = {"testlab", "wearable3", "wearable4", "ResearchPlanning"};
    private final static String floor13wifi[] = {"wearable", "wearable2", "smartgrid-13"};
    private final static String floor1wifi[] = {};

    public double coff1;
    public double coff2;

    //wifi
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private List<ScanResult> scanResults;
    private ArrayList<String> wifiResults;
    private WifiAdapter wifiAdapter = new WifiAdapter(coff1, coff2);

    //bluetooth
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ed_coff1 = (EditText) findViewById(R.id.ed_coff1);
        ed_coff2 = (EditText) findViewById(R.id.ed_coff2);
        ed_scan_period = (EditText) findViewById(R.id.ed_scan_period);
        ed_filterQ = (EditText) findViewById(R.id.ed_filterQ);
        ed_filterR = (EditText) findViewById(R.id.ed_filterR);
        ed_height = (EditText) findViewById(R.id.ed_height);
        btn_scan_start = (Button) findViewById(R.id.btn_scan_start);
        btn_move = (Button) findViewById(R.id.btn_move);

        coff1 = Double.parseDouble(ed_coff1.getText().toString());
        coff2 = Double.parseDouble(ed_coff2.getText().toString());

        //Wifi

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        wifiResults = new ArrayList<String>();




        btn_scan_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = null;



                int wifi_num = wifiManager.getScanResults().size();
                int floor_num = 0;
                for (int i = 0; i < wifi_num; i++) {
                    String wifiname = wifiManager.getScanResults().get(i).SSID;
                    Log.d("wifiname", wifiname);
                    if (wifiname.equals(floor15wifi[0]) || wifiname.equals(floor15wifi[1]) || wifiname.equals(floor15wifi[2]) || wifiname.equals(floor15wifi[3])) {
                        intent = new Intent(MainActivity.this, Floor15Activity.class);
                        floor_num = 15;
                        break;
                    } else if (wifiname.equals(floor13wifi[0]) || wifiname.equals(floor13wifi[1]) || wifiname.equals(floor13wifi[2])) {
                        intent = new Intent(MainActivity.this, Floor13Activity.class);
                        floor_num = 13;
                        break;
                    }
//                    else {
//                        Toast.makeText(MainActivity.this, "와이파이가 부족합니다.", Toast.LENGTH_SHORT).show();
//                        intent = new Intent(MainActivity.this, ScanResultActivity.class);
//                        break;
//                    }
                }

                intent.putExtra("coff1", ed_coff1.getText().toString());
                intent.putExtra("coff2", ed_coff2.getText().toString());
                intent.putExtra("scan_period", ed_scan_period.getText().toString());
                intent.putExtra("filterQ", ed_filterQ.getText().toString());
                intent.putExtra("filterR", ed_filterR.getText().toString());
                intent.putExtra("user_height", ed_height.getText().toString());

                Toast.makeText(MainActivity.this, "지금 " + floor_num + "에 있습니다.", Toast.LENGTH_SHORT).show();

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    startActivity(intent);
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 100);
                }
            }
        });

        btn_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanResultActivity.class);

                intent.putExtra("coff1", ed_coff1.getText().toString());
                intent.putExtra("coff2", ed_coff2.getText().toString());

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    startActivity(intent);
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 100);
                }
                Log.d("btn_move", "intent" + intent);
            }
        });

    }

    protected void onResume() {
        super.onResume();
        // Register wifi receiver to get the results
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    protected void onPause() {
        super.onPause();
        // Unregister the wifi receiver
        unregisterReceiver(wifiReceiver);
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiResults.clear();
            scanResults = wifiManager.getScanResults();
            Collections.sort(scanResults, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult t1, ScanResult t2) {
                    if (t1.level > t2.level) {
                        return -1;
                    } else if (t1.level < t2.level) {
                        return 1;
                    }
                    return 0;
                }
            });
            wifiAdapter.setResults(scanResults);
            wifiAdapter.notifyDataSetChanged();
        }
    }
}
