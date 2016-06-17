package com.skyhookwireless.venuelock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by steveh on 12/15/15.
 */
public class ScanFragment extends Fragment implements View.OnClickListener, SensorEventListener {
    public ScanFragment() {
    }

    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        return fragment;
    }

    public interface onScanDataReceivedListener {
        public void sendScanData(List<ScanResult> wifiList);

        public void stopScanning();
        public void startScanning();
    }
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorBarometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            List<Float> eventList = Arrays.asList(event.values[0], event.values[1], event.values[2]);
            accelData.add(eventList);
        }
        else if (mySensor.getType() == Sensor.TYPE_PRESSURE) {
            pressureData.add(event.values[0]);
        }
        else if (mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            List<Float> eventList = Arrays.asList(event.values[0], event.values[1], event.values[2]);
            magData.add(eventList);
        }
        else if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            List<Float> eventList = Arrays.asList(event.values[0], event.values[1], event.values[2]);

            gravData.add(eventList);
        }
        else if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            List<Float> eventList = Arrays.asList(event.values[0], event.values[1], event.values[2]);

            gyroData.add(eventList);
        }
        else if (mySensor.getType() == Sensor.TYPE_LIGHT) {
            lightData.add(event.values[0]);
        }
        else if (mySensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            humidityData.add(event.values[0]);
        }
        else if (mySensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            temperatureData.add(event.values[0]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            scanDataReceivedListener = (onScanDataReceivedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        venueEditText = (EditText) view.findViewById(R.id.venueEditText);
        userEditText = (EditText) view.findViewById(R.id.userEditText);

        startScanButton = (Button) view.findViewById(R.id.startScanButton);
        startScanButton.setOnClickListener(this);
        stopScanButton = (Button) view.findViewById(R.id.stopScanButton);
        stopScanButton.setOnClickListener(this);
        stopScanButton.setEnabled(false);
        Button outsideButton = (Button) view.findViewById(R.id.outsideButton);
        outsideButton.setOnClickListener(this);
        Button nearbyButton = (Button) view.findViewById(R.id.nearbyButton);
        nearbyButton.setOnClickListener(this);
        Button veryCloseButton = (Button) view.findViewById(R.id.veryCloseButton);
        veryCloseButton.setOnClickListener(this);
        Button justInsideButton = (Button) view.findViewById(R.id.justInsideButton);
        justInsideButton.setOnClickListener(this);
        Button completelyInsideButton = (Button) view.findViewById(R.id.completelyInsideButton);
        completelyInsideButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mHandler = new Handler();

        scanTextView = (TextView) getActivity().findViewById(R.id.scanTextView);
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        cellManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bleDevices = new ArrayList<String>();
        if (btAdapter != null) {    // make sure device supports bluetooth
            if (!btAdapter.isEnabled()) { // if bluetooth is disabled, turn on
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            btleScanner = btAdapter.getBluetoothLeScanner();
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                    super.onScanResult(callbackType, result);
                    bleDevices.add(result.toString());
                }
            };
        }

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorBarometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGrav = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorHumid = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        sensorManager.registerListener(this, sensorBarometer , 100000); // Delay between samples is 100,000 microseconds. 10 samples per second
        sensorManager.registerListener(this, sensorMagnetometer , 100000);
        sensorManager.registerListener(this, sensorLight , 100000);
        sensorManager.registerListener(this, sensorGyro , 100000);
        sensorManager.registerListener(this, sensorAccel , 100000);
        sensorManager.registerListener(this, sensorGrav , 100000);
        sensorManager.registerListener(this, sensorHumid , 100000);
        sensorManager.registerListener(this, sensorTemp , 100000);

        this.pressureData = new ArrayList<Float>();
        this.lightData = new ArrayList<Float>();
        this.magData = new ArrayList<List<Float>>();
        this.accelData = new ArrayList<List<Float>>();
        this.gyroData = new ArrayList<List<Float>>();
        this.gravData = new ArrayList<List<Float>>();
        this.humidityData = new ArrayList<Float>();
        this.temperatureData = new ArrayList<Float>();

        mallCheckBox = (CheckBox) view.findViewById(R.id.checkBox);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.startScanButton:
                startScanButton.setEnabled(false);
                stopScanButton.setEnabled(true);
                venueEditText.setFocusable(false);
                userEditText.setFocusable(false);

                if (venueEditText.getText() != null && userEditText.getText() != null) {
                    if (venueEditText.getText().toString().isEmpty() && userEditText.getText().toString().isEmpty()) {
                        if (mallCheckBox.isChecked())
                            filename = "venuelock-" + getDate()+ "-mall.txt";
                        else
                            filename = "venuelock-" + getDate()+ ".txt";
                    }
                    else {
                        if (mallCheckBox.isChecked())
                            filename = "venuelock-" + userEditText.getText().toString() + "-" + venueEditText.getText().toString() +"-" + getDate()+ "-mall.txt";
                        else
                            filename = "venuelock-" + userEditText.getText().toString() + "-" + venueEditText.getText().toString() +"-" + getDate()+ ".txt";
                    }
                }
                else {
                    filename = "venuelock-" + getDate()+ "-mall.txt";
                }
                clearScanCaches();
                mHandler.postDelayed(mStatusChecker, interval);
                scanDataReceivedListener.startScanning();
                btleScanner.startScan(mScanCallback);
                break;
            case R.id.stopScanButton:
                scanSB.setLength(0);
                venueEditText.setFocusableInTouchMode(true);
                venueEditText.setFocusable(true);
                userEditText.setFocusableInTouchMode(true);
                userEditText.setFocusable(true);
                startScanButton.setEnabled(true);
                stopScanButton.setEnabled(false);
                mHandler.removeCallbacks(mStatusChecker);
                scanDataReceivedListener.stopScanning();
                scanTextView.setText("Scan Finished with " + numScans + " scans.\n\n ");
                numScans = 0;
                btleScanner.stopScan(mScanCallback);
                break;
            case R.id.outsideButton:
                proximity = "Outside";
                showToast(proximity);
                break;
            case R.id.nearbyButton:
                proximity = "Nearby";
                showToast(proximity);
                break;
            case R.id.veryCloseButton:
                proximity = "Very Close";
                showToast(proximity);
                break;
            case R.id.justInsideButton:
                proximity = "Just Inside";
                showToast(proximity);
                break;
            case R.id.completelyInsideButton:
                proximity = "Completely Inside";
                showToast(proximity);
                break;
        }
    }

    public String getScans() {
        //scanSB.append("Scan number " + new Integer(numScans+1).toString() + " (" + proximity + "):\n");

        Log.d("Venuelock Scanfragment","collecting wifi data");
        wifiList = wifiManager.getScanResults();
        scanDataReceivedListener.sendScanData(wifiList);
        for (ScanResult wifiScan : wifiList) {
            scanSB.append(filename + ", Scan "
                    + new Integer(numScans+1).toString() + ", "
                    + proximity + ", "
                    + wifiScan.toString()+"\n");
        }

        Log.d("Venuelock Scanfragment","collecting cell data");
        cellList = cellManager.getAllCellInfo();
        for (CellInfo cellScan : cellList) {
            scanSB.append(filename + ", Scan "
                    + new Integer(numScans+1).toString() + ", "
                    + proximity + ", "
                    + cellScan.toString() + "\n");
        }

        Log.d("Venuelock Scanfragment","collecting bluetooth data");
        for (String device : bleDevices) {
            scanSB.append(filename + ", Scan "
                    + new Integer(numScans+1).toString() + ", "
                    + proximity + ", Bluetooth device: "
                    + device + "\n");
        }
        bleDevices.clear();

        AppendSensorData(numScans + 1);

        if (isExternalStorageWritable())
        {
            try {
                file = new File(Environment.getExternalStorageDirectory(), filename);
                outputstream = new FileOutputStream(file);
                outputstream.write(scanSB.toString().getBytes());
                outputstream.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
                showToast("Exception, File write failed: " + e.toString());
            }
            scanTextView.setText(filename + "\nScan " +new Integer(numScans+1).toString()+", Proximity: "+proximity+"\n\n");
        }
        numScans++;

        return scanSB.toString();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            getScans();
            mHandler.postDelayed(mStatusChecker, interval);
        }
    };


    private boolean isExternalStorageWritable () {
        String State = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(State)) {
            return true;
        }
        return false;
    }

    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        String time = format.format(date);
        return time;
    }

    public void writeGroundTruth(String groundTruth) {
        scanSB.append("Ground Truth Set: "+ groundTruth + "\n");
    }

    public void writeVenueLockTrigger(ScannedVenue venue) {
        scanSB.append("VenueLock Trigger VID: " + venue.getVID()
                        + " " + venue.getvLatLng().toString()
                        + " Venue Name: " + venue.getName()
                        + " Triggering Algorithm: " + venue.getTriggeringAlgorithm() + "\n");
    }

    private void showToast(String toastString) {
        Toast.makeText(getActivity().getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }


    public String getFileName(){
        return filename;
    }

    private void AppendSensorData(Integer scanNumber) {
        Log.d("VenueLock ScanFragment", "Appending sensor data");

        String date = getDate();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + pressureData.size() + ", "
                + "Atmospheric Pressure in hPA: "
                + pressureData.toString() + "\n");
        pressureData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + lightData.size() + ", "
                + "Ambient light level in SI lux units: "
                + lightData.toString() + "\n");
        lightData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + gravData.size() + ", "
                + "Gravity data in m/s^2: "
                + gravData.toString() + "\n");
        gravData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + magData.size() + ", "
                + "Magnet data in micro-Tesla: "
                + magData.toString() + "\n");
        magData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + accelData.size() + ", "
                + "Accelerator data in m/s^2: "
                + accelData.toString() + "\n");
        accelData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + gyroData.size() + ", "
                + "Gyroscope data in radians/second: "
                + gyroData.toString() + "\n");
        gyroData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + humidityData.size() + ","
                + "Humidity data in percentage: "
                + humidityData.toString() + "\n");
        humidityData.clear();

        scanSB.append(filename + ", " + date + ", Scan " + scanNumber + ", Samples: "
                + temperatureData.size() + ","
                + "Temperature data in C: "
                + temperatureData.toString() + "\n");
        temperatureData.clear();

        try {
            file = new File(Environment.getExternalStorageDirectory(), filename);
            outputstream = new FileOutputStream(file);
            outputstream.write(scanSB.toString().getBytes());
            outputstream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            showToast("Exception, File write failed: " + e.toString());
        }
    }

    private void clearScanCaches() {
        pressureData.clear();
        lightData.clear();
        humidityData.clear();
        temperatureData.clear();
        magData.clear();
        accelData.clear();
        gyroData.clear();
        gravData.clear();
        bleDevices.clear();
    }


    public void AppendComments(String comments) {
        scanSB.append(comments);
        try {
            file = new File(Environment.getExternalStorageDirectory(), filename);
            outputstream = new FileOutputStream(file);
            outputstream.write(scanSB.toString().getBytes());
            outputstream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            showToast("Exception, File write failed: " + e.toString());
        }
    }



    private File file;
    private FileOutputStream outputstream;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private List<CellInfo> cellList;
    private StringBuilder scanSB = new StringBuilder();
    private int interval = 3000;
    private Handler mHandler;
    private int numScans = 0;
    private Button startScanButton, stopScanButton;
    private CheckBox mallCheckBox;
    private EditText venueEditText;
    private EditText userEditText;
    TextView scanTextView;
    String filename, proximity;
    private TelephonyManager cellManager;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btleScanner;
    ScanCallback mScanCallback;
    private float lastPressure, magnetx, magnety, magnetz;
    private List<Float> pressureData, lightData, humidityData, temperatureData;
    private List<List<Float>> magData, accelData, gyroData, gravData;
    private List<String> bleDevices;

    private SensorManager sensorManager;
    private Sensor sensorBarometer, sensorMagnetometer, sensorLight, sensorAccel, sensorGyro, sensorGrav, sensorHumid, sensorTemp;
    onScanDataReceivedListener scanDataReceivedListener;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
}
