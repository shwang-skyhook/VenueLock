package com.skyhookwireless.venuelock;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.database.SQLException;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public BlankFragment() {
        // Required empty public constructor
    }

    public interface onVenueTriggeredListener {
        public void plotVenue(ScannedVenue scannedVenue);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        //strings = new LinkedList<String>(Arrays.asList("No Scans Yet"));
        stringAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, strings);

        lv = (ListView) view.findViewById(R.id.listView);
        lv.setAdapter(stringAdapter);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mHandler = new Handler();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            venueTriggeredListener = (onVenueTriggeredListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void clearTriggers() {
        mHandler.removeCallbacks(mStatusChecker);
        currentScanTriggers.clear();
        strings.clear();
        stringAdapter.notifyDataSetChanged();
    }
    public void setScanList(List<ScanResult> wifiList) {
        this.scans = wifiList;
    }

    public void stopScanning() {
        parseCount = 0;
        currentScanTriggers.clear();
        mHandler.removeCallbacks(mStatusChecker);
        //myDbHelper.closeDataBase();
    }

    public void startScanning() {
        //initializeDB();
        parseCount = 0;
        parseScan();
    }

    public void parseScan() {
        parseCount++;
        try {
            if (parseCount > 10) {
                mHandler.removeCallbacks(mStatusChecker);
            }
            new queryDB().execute(this.scans);
            mHandler.postDelayed(mStatusChecker, interval);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //class queryDB extends AsyncTask<List<ScanResult>, Void, Map<String, Integer>> {

    class queryDB extends AsyncTask<List<ScanResult>, Void, ScannedVenue> {

        protected ScannedVenue doInBackground(List<ScanResult>... wifiList) {
            // using this.mContext
            initializeDB();

            if (currentScanTriggers.size()>15) {
                currentScanTriggers.clear();
            }
            HashMap<String, ScannedVenue> vidToScannedVenueCaseA = new HashMap<>();
            HashMap<String, ScannedVenue> vidToScannedVenueCaseB = new HashMap<>();

            if (wifiList != null) {
                for (ScanResult sr : wifiList[0]) {
                    //filter rssi < -80
                    if (sr.level > -80) {
                        if (sr.level > -75) {
                            //remove ":" from mac
                            String vid = myDbHelper.getVidForMac(sr.BSSID.replace(":", "").toUpperCase());
                            //insert vid and count into map
                            if (!vid.equals("")) {
                                if (vidToScannedVenueCaseA.containsKey(vid)) {
                                    vidToScannedVenueCaseA.get(vid).IncrementCount();
                                } else {
                                    ScannedVenue scannedVenue = myDbHelper.getScannedVenue(sr.BSSID.replace(":", "").toUpperCase());
                                    if (scannedVenue != null) {
                                        vidToScannedVenueCaseA.put(vid, scannedVenue);
                                    }
                                }
                            }
                        }
                        else {
                            //remove ":" from mac
                            String vid = myDbHelper.getVidForMac(sr.BSSID.replace(":", "").toUpperCase());
                            //insert vid and count into map
                            if (!vid.equals("")) {
                                if (vidToScannedVenueCaseB.containsKey(vid)) {
                                    vidToScannedVenueCaseB.get(vid).IncrementCount();
                                } else {
                                    ScannedVenue scannedVenue = myDbHelper.getScannedVenue(sr.BSSID.replace(":", "").toUpperCase());
                                    if (scannedVenue != null) {
                                        vidToScannedVenueCaseB.put(vid, scannedVenue);
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if (vidToScannedVenueCaseA != null && !vidToScannedVenueCaseA.isEmpty()) {
                //case A algorithm
                switch (vidToScannedVenueCaseA.size()) {
                    case 0:
                        return null;
                    default:
                        Integer secondHighest = Integer.MIN_VALUE;
                        Integer highest = Integer.MIN_VALUE;
                        Integer current = Integer.MIN_VALUE;
                        String venueId = "";
                        for (String vid : vidToScannedVenueCaseA.keySet()) {
                            current = vidToScannedVenueCaseA.get(vid).getCount();
                            if (current > highest) {
                                secondHighest = highest;
                                highest = current;
                                venueId = vid;
                            } else if (current > secondHighest) {
                                secondHighest = current;
                            }
                        }
                        if (highest >= secondHighest + 2) {
                            return vidToScannedVenueCaseA.get(venueId);
                        } else {
                            return null;
                        }
                }
            }
            else if (vidToScannedVenueCaseB != null && vidToScannedVenueCaseB.size() == 1) {
                for (String vid : vidToScannedVenueCaseB.keySet()) {
                    if (vidToScannedVenueCaseB.get(vid).getCount() > 1) {
                        return vidToScannedVenueCaseB.get(vid);
                    } else {
                        return vidToScannedVenueCaseB.get(vid);
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(ScannedVenue scannedVenue) {
            myDbHelper.closeDataBase();

            if (scannedVenue != null) {
                if (!currentScanTriggers.contains(scannedVenue.getName())){
                    currentScanTriggers.add(scannedVenue.getName());
                    for (String venues : currentScanTriggers) {
                        showNotification(venues);
                        showToast("Triggered venue: " + scannedVenue.getName());
                        venueTriggeredListener.plotVenue(scannedVenue);
                    }
                    strings.add(scannedVenue.getName() + " " +getDate());
                }
                stringAdapter.notifyDataSetChanged();
            }
        }


    }

    private void initializeDB() {
        myDbHelper = new DataBaseHelper(getActivity().getApplicationContext());
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            parseScan();
            mHandler.postDelayed(mStatusChecker, interval);
        }
    };

    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        String time = format.format(date);
        return time;
    }

    private void showNotification(String message) {
        mBuilder = new NotificationCompat.Builder(getActivity().getApplicationContext());
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setSmallIcon(R.drawable.abc_btn_check_material);
        mBuilder.setContentTitle("VenueLock Trigger");
        mBuilder.setContentText(message);

        //Intent intent = new Intent(this, AcceleratorActivity.class);
        //PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        //mBuilder.setContentIntent(pIntent);
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        int mNotificationId = 001;
        notificationManager.notify(mNotificationId, mBuilder.build());
    }

    private void showToast(String toastString) {
        Toast.makeText(getActivity().getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    private List<ScanResult> scans = new LinkedList<ScanResult>();
    private List<String> strings = new LinkedList<String>();
    private List<String> currentScanTriggers = new LinkedList<String>();
    private ArrayAdapter<String> stringAdapter;
    private DataBaseHelper myDbHelper;
    private Handler mHandler;
    private int interval = 8000;
    private ListView lv;
    private Integer parseCount;
    private NotificationCompat.Builder mBuilder;
    onVenueTriggeredListener venueTriggeredListener;

}