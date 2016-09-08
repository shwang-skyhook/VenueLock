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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

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
    private Timer timer;
    private TimerTask doAsynchronousTask;

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


        copyBundledRealmFile(this.getResources().openRawResource(R.raw.export2), "export2");
        RealmConfiguration config = new RealmConfiguration.Builder(this.getActivity().getApplicationContext())
                .name("export2")
                .schemaVersion(0)
                .build();
        realm = Realm.getInstance(config);
        realm.setDefaultConfiguration(config);
        RealmResults<MacAddress> results = realm.where(MacAddress.class).equalTo("mac", "E01C413BD514").findAll();
        if (results.isEmpty()) {
            realm.beginTransaction();MacAddress ma404 = realm.createObject(MacAddress.class);ma404.setMac("E01C413BD514");ma404.setSsid("QEBHW");ma404.setVid("Skyhook vid");ma404.setVname("Skyhook Office");ma404.setVlatitude("42.3519410");ma404.setVlongitude("-71.0478470");realm.commitTransaction();
            realm.beginTransaction();MacAddress ma405 = realm.createObject(MacAddress.class);ma405.setMac("E01C413BDF15");ma405.setSsid("QEBHW");ma405.setVid("Skyhook vid");ma405.setVname("Skyhook Office");ma405.setVlatitude("42.3519410");ma405.setVlongitude("-71.0478470");realm.commitTransaction();
            realm.beginTransaction();MacAddress ma406 = realm.createObject(MacAddress.class);ma406.setMac("E01C413BD515");ma406.setSsid("QEBHW");ma406.setVid("Skyhook vid");ma406.setVname("Skyhook Office");ma406.setVlatitude("42.3519410");ma406.setVlongitude("-71.0478470");realm.commitTransaction();
            realm.beginTransaction();MacAddress ma407 = realm.createObject(MacAddress.class);ma407.setMac("E01C413BD514");ma407.setSsid("QEBHW");ma407.setVid("Skyhook vid");ma407.setVname("Skyhook Office");ma407.setVlatitude("42.3519410");ma407.setVlongitude("-71.0478470");realm.commitTransaction();
            realm.beginTransaction();MacAddress ma408 = realm.createObject(MacAddress.class);ma408.setMac("E01C413BDF14");ma408.setSsid("QEBHW");ma408.setVid("Skyhook vid");ma408.setVname("Skyhook Office");ma408.setVlatitude("42.3519410");ma408.setVlongitude("-71.0478470");realm.commitTransaction();
            realm.beginTransaction();MacAddress ma409 = realm.createObject(MacAddress.class);ma409.setMac("506028363020");ma409.setSsid("QEBHW");ma409.setVid("Skyhook vid");ma409.setVname("Skyhook Office");ma409.setVlatitude("42.3519410");ma409.setVlongitude("-71.0478470");realm.commitTransaction();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public void setScanList(List<ScanResult> wifiList) {
        this.scans = wifiList;
    }

    public void stopScanning() {

        parseCount = 0;
        currentScanTriggers.clear();

        doAsynchronousTask.cancel();
        timer.cancel();
        //myDbHelper.closeDataBase();
    }

    public void startScanning() {
        //initializeDB();
        if (scans != null && scans.size() > 0) {
            scans.clear();
        }
        parseCount = 0;
        parseScan();
    }

    public void parseScan() {
        parseCount++;
        try {
            final Handler handler = new Handler();
            timer = new Timer();
            doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                RealmAsyncTest realmAsyncTest = new RealmAsyncTest();
                                realmAsyncTest.execute(scans);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                Log.e("Venuelock Algorithm", e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsynchronousTask, 0, INTERVAL); //execute in every 50000 ms
        } catch (Exception e)
        {
            Log.e("Venuelock Algorithm", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private class RealmAsyncTest extends AsyncTask<List<ScanResult>, Void, ScannedVenue> {
        String date;

        @Override
        protected void onPreExecute() {
            date = getDate();
        }
        @Override
        protected ScannedVenue doInBackground(List<ScanResult>... wifiList) {
            Realm realm = Realm.getDefaultInstance();
            Log.d("Venuelock Algorithm", "Starting background db query");


            if (currentScanTriggers.size()>15) {
                currentScanTriggers.clear();
            }

            HashMap<String, ScannedVenue> vidToScannedVenueCaseAv3 = new HashMap<>();
            HashMap<String, ScannedVenue> vidToScannedVenueCaseBv3 = new HashMap<>();
            HashMap<String, ScannedVenue> vidToScannedVenueCaseCv3 = new HashMap<>();
            HashMap<String, ScannedVenue> vidToScannedVenueCaseDv3 = new HashMap<>();

            if (wifiList != null && wifiList.length > 0) {
                for (ScanResult sr : wifiList[0]) {
                    if (sr.level >= -73 && sr.level < 0) {   //Filter WiFi APs with Rssi >= -73 dBm
                        String vid = "";
                        RealmResults<MacAddress> results = realm.where(MacAddress.class).equalTo("mac", sr.BSSID.replace(":", "").toUpperCase()).findAll();
                        ScannedVenue scannedVenue = new ScannedVenue();
                        if (results.size() > 0) {
                            vid = results.first().getVid();
                            scannedVenue.setVID(vid);
                            scannedVenue.setName(results.first().getVname());
                            scannedVenue.setvLatLng(results.first().getVlatitude(),results.first().getVlongitude());
                            scannedVenue.addMac(sr.BSSID.replace(":", "").toUpperCase());
                        }
                        if (vid != "") {
                            if (sr.level >= -65) {
                                if (sr.level >= -55) {
                                    if (sr.level >= -50) {
                                        if (vidToScannedVenueCaseAv3.containsKey(vid)) {
                                            vidToScannedVenueCaseAv3.get(vid).IncrementCount();
                                            vidToScannedVenueCaseAv3.get(vid).addMac(sr.BSSID.replace(":", "").toUpperCase());
                                        } else {
                                            if (scannedVenue != null) {
                                                vidToScannedVenueCaseAv3.put(vid, scannedVenue);
                                            }
                                        }
                                    }
                                    if (vidToScannedVenueCaseBv3.containsKey(vid)) {
                                        vidToScannedVenueCaseBv3.get(vid).IncrementCount();
                                        vidToScannedVenueCaseBv3.get(vid).addMac(sr.BSSID.replace(":", "").toUpperCase());
                                    } else {
                                        if (scannedVenue != null) {
                                            vidToScannedVenueCaseBv3.put(vid, scannedVenue);
                                        }
                                    }                                }
                                if (vidToScannedVenueCaseCv3.containsKey(vid)) {
                                    vidToScannedVenueCaseCv3.get(vid).IncrementCount();
                                    vidToScannedVenueCaseCv3.get(vid).addMac(sr.BSSID.replace(":", "").toUpperCase());
                                } else {
                                    if (scannedVenue != null) {
                                        vidToScannedVenueCaseCv3.put(vid, scannedVenue);
                                    }
                                }                              }
                            if (vidToScannedVenueCaseDv3.containsKey(vid)) {
                                vidToScannedVenueCaseDv3.get(vid).IncrementCount();
                                vidToScannedVenueCaseDv3.get(vid).addMac(sr.BSSID.replace(":", "").toUpperCase());
                            } else {
                                if (scannedVenue != null) {
                                    vidToScannedVenueCaseDv3.put(vid, scannedVenue);
                                }
                            }
                        }
                    }
                }
            }

            if (vidToScannedVenueCaseAv3 != null && !vidToScannedVenueCaseAv3.isEmpty()) {
                //case A algorithm
                switch (vidToScannedVenueCaseAv3.size()) {
                    case 0:
                        break;
                    default:
                        /*  a. Filter WiFi APs with Rssi >= -50 dBm
                            b. Count number of APs per venue
                            c. Lock to a venue with the maximum number of APs
                            note that one AP with RSSi higher than -50 dBm is sufficient to VL here
                        */

                        Integer highest = Integer.MIN_VALUE;
                        Integer current = Integer.MIN_VALUE;
                        String venueId = "";
                        for (String vid : vidToScannedVenueCaseAv3.keySet()) {
                            current = vidToScannedVenueCaseAv3.get(vid).getCount();
                            if (current > highest) {
                                highest = current;
                                venueId = vid;
                            }
                        }
                        if (venueId != ""){
                            vidToScannedVenueCaseAv3.get(venueId).setTriggeringAlgorithm("A");
                            return vidToScannedVenueCaseAv3.get(venueId);
                        }
                        break;
                }
            }
            if (vidToScannedVenueCaseBv3 != null && !vidToScannedVenueCaseBv3.isEmpty()) {
                //case B algorithm
                switch (vidToScannedVenueCaseBv3.size()) {
                    case 0:
                        break;
                    default:
                        /*  a. Filter WiFi APs with Rssi >= -55 dBm
                            b. Count number of APs per venue
                            c. Lock to a venue with the maximum number of APs with at least 3 MAC addresses
                        */
                        Integer highest = Integer.MIN_VALUE;
                        Integer current = Integer.MIN_VALUE;
                        String venueId = "";
                        for (String vid : vidToScannedVenueCaseBv3.keySet()) {
                            current = vidToScannedVenueCaseBv3.get(vid).getCount();
                            if (current > highest) {
                                highest = current;
                                venueId = vid;
                            }
                        }
                        if (highest >= 3) {
                            vidToScannedVenueCaseBv3.get(venueId).setTriggeringAlgorithm("B");
                            return vidToScannedVenueCaseBv3.get(venueId);
                        }
                        break;
                }
            }
            if (vidToScannedVenueCaseCv3 != null && !vidToScannedVenueCaseCv3.isEmpty()) {
                //case C algorithm
                switch (vidToScannedVenueCaseCv3.size()) {
                    case 0:
                        break;
                    default:
                        /*  a. Filter WiFi APs with Rssi >= -65 dBm
                            b. Count number of APs per venue
                            c. Lock to a venue with the maximum number of APs with at least 4 MAC addresses
                        */
                        Integer highest = Integer.MIN_VALUE;
                        Integer current = Integer.MIN_VALUE;
                        String venueId = "";
                        for (String vid : vidToScannedVenueCaseCv3.keySet()) {
                            current = vidToScannedVenueCaseCv3.get(vid).getCount();
                            if (current > highest) {
                                highest = current;
                                venueId = vid;
                            }
                        }
                        if (highest >= 4) {
                            vidToScannedVenueCaseCv3.get(venueId).setTriggeringAlgorithm("C");
                            return vidToScannedVenueCaseCv3.get(venueId);
                        }
                        break;
                }
            }
            if (vidToScannedVenueCaseDv3 != null && !vidToScannedVenueCaseDv3.isEmpty()) {
                //case C algorithm
                switch (vidToScannedVenueCaseDv3.size()) {
                    case 0:
                        break;
                    default:
                        /*  a. Filter WiFi APs with Rssi >= -73 dBm
                            b. Count number of APs per venue
                            c. Lock to a venue with the maximum number of APs with at least 9 (nine) MAC addresses
                        */
                        Integer highest = Integer.MIN_VALUE;
                        Integer current = Integer.MIN_VALUE;
                        String venueId = "";
                        for (String vid : vidToScannedVenueCaseDv3.keySet()) {
                            current = vidToScannedVenueCaseDv3.get(vid).getCount();
                            if (current > highest) {
                                highest = current;
                                venueId = vid;
                            }
                        }
                        if (highest >= 9) {
                            vidToScannedVenueCaseDv3.get(venueId).setTriggeringAlgorithm("D");
                            return vidToScannedVenueCaseDv3.get(venueId);
                        }
                        break;
                }
            }
            return null;
        }

        protected void onPostExecute(ScannedVenue scannedVenue) {
            String finishDate = getDate();
            Log.d("Venuelock Algorithm", "Finished background db query");
            if (scannedVenue != null) {
                if (!currentScanTriggers.contains(scannedVenue.getName())){
                    currentScanTriggers.add(scannedVenue.getName());
                    for (String venues : currentScanTriggers) {
                        showNotification(venues);
                        showToast("Triggered venue: " + scannedVenue.getName());
                        venueTriggeredListener.plotVenue(scannedVenue);
                    }
                    strings.add(log);
                    Log.d("Venuelock Trigger", log);
                    Log.d("Venuelock Trigger", scannedVenue.getMacs());
                }
                stringAdapter.notifyDataSetChanged();
            }
        }
    }

    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        String time = format.format(date);
        return time;
    }

    private void showNotification(String message) {
        mBuilder = new NotificationCompat.Builder(getActivity().getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.abc_btn_check_material);
        mBuilder.setContentTitle("VenueLock Trigger");
        mBuilder.setContentText(message);

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        int mNotificationId = 001;
        notificationManager.notify(mNotificationId, mBuilder.build());
    }

    private void showToast(String toastString) {
        Toast.makeText(getActivity().getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getActivity().getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Realm realm;
    private List<ScanResult> scans = new LinkedList<ScanResult>();
    private List<String> strings = new LinkedList<String>();
    private List<String> currentScanTriggers = new LinkedList<String>();
    private ArrayAdapter<String> stringAdapter;
    private ListView lv;
    private Integer parseCount;
    private NotificationCompat.Builder mBuilder;
    private Integer INTERVAL = 3000;
    private String walking;
    onVenueTriggeredListener venueTriggeredListener;

}