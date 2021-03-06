package com.healthcarelab.wearable.ips;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.healthcarelab.wearable.ips.Database.DBHelper;
import com.healthcarelab.wearable.ips.Positioning.DeadReckoning;
import com.healthcarelab.wearable.ips.Signal.KalmanFilter;
import com.healthcarelab.wearable.ips.Positioning.WifiPosition;
import com.healthcarelab.wearable.ips.Signal.BeaconAdapter;
import com.healthcarelab.wearable.ips.Signal.BeaconItem;
import com.healthcarelab.wearable.ips.Signal.BluetoothInfo;
import com.healthcarelab.wearable.ips.Signal.WifiAdapter;
import com.healthcarelab.wearable.ips.Signal.WifiInfo;
import com.healthcarelab.wearable.ips.Signal.WifiItem;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.distance.AndroidModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Floor13Activity extends AppCompatActivity implements BeaconConsumer, SensorEventListener {

    public static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    public static int display_wid; //화면 해상도
    public static int display_hei;
    // 화면 비율, 초기 위치 관련 변수
    private static double floor[] = {20.0, 55.0}; // 15층 사이즈(단위는 m)
    private final Handler handler = new Handler();

    // setting parameter value and initial value
    public double coff1;
    public double coff2;
    public double filterR;
    public double filterQ;
    public int user_hei_ed;
    public int last_count = 0;

    // PDR관련 추가 변수
    private int count_step = 0;
    private int direction = 0; // 1:북쪽, 2: 동쪽, 3: 남쪽, 4: 서쪽
    private int step_value = 70;
    private TextView tv_sensor;
    private SensorManager sensorManager; // 센서관련
    private Sensor direction_sensor;
    private Sensor accel_sensor;
    private Sensor count_sensor;
    private Sensor mag_sensor;
    private int x_axis, y_axis, z_axis = 0;

    private boolean moveFlag = false;
    private double iv_point[] = {1000.0, 2500.0}; //초기 위치 설정
    private int period = 0; //와이파이 비컨 평균계산 주기
    private double ratio[] = {(float) 0.7, (float) 0.4}; //화면 비율

    // 13층 와이파이 정보 & 와이파이 관련 변수
    private WifiInfo wearable = new WifiInfo("wearable", "", 1556.0, 1600.0);
    private WifiInfo wearable2 = new WifiInfo("wearable2","", 1556.0, 1000.0);
    private WifiInfo smartgrid = new WifiInfo("smartgrid-13","", 1500.0, 3555.0);
    private WifiInfo aict = new WifiInfo("Aict-Wlans","", 1100.0, 3555.0);
    private WifiInfo wifiInfo[] = {wearable, wearable2, smartgrid};
    private ImageView iv, wifi1, wifi2, wifi3;
    private WifiManager mainWifi;
    private WifiListReceiver receiverWifi;
    private List<ScanResult> results = new ArrayList<>();
    private WifiAdapter wifiAdapter;
    private boolean wifiWasEnabled;

    List<Double> wear_d_list = new ArrayList<Double>();
    List<Double> wear2_d_list = new ArrayList<Double>();
    List<Double> smart_d_list = new ArrayList<Double>();
    List<Double> beacon_list = new ArrayList<Double>();

    List<WifiItem> allWifiList = new ArrayList<WifiItem>();

    // 15층 비컨 정보 & 비컨 관련 변수 (블루투스 관련)
    private BluetoothInfo Candy1 = new BluetoothInfo("candy1", "", 1500.0, 850.0);
    private BluetoothInfo Candy2 = new BluetoothInfo("candy2", "",  1500.0, 1500.0);
    private BluetoothInfo Candy3 = new BluetoothInfo("candy3", "", 1500.0, 2400.0);
    private BluetoothInfo beaconinfo[] = {Candy1, Candy2, Candy3};
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconAdapter beaconAdapter;
    private BeaconManager mBeaconManager;
    private Vector<BeaconItem> items_b;
    private int p[] = {0, 0, 0, 0, 0, 0};
    private String name[] = {" ", " ", " "};
    private TextView tv_wifi, tv_beacon, tv_log;

    // 파이어베이스 관련
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
    private long Time = System.currentTimeMillis();
    private String day_s;
    private String time_s;
    private AndroidModel am = AndroidModel.forThisDevice();
    private String user = am.getModel();
    private SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timetime = new SimpleDateFormat("HH:mm:ss");

    // 프로그램 동작 관련 변수
    private double x_distance = iv_point[0];
    private double y_distance = iv_point[1];
    private int positioning_method = 0;
    private int positioning_test = 0;
    private boolean flag_wifiscan = false;
    private TextView tv_current;
    private String run_val = null;

    //데이터베이스
    DBHelper dbHelper;
    private Button btn_save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor13);

        // 초기화
        wearable.setDist(0.0);
        wearable2.setDist(0.0);
        smartgrid.setDist(0.0);
        aict.setDist(0.0);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        display_wid = dm.widthPixels;
        display_hei = dm.heightPixels;
        Log.d("display", display_wid + "," + display_hei);

        ratio[0] = display_wid / (floor[0] * 100);
        ratio[1] = (float) display_hei / (floor[1] * 100);
        Log.d("비율", ratio[0] + "," + ratio[1]);

        final Intent intent = getIntent();
        coff1 = Double.parseDouble(intent.getStringExtra("coff1"));
        coff2 = Double.parseDouble(intent.getStringExtra("coff2"));
        period = Integer.parseInt((intent.getStringExtra("scan_period")));
        filterR = Double.parseDouble((intent.getStringExtra("filterR")));
        filterQ = Double.parseDouble((intent.getStringExtra("filterQ")));
        user_hei_ed = Integer.parseInt(intent.getStringExtra("user_height"));

        wifiAdapter = new WifiAdapter(coff1, coff2);


        step_value = user_hei_ed - 120;

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiListReceiver();

        wifiWasEnabled = mainWifi.isWifiEnabled();
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, 100);
        } else {
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));
        }
        try {
            mBeaconManager.setForegroundScanPeriod(600l);
            mBeaconManager.setForegroundBetweenScanPeriod(0l);
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
        }
        mBeaconManager.bind(Floor13Activity.this);

        iv = findViewById(R.id.iv_position);

        // 와이파이 위치에 와이파이 사진 위치시킴
        wifi1 = findViewById(R.id.iv_wifi1);
        wifi1.setX((float) (wearable.getPx() * ratio[0]));
        wifi1.setY((float) (wearable.getPy() * ratio[1]));

        wifi2 = findViewById(R.id.iv_wifi2);
        wifi2.setX((float) (wearable2.getPx() * ratio[0]));
        wifi2.setY((float) (wearable2.getPy() * ratio[1]));

        wifi3 = findViewById(R.id.iv_wifi3);
        wifi3.setX((float) (smartgrid.getPx() * ratio[0]));
        wifi3.setY((float) (smartgrid.getPy() * ratio[1]));

        tv_wifi = (TextView) findViewById(R.id.tv_wifi);
        tv_beacon = (TextView) findViewById(R.id.tv_beacon);
        tv_current = (TextView) findViewById(R.id.tv_current);
        tv_log = (TextView) findViewById(R.id.tv_log);

        tv_sensor = (TextView) findViewById(R.id.tv_sensor);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        direction_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accel_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        count_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mag_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Time = System.currentTimeMillis();
        day_s = dayTime.format(new Date(Time));
        time_s = timetime.format(new Date(Time));

        dbHelper = new DBHelper(this, "floor13", null, 1);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertRSSI();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));
        }
    }

    public void addDevice(Vector<BeaconItem> items_b) {
        Collections.sort(items_b, new Comparator<BeaconItem>() {
            @Override
            public int compare(BeaconItem it1, BeaconItem it2) {
                if (it1.getDistance() > it2.getDistance()) {
                    return 1;
                } else if (it2.getDistance() > it1.getDistance()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public double[] setPositionWithBeacon(BluetoothInfo bluebeacon, double dis) {
        double[] position = {};
        position[0] = bluebeacon.getPx() - dis;
        position[1] = bluebeacon.getPy();
        return position;
    }


    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, final Region region) {
                final Iterator<Beacon> iterator = beacons.iterator();
                items_b = new Vector<>();
                while (iterator.hasNext()) {
                    Beacon beacon = iterator.next();
                    items_b.add(new BeaconItem(beacon.getBluetoothAddress(), beacon.getRssi(), beacon.getDistance()));
                    addDevice(items_b);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // 와이파이
                        wear_d_list = setDistanceList(wear_d_list, wearable.getDist());
                        wearable.setDistance_list(wear_d_list);
                        wear2_d_list = setDistanceList(wear2_d_list, wearable2.getDist());
                        wearable2.setDistance_list(wear2_d_list);
                        smart_d_list = setDistanceList(smart_d_list, smartgrid.getDist());
                        smartgrid.setDistance_list(smart_d_list);
                        tv_log.setText("T: " + String.format("%.3f", wearable.getDist()) + "\nW: " + String.format("%.3f", wearable2.getDist()) + "\nR: " + String.format("%.3f", smartgrid.getDist()) + "\nscan" + positioning_method + "\nrun" + positioning_test++);

                        /*
                         *   블루투스 비컨을 이용하여 복도에서 거리 계산
                         *
                         *      스캔된 비컨이 하나 이상일때, 가장 가까운 비컨의 거리가 1.5m 이내 일 때,
                         *      걸음이 발견되면 걸음에 따라 위치 변화
                         */


                        if (items_b.size() >= 1 && items_b.get(0).getDistance() < 1.5) {
//                                && (items_b.get(0).getAddress().equals(Lemon1.getBssid()) || items_b.get(0).getAddress().equals(Lemon2.getBssid()) || items_b.get(0).getAddress().equals(Lemon3.getBssid()))) {
                            Log.d("BEACON_START", "BEACON: " + items_b.get(0).getAddress());
                            match(items_b);
                            beacon_list.add(items_b.get(0).getDistance());
                            String beaconame = name[0];
                            if (count_step - last_count >= 1) {
                                run_val = "추측";
                                DeadReckoning deadReckoning = new DeadReckoning(step_value, direction, count_step - last_count, iv_point, ratio, floor);
                                double temp[] = deadReckoning.RetPosition(deadReckoning);
                                x_distance = temp[0];
                                y_distance = temp[1];
                                last_count = count_step;
                                tv_wifi.setText("복도: " + wearable.getSsid() + ", " + (int) wearable.getDist());
                                tv_beacon.setText("복도: " + smartgrid.getSsid() + ", " + (int) smartgrid.getDist());
                            } else {
                                run_val = "비컨";
                                if ((beacon_list.size() >= period) && (beacon_list.size() % 2 == 0)
                                        && (items_b.get(0).getAddress().equals(Candy1.getBssid()) || items_b.get(0).getAddress().equals(Candy2.getBssid()) || items_b.get(0).getAddress().equals(Candy3.getBssid()))) {
                                    for (int i = 0; i < beaconinfo.length; i++) {
                                        String b_bssid = beaconinfo[i].getBssid();
                                        if (items_b.get(0).getAddress().equals(b_bssid)) {
                                            x_distance = beaconinfo[i].getPx() - (150 * items_b.get(0).getDistance());
                                            y_distance = beaconinfo[i].getPy();
                                        }
                                    }
                                    tv_wifi.setText("비컨 사용");
                                    tv_beacon.setText("방: " + beaconame + ", " + String.format("%.3f", items_b.get(0).getDistance()));
                                }
                            }
                            /*
                             *   와이파이를 이용하여 복도에서 거리 계산
                             *
                             *       스캔하여 리스트에 저장된 와이파이 거리의 평균 계산
                             *       와이파이 거리 평균 값 가장 작은 곳과 가까이 있음
                             *       이전의 위치값을 참고하여 다음 위치 계산
                             *       걸음이 발견되면 걸음에 따라 위치 변화
                             */
                        } else {
                            if (positioning_method % period != 0) {
                                run_val = "추측";
                                DeadReckoning deadReckoning = new DeadReckoning(step_value, direction, count_step - last_count, iv_point, ratio, floor);
                                double temp[] = deadReckoning.RetPosition(deadReckoning);
                                x_distance = temp[0];
                                y_distance = temp[1];
                                last_count = count_step;
                                tv_wifi.setText("복도: " + wearable.getSsid() + ", " + (int) wearable.getDist());
                                tv_beacon.setText("복도: " + smartgrid.getSsid() + ", " + (int) smartgrid.getDist());
                            } else {
                                wearable.setAver_dist(getAvearge(wearable.getDistance_list(), period));
                                wearable2.setAver_dist(getAvearge(wearable2.getDistance_list(), period));
                                smartgrid.setAver_dist(getAvearge(smartgrid.getDistance_list(), period));

                                run_val = "WIFI";
                                WifiPosition wifiPosition = new WifiPosition(wearable, wearable2, smartgrid);
                                double[] ret_val = wifiPosition.returnPosition(wifiPosition);

                                Log.d("Y_MOVE", "move : " + ret_val[0] + "wifi : " + ret_val[1]);

                                Log.d("Wifi Signal", "wearable " + wearable.getDist());
                                Log.d("Wifi Signal", "wearable2 " + wearable2.getDist());
                                Log.d("Wifi Signal", "smartgrid " + smartgrid.getDist());


                                if (ret_val[1] == 1) { // 1 > smartgrid
                                    y_distance = smartgrid.getPy() - ret_val[0];
                                    tv_wifi.setText("메인: " + smartgrid.getSsid() + ", " + (int) smartgrid.getAver_dist());
                                    tv_beacon.setText("서브: " + wearable.getSsid() + ", " + (int) wearable.getAver_dist() + "/ " + wearable2.getSsid() + ", " + (int) wearable2.getAver_dist());
                                } else if (ret_val[1] == 2) { // 2 >> wearable2
                                    if (ret_val[2] == 3) {
                                        y_distance = wearable2.getPy() - ret_val[0];
                                    } else {
                                        y_distance = wearable2.getPy() + ret_val[0];
                                    }
                                    tv_wifi.setText("메인: " + wearable2.getSsid() + ", " + (int) wearable2.getAver_dist());
                                    tv_beacon.setText("서브: " + wearable.getSsid() + ", " + (int) wearable.getAver_dist() + "/ " + smartgrid.getSsid() + ", " + (int) smartgrid.getAver_dist());
                                } else if (ret_val[1] == 3) { //3 >> wearable
                                    y_distance = wearable.getPy() + ret_val[0];
                                    tv_wifi.setText("메인: " + wearable.getSsid() + ", " + (int) wearable.getAver_dist());
                                    tv_beacon.setText("서브: " + wearable2.getSsid() + ", " + (int) wearable2.getAver_dist() + "/ " + smartgrid.getSsid() + ", " + (int) smartgrid.getAver_dist());
                                }

                                x_distance = 100 * (floor[0] / 2) + 100;
                                beaconAdapter = new BeaconAdapter(items_b, Floor13Activity.this);
                                beaconAdapter.notifyDataSetChanged();

                            }
                        }

                        if(positioning_method > period * 2){

                            if(Math.abs(iv_point[0] - x_distance) > step_value){

                                if ((iv_point[0] - x_distance) > 0) {
                                    x_distance = iv_point[0] - step_value;
                                } else if ((iv_point[0] - x_distance) < 0) {
                                    x_distance = iv_point[0] + step_value;
                                } else {
                                    x_distance = iv_point[0];
                                }
                            }

                            if(Math.abs(iv_point[1] - y_distance) > step_value){

                                if ((iv_point[1] - y_distance) > 0) {
                                    y_distance = iv_point[1] - step_value;
                                } else if ((iv_point[1] - y_distance) < 0) {
                                    y_distance = iv_point[1] + step_value;
                                } else {
                                    y_distance = iv_point[1];
                                }
                            }
                        }

                        iv_point[0] = x_distance;
                        iv_point[1] = y_distance;
                        iv.setX((float) (x_distance * ratio[0]));
                        iv.setY((float) (y_distance * ratio[1]));
                        tv_current.setText("x: " + String.format("%.2f", x_distance) + ", y: " + String.format("%.2f", y_distance) + " run: " + run_val);
                        Log.d("last_pos", iv_point[0] + ", " + iv_point[1] + "   |||   " + x_distance + ", " + y_distance);
                    }
                });
            }
        });
        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {

            @Override
            public void didEnterRegion(Region region) {
            }

            @Override
            public void didExitRegion(Region region) {
            }


            @Override
            public void didDetermineStateForRegion(int state, Region region) {
            }
        });
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public double getAvearge(List<Double> list, int period) {

        if (list.size() > period) {
            double sum = 0;
            double average = 0;
            int n = list.size();
            KalmanFilter kalmanFilter = new KalmanFilter(filterR, filterQ);
            for (int i = 1; i < n; i++) {
                double temp = kalmanFilter.filter(list.get(n - i));
                Log.i("Kalman", "temp: " + temp + "/ ori: " + list.get(i));
                sum += temp;
            }
            average = sum / (period * 1.0);
            return average;
        } else {
            return 0;
        }

    }

    public void refresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainWifi.startScan();
                Log.d("WifiScan", "scan" + positioning_method++);
                wearable.setDist(0.0);
                wearable2.setDist(0.0);
                smartgrid.setDist(0.0);

                for (int i = 0; i < mainWifi.getScanResults().size(); i++) {
                    try {
                        String wifibssid = mainWifi.getScanResults().get(i).BSSID;
                        double wififreq = mainWifi.getScanResults().get(i).frequency;
                        double wifilevel = mainWifi.getScanResults().get(i).level;
                        double distance = wifiAdapter.getDistance(wififreq, wifilevel);
                        if (wifibssid.equals(wearable.getBssid())) {
                            wearable.setDist(distance);
                            Log.d("wearable", "wearable_distance: " + wearable.getDist());
                        } else if (wifibssid.equals(wearable2.getBssid())) {
                            wearable2.setDist(distance);
                            Log.d("wearable2", "wearable2_distance: " + wearable2.getDist());
                        } else if (wifibssid.equals(smartgrid.getBssid())) {
                            smartgrid.setDist(distance);
                            Log.d("smartgrid", "smartgrid_distance: " + smartgrid.getDist());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.d("exception", "e: " + e);
                        continue;
                    }

                }
            }
        }, 1); //샤오미 A1연결해서 하는거면 500이나 1000으로 올리기
    }

    // ***** 와이파이 거리 리스트
    public List setDistanceList(List distanceList, double distance) {
//        if (distance < floor[1] && distance > 0 && distanceList.size() <= period) {
        if (distanceList.size() <= period) {
            distanceList.add(distance);
//        } else if (distance < floor[1] && distance > 0 && distanceList.size() > period) {
        } else if (distanceList.size() > period) {
            distanceList.add(distance);
            distanceList.remove(0);
            for (int i = 0; i < distanceList.size(); i++) {
                Log.d("wifiList", i + " : " + distanceList.get(i));
            }
        }
        return distanceList;
    }

    @Override
    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        sensorManager.registerListener(this, direction_sensor, sensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, accel_sensor, sensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, count_sensor, sensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, mag_sensor, sensorManager.SENSOR_DELAY_UI);
        refresh();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (!wifiWasEnabled) {
            mainWifi.setWifiEnabled(false);
        }
    }

    // ***** 센서 관련
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                int temp = (int) sensorEvent.values[0];
                iv.setRotation(temp);
                if (temp >= 151 && temp < 313) {
                    direction = 3; //남(화물엘베쪽) 171~293
                } else if (temp >= 313 && temp < 335) {
                    direction = 4; //서(경기대쪽) 293~355
                } else if (temp >= 101 && temp < 151) {
                    direction = 2; //동(후문쪽) 81~171
                } else { //나머지 경우
                    direction = 1; //북(엘베쪽) 355~360 0~81
                }
                break;

            case Sensor.TYPE_STEP_COUNTER:
                if (sensorEvent.values[0] != 0) {
                    count_step = (int) sensorEvent.values[0];
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                x_axis = (int) sensorEvent.values[0];
                y_axis = (int) sensorEvent.values[1];
                z_axis = (int) sensorEvent.values[2];
                break;


        }
        tv_sensor.setText("방향: " + direction + "  걸음수: " + count_step);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int match(Vector<BeaconItem> items) {
        for (int j = 0; j < 1; j++) {
            if (items.get(j).getAddress().equals(Candy1.getBssid())) {
                p[2 * j] = (int) Candy1.getPx();
                p[2 * j + 1] = (int) Candy1.getPy();
                name[j] = Candy1.getSsid();
            } else if (items.get(j).getAddress().equals(Candy2.getBssid())) {
                p[2 * j] = (int) Candy2.getPx();
                p[2 * j + 1] = (int) Candy2.getPy();
                name[j] = Candy2.getSsid();
            } else if (items.get(j).getAddress().equals(Candy3.getBssid())) {
                p[2 * j] = (int) Candy3.getPx();
                p[2 * j + 1] = (int) Candy3.getPy();
                name[j] = Candy3.getSsid();
            } else {
                return -1;
            }
        }
        return 0;
    }

    class WifiListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = mainWifi.getScanResults();
            wifiAdapter.setResults(results);
//            wifiAdapter.notifyDataSetChanged();
            refresh();
        }
    }

    //데이터 베이스
    public void insertRSSI() {

        if (iv_point[0] != 0 && iv_point[1] != 0 && iv_point[0] > 0 && iv_point[1] > 0) {
            Map<String, Integer> map = new HashMap<>();
            map.put("x", (int) iv_point[0]);
            map.put("y", (int) iv_point[1]);

            String wifi_str = String.valueOf(wearable.getLevel()) + "," + String.valueOf(wearable2.getLevel()) + "," + String.valueOf(smartgrid.getLevel());
            String mag_str = String.valueOf(x_axis) + "," + String.valueOf(y_axis) + "," + String.valueOf(z_axis);

            databaseReference.child("13").child(user).child("wifi").child(wifi_str).setValue(map);
            databaseReference.child("13").child(user).child("magn").child(mag_str).setValue(map);
        }

        Log.d("insertRSSI", "data insert");
    }
}


