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

public class Floor15Activity extends AppCompatActivity implements BeaconConsumer, SensorEventListener {

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

    // 15층 와이파이 정보 & 와이파이 관련 변수
    private WifiInfo testlab = new WifiInfo("testlab", "8a:36:6c:ce:4b:34", 780.0, 3555.0);
    private WifiInfo ResearchPlanning = new WifiInfo("ResearchPlanning", "88:36:6c:18:b7:de", 1395.0, 1000.0);
    private WifiInfo wearable3 = new WifiInfo("wearable3", "90:9f:33:f2:a6:4e", 1556.0, 1914.0);
    private WifiInfo wearable4 = new WifiInfo("wearable4", "8a:36:6c:ce:79:a8", 1800.0, 2100.0);
    private WifiInfo aict = new WifiInfo("Aict-Wlans", "00:1c:c5:06:b3:40", 1124.0, 4266.0);
    private WifiInfo wifiInfo[] = {testlab, ResearchPlanning, wearable3, wearable4};
    private ImageView iv, wifi1, wifi2, wifi3;
    private WifiManager mainWifi;
    private WifiListReceiver receiverWifi;
    private List<ScanResult> results = new ArrayList<>();
    private WifiAdapter wifiAdapter;
    private boolean wifiWasEnabled;

    List<Double> testlab_d_list = new ArrayList<Double>();
    List<Double> resear_d_list = new ArrayList<Double>();
    List<Double> wear3_d_list = new ArrayList<Double>();
    List<Double> wear4_d_list = new ArrayList<Double>();
    List<Double> beacon_list = new ArrayList<Double>();

    List<WifiItem> allWifiList = new ArrayList<WifiItem>();

    // 15층 비컨 정보 & 비컨 관련 변수 (블루투스 관련)
    private BluetoothInfo Lemon1 = new BluetoothInfo("lemon1", "EE:CB:CC:05:B1:5E", 1500.0, 850.0);
    private BluetoothInfo Lemon2 = new BluetoothInfo("lemon2", "DE:DD:80:81:1C:F1", 1500.0, 1700.0);
    private BluetoothInfo Lemon3 = new BluetoothInfo("lemon3", "E5:C1:4A:63:B5:7F", 1500.0, 2400.0);
    private BluetoothInfo beaconinfo[] = {Lemon1, Lemon2, Lemon3};
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconAdapter beaconAdapter;
    private BeaconManager mBeaconManager;
    private Vector<BeaconItem> items_b;
    private int p[] = {0, 0, 0, 0, 0, 0};
    private String name[] = {" ", " ", " "};
    private TextView tv_wifi, tv_beacon, tv_log;

    // 파이어베이스 관련
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
    private long Time;
    private String day_s;
    private String time_s;
    SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timetime = new SimpleDateFormat("HH:mm:ss");
    private AndroidModel am = AndroidModel.forThisDevice();
    private String user = am.getModel();

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
        setContentView(R.layout.activity_floor15);

        // 초기화
        testlab.setDist(0.0);
        ResearchPlanning.setDist(0.0);
        wearable3.setDist(0.0);
        wearable4.setDist(0.0);

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
        mBeaconManager.bind(Floor15Activity.this);

        iv = findViewById(R.id.iv_position);

        // 와이파이 위치에 와이파이 사진 위치시킴
        wifi1 = findViewById(R.id.iv_wifi1);
        wifi1.setX((float) (ResearchPlanning.getPx() * ratio[0]));
        wifi1.setY((float) (ResearchPlanning.getPy() * ratio[1]));

        wifi2 = findViewById(R.id.iv_wifi2);
        wifi2.setX((float) (wearable3.getPx() * ratio[0]));
        wifi2.setY((float) (wearable3.getPy() * ratio[1]));

        wifi3 = findViewById(R.id.iv_wifi3);
        wifi3.setX((float) (testlab.getPx() * ratio[0]));
        wifi3.setY((float) (testlab.getPy() * ratio[1]));

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


        dbHelper = new DBHelper(this, "floor15", null, 1);
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
                        testlab_d_list = setDistanceList(testlab_d_list, testlab.getDist());
                        testlab.setDistance_list(testlab_d_list);
                        resear_d_list = setDistanceList(resear_d_list, ResearchPlanning.getDist());
                        ResearchPlanning.setDistance_list(resear_d_list);
                        wear3_d_list = setDistanceList(wear3_d_list, wearable3.getDist());
                        wearable3.setDistance_list(wear3_d_list);
                        tv_log.setText("T: " + String.format("%.3f", testlab.getDist()) + "\nW: " + String.format("%.3f", wearable3.getDist()) + "\nR: " + String.format("%.3f", ResearchPlanning.getDist()) + "\nscan" + positioning_method + "\nrun" + positioning_test++);

                        /*
                         *   블루투스 비컨을 이용하여 복도에서 거리 계산
                         *
                         *      스캔된 비컨이 하나 이상일때, 가장 가까운 비컨의 거리가 1.5m 이내 일 때,
                         *      걸음이 발견되면 걸음에 따라 위치 변화
                         *
                         *
                         *      맨 처음에 정해 준 비컨에 따라 위치 계산
                         *
                         *       ****************** 문제점1 : x 축으로 움직이는게 없음
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
                                tv_wifi.setText("복도: " + testlab.getSsid() + ", " + (int) testlab.getDist());
                                tv_beacon.setText("복도: " + ResearchPlanning.getSsid() + ", " + (int) ResearchPlanning.getDist());
                            } else {
                                run_val = "비컨";
                                if ((beacon_list.size() >= period) && (beacon_list.size() % 2 == 0)
                                        && (items_b.get(0).getAddress().equals(Lemon1.getBssid()) || items_b.get(0).getAddress().equals(Lemon2.getBssid()) || items_b.get(0).getAddress().equals(Lemon3.getBssid()))) {
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
                             *
                             *       맨 처음 정해준 와이파이 위치 값 적용
                             *       ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★샤오미에서안돼애애애ㅐ애애  >> 방향(1, 2, 3, 4)도이상함.
                             *
                             *       ****************** 문제점1 : 왔다리 갔다리 >> 전 위치에 따라 현 위치 배치할 때 나타남 >> 두번째로 가까운 값 까지 비교 △
                             *       ****************** 문제점2 : wifi scan 느림, 멀어지면 리스트에 추가 안되서 계속 그값. >> 계속 같은 값이 추가됨 이전값 없애기 (초기화 필요함) ○
                             *       ****************** 문제점3 : 걸어서 이동한거랑, 와이파이 검색해서 추가된 위치랑 값이 안맞아서 역주행 >>  값이 보폭*2 차이나면 그 방향으로 보폭만 움직이게?
                             *       ****************** 문제점4 : 와이파이 위치 정할 때 생기는 딜레이 시간 >> 와이파이 검색할 동안 주기마다 와이파이 검색
                             *       ****************** 문제점5 : 3번마다 한번씩 와이파이 위치 지정시 이동 타임
                             */
                        } else {
                            /* 문제점4 : 와이파이 위치 정할 때 생기는 딜레이 시간 >> 와이파이 검색할 동안 주기마다 와이파이 검색 */
                            if (positioning_method % period != 0) {
//                            if (count_step - last_count > 1) {
                                run_val = "추측";
                                DeadReckoning deadReckoning = new DeadReckoning(step_value, direction, count_step - last_count, iv_point, ratio, floor);
                                double temp[] = deadReckoning.RetPosition(deadReckoning);
                                x_distance = temp[0];
                                y_distance = temp[1];
                                last_count = count_step;
                                tv_wifi.setText("복도: " + testlab.getSsid() + ", " + (int) testlab.getDist());
                                tv_beacon.setText("복도: " + ResearchPlanning.getSsid() + ", " + (int) ResearchPlanning.getDist());
                            } else {
                                testlab.setAver_dist(getAvearge(testlab.getDistance_list(), period));
                                wearable3.setAver_dist(getAvearge(wearable3.getDistance_list(), period));
                                ResearchPlanning.setAver_dist(getAvearge(ResearchPlanning.getDistance_list(), period));

                                run_val = "WIFI";
                                WifiPosition wifiPosition = new WifiPosition(testlab, wearable3, ResearchPlanning);
                                double[] ret_val = wifiPosition.returnPosition(wifiPosition);

                                Log.d("Y_MOVE", "move : " + ret_val[0] + "wifi : " + ret_val[1]);

                                Log.d("Wifi Signal", "wearable3 " + wearable3.getDist());
                                Log.d("Wifi Signal", "testlab " + testlab.getDist());
                                Log.d("Wifi Signal", "ResearchPlanning " + ResearchPlanning.getDist());

                                /* 문제점1 : 왔다리 갔다리 >> 전 위치에 따라 현 위치 배치할 때 나타남 >> 방향까지 고려?
                                          >> 두번째로 가까운 값 까지 비교  왜 저멀리 갈가...*/
                                if (ret_val[1] == 1) { // 1 > testlab
//                                    y_distance = testlab.getPy() - ret_val[0];
                                    if (ret_val[2] == 0 && (testlab.getPy() - iv_point[1] < 0)) {
                                        y_distance = testlab.getPy() + ret_val[0];
                                    } else {
                                        y_distance = testlab.getPy() - ret_val[0];
                                    }
                                    tv_wifi.setText("메인: " + testlab.getSsid() + ", " + (int) testlab.getAver_dist());
                                    tv_beacon.setText("서브: " + ResearchPlanning.getSsid() + ", " + (int) ResearchPlanning.getAver_dist() + "/ " + wearable3.getSsid() + ", " + (int) wearable3.getAver_dist());
                                } else if (ret_val[1] == 2) { // 2 >> wearable3
                                    if (ret_val[2] == 3) {
                                        y_distance = wearable3.getPy() - ret_val[0];
                                    } else {
                                        y_distance = wearable3.getPy() + ret_val[0];
                                    }
                                    tv_wifi.setText("메인: " + wearable3.getSsid() + ", " + (int) wearable3.getAver_dist());
                                    tv_beacon.setText("서브: " + testlab.getSsid() + ", " + (int) testlab.getAver_dist() + "/ " + ResearchPlanning.getSsid() + ", " + (int) ResearchPlanning.getAver_dist());
                                } else if (ret_val[1] == 3) { //3 >> ResearchPlanning
//                                    y_distance = ResearchPlanning.getPy() + ret_val[0];
                                    if (ret_val[2] == 0 || iv_point[1] - ResearchPlanning.getPy() < 0) {
                                        y_distance = ResearchPlanning.getPy() - ret_val[0];
                                    } else {
                                        y_distance = ResearchPlanning.getPy() + ret_val[0];
                                    }
                                    tv_wifi.setText("메인: " + ResearchPlanning.getSsid() + ", " + (int) ResearchPlanning.getAver_dist());
                                    tv_beacon.setText("서브: " + wearable3.getSsid() + ", " + (int) wearable3.getAver_dist() + "/ " + testlab.getSsid() + ", " + (int) testlab.getAver_dist());
                                }

                                x_distance = 100 * (floor[0] / 2) + 100;
                                beaconAdapter = new BeaconAdapter(items_b, Floor15Activity.this);
                                beaconAdapter.notifyDataSetChanged();

                            }
                        }

                        /* 한번에 휙 움직이는거 방지 >> 조건문 고쳐야ㅑㅐ대ㅐㅐ */
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

                        if (positioning_test % 2 == 0) {

                            Time = System.currentTimeMillis();
                            day_s = dayTime.format(new Date(Time));
                            time_s = timetime.format(new Date(Time));


                            Map<String, Integer> map = new HashMap<>();
                            map.put("x", (int) iv_point[0]);
                            map.put("y", (int) iv_point[1]);
                            databaseReference.child("15").child(user).child(day_s).child(time_s).setValue(map);
                        }

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
                /* 문제점2 : wifi scan 느림, 멀어지면 리스트에 추가 안되서 계속 그값. >> 계속 같은 값이 추가됨 이전값 없애기 (초기화 필요함) */
                testlab.setDist(0.0);
                ResearchPlanning.setDist(0.0);
                wearable3.setDist(0.0);

                for (int i = 0; i < mainWifi.getScanResults().size(); i++) {
                    try {
                        String wifibssid = mainWifi.getScanResults().get(i).BSSID;
                        double wififreq = mainWifi.getScanResults().get(i).frequency;
                        double wifilevel = mainWifi.getScanResults().get(i).level;
                        double distance = wifiAdapter.getDistance(wififreq, wifilevel);
                        if (wifibssid.equals(testlab.getBssid())) {
                            testlab.setDist(distance);
                            Log.d("testlab", "testlab_distance: " + testlab.getDist());
                        } else if (wifibssid.equals(ResearchPlanning.getBssid())) {
                            ResearchPlanning.setDist(distance);
                            Log.d("ResearchPlanning", "res_distance: " + ResearchPlanning.getDist());
                        } else if (wifibssid.equals(wearable3.getBssid())) {
                            wearable3.setDist(distance);
                            Log.d("wearable3", "wearable3_distance: " + wearable3.getDist());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.d("exception", "e: " + e);
                        continue;
                    }

                }
            }
        }, 1);
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
            if (items.get(j).getAddress().equals(Lemon1.getBssid())) {
                p[2 * j] = (int) Lemon1.getPx();
                p[2 * j + 1] = (int) Lemon1.getPy();
                name[j] = Lemon1.getSsid();
            } else if (items.get(j).getAddress().equals(Lemon2.getBssid())) {
                p[2 * j] = (int) Lemon2.getPx();
                p[2 * j + 1] = (int) Lemon2.getPy();
                name[j] = Lemon2.getSsid();
            } else if (items.get(j).getAddress().equals(Lemon3.getBssid())) {
                p[2 * j] = (int) Lemon3.getPx();
                p[2 * j + 1] = (int) Lemon3.getPy();
                name[j] = Lemon3.getSsid();
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

            String wifi_str = String.valueOf(testlab.getLevel()) + "," + String.valueOf(wearable3.getLevel()) + "," + String.valueOf(ResearchPlanning.getLevel());
            String mag_str = String.valueOf(x_axis) + "," + String.valueOf(y_axis) + "," + String.valueOf(z_axis);

            databaseReference.child("15").child(user).child("wifi").child(wifi_str).setValue(map);
            databaseReference.child("15").child(user).child("magn").child(mag_str).setValue(map);
        }

        Log.d("insertRSSI", "data insert");
    }
}


