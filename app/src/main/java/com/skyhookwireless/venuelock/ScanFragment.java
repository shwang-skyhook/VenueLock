package com.skyhookwireless.venuelock;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
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
import java.text.SimpleDateFormat;
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
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
        else if (mySensor.getType() == Sensor.TYPE_PRESSURE) {
            lastPressure = event.values[0];
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
//        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        if(imm != null){
//            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
//        }
        scanTextView = (TextView) getActivity().findViewById(R.id.scanTextView);
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        cellManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorBarometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, sensorBarometer , SensorManager.SENSOR_DELAY_NORMAL);
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
                mHandler.postDelayed(mStatusChecker, interval);
                scanDataReceivedListener.startScanning();
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

        wifiList = wifiManager.getScanResults();
        scanDataReceivedListener.sendScanData(wifiList);
        for (ScanResult wifiScan : wifiList) {
            scanSB.append(filename + ", Scan "
                    + new Integer(numScans+1).toString() + ", "
                    + proximity + ", "
                    + wifiScan.toString()+"\n");
        }

        cellList = cellManager.getAllCellInfo();
        for (CellInfo cellScan : cellList) {
            scanSB.append(filename + ", Scan "
                    + new Integer(numScans+1).toString() + ", "
                    + proximity + ", "
                    + cellScan.toString() + "\n");
        }

        scanSB.append(filename + ", Scan "
                + new Integer(numScans+1).toString() + ", "
                + "Atmospheric Pressure in hPA:" +
                + lastPressure + "\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(getActivity() ,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                        super.onScanResult(callbackType, result);
                        scanSB.append(filename + ", Scan "
                                + new Integer(numScans+1).toString() + ", "
                                + proximity + ", " + "Bluetooth: "
                                + result.toString() + "\n");                    }
                };
            }
        }

        if (btAdapter.getState() != BluetoothAdapter.STATE_ON) {
            btAdapter.enable(); }
        btleScanner = btAdapter.getBluetoothLeScanner();
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if (btleScanner != null) {
                btleScanner.startScan(mScanCallback);
            }
        }



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

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mLeDeviceListAdapter.addDevice(device);
                            //mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

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
    private float lastPressure;

    private SensorManager sensorManager;
    private Sensor sensorBarometer;
    onScanDataReceivedListener scanDataReceivedListener;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
}
