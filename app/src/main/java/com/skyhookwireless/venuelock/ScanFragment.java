package com.skyhookwireless.venuelock;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class ScanFragment extends Fragment implements View.OnClickListener{
    public ScanFragment() {
    }

    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        fileNameEditText = (EditText) view.findViewById(R.id.fileNameEditText);
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
        scanTextView.setMovementMethod(new ScrollingMovementMethod());
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        cellManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
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
                fileNameEditText.setFocusable(false);
                if (fileNameEditText.getText() != null) {
                    filename = "venuelock-" + fileNameEditText.getText().toString() +"-" + getDate()+ ".txt";
                }
                else {
                    filename = "venuelock-" + getDate()+ ".txt";
                }
                mHandler.postDelayed(mStatusChecker, interval);
                break;
            case R.id.stopScanButton:
                scanSB.setLength(0);
                fileNameEditText.setFocusableInTouchMode(true);
                fileNameEditText.setFocusable(true);
                startScanButton.setEnabled(true);
                stopScanButton.setEnabled(false);
                mHandler.removeCallbacks(mStatusChecker);
                scanTextView.setText("Scan Finished with " + numScans + " scans.\n ");
                numScans = 0;
                break;
            case R.id.outsideButton:
                //scanSB.append("Outside \n");
                proximity = "Outside";
                showToast(proximity);
                break;
            case R.id.nearbyButton:
                proximity = "Nearby";
                showToast(proximity);
                //scanSB.append("Nearby \n");
                break;
            case R.id.veryCloseButton:
                proximity = "Very Close";
                showToast(proximity);
                //scanSB.append("Very Close \n");
                break;
            case R.id.justInsideButton:
                proximity = "Just Inside";
                showToast(proximity);
                //scanSB.append("Just Inside \n");
                break;
            case R.id.completelyInsideButton:
                proximity = "Completely Inside";
                showToast(proximity);
                //scanSB.append("Completely Inside \n");
                break;
        }
    }

    public String getScans() {
        wifiList = wifiManager.getScanResults();
        cellList = cellManager.getAllCellInfo();
        scanSB.append("Scan number " + new Integer(numScans+1).toString() + " (" + proximity + "):\n");
        scanSB.append(wifiList.toString() + "\n");
        scanSB.append(cellList.toString() + "\n");
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
            scanTextView.setText(filename + "\nScan " +new Integer(numScans+1).toString()+"\n");
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

    private void showToast(String toastString) {
        Toast.makeText(getActivity().getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    public String getFileName(){
        return filename;
    }

    private File file;
    private FileOutputStream outputstream;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private List<CellInfo> cellList;
    private StringBuilder scanSB = new StringBuilder();
    private int interval = 5000;
    private Handler mHandler;
    private int numScans = 0;
    private Button startScanButton, stopScanButton, setFileNameButton;
    private EditText fileNameEditText;
    TextView scanTextView;
    String filename, gTruth, proximity, customFileName;
    TelephonyManager cellManager;

}
