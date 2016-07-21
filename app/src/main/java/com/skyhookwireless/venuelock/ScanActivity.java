package com.skyhookwireless.venuelock;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.maps.model.LatLng;
import com.skyhookwireless.accelerator.AcceleratorClient;
import com.skyhookwireless.accelerator.CampaignVenue;
import com.skyhookwireless.accelerator.NearbyCampaignVenue;
import com.skyhookwireless.accelerator.VenueInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ScanActivity extends AppCompatActivity
        implements BlankFragment.onVenueTriggeredListener,
        ScanFragment.onScanDataReceivedListener,
        AcceleratorClient.OnConnectionFailedListener,
        AcceleratorClient.ConnectionCallbacks,
        AcceleratorClient.OnRegisterForCampaignMonitoringResultListener,
        AcceleratorClient.OnStopCampaignMonitoringResultListener,
        AcceleratorClient.OnStartCampaignMonitoringResultListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnected(Bundle bundle) {
        Intent activityRecognitionIntent = new Intent( this, ActivityRecognitionService.class );
        PendingIntent activityRecognitionPendingIntent = PendingIntent.getService( this, 0, activityRecognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, activityRecognitionPendingIntent );

        IntentFilter filter = new IntentFilter(ResponseReceiver.UPDATE_WALKING);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String UPDATE_WALKING = "com.skyhookwireless.venuelock.intent.action.UPDATE_WALKING";

        @Override
        public void onReceive(Context context, Intent intent) {
            String walkingConfidence = intent.getStringExtra(ActivityRecognitionService.UPDATE_WALKING);
            blankFragment.setActivityRecognition(walkingConfidence);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStopCampaignMonitoringResult(int i, String s) {

    }

    @Override
    public void onStartCampaignMonitoringResult(int i, String s) {
        boolean monitoringAll = accelerator.isMonitoringAllCampaigns();
    }

    @Override
    public void sendScanData(List<ScanResult> wifiList) {
        blankFragment.setScanList(wifiList);
    }

    @Override
    public void stopScanning() {
        if (accelerator.isConnected()){
            fetchNearbyMonitoredVenues();
        }
        blankFragment.stopScanning();

        new AlertDialog.Builder(this)
                .setTitle("Save Scan Session on Server?")
                .setMessage("Scan sessions are always saved locally.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        fileName = scanFragment.getFileName();
                        if (fileName != null) {
                            new UploadLogTask().execute(fileName);
                        } else {
                            scanFragment.scanTextView.setText("No Scans created");
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_menu_save)
                .show();

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(false);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Comments")
                .setMessage("Include any notes regarding this scan")
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        scanFragment.AppendComments(input.getText().toString());
                    }
                })
                .setIcon(android.R.drawable.btn_plus)
                .show();

    }

    @Override
    public void startScanning() {
        if (accelerator.isConnected()) {
            accelerator.startMonitoringForAllCampaigns(this);
            fetchNearbyMonitoredVenues();
        }
        blankFragment.startScanning();
    }

    @Override
    public void plotVenue(ScannedVenue scannedVenue) {
        scanFragment.writeVenueLockTrigger(scannedVenue);
        venueMapFragment.plotTriggeredVenue(scannedVenue);
    }


    public void fetchNearbyMonitoredVenues() {
        accelerator.fetchNearbyMonitoredVenues(40, new AcceleratorClient.NearbyMonitoredVenuesListener() {
            @Override
            public void onNearbyMonitoredVenuesFetched(List<NearbyCampaignVenue> venues) {
                // Fetch venue information for nearby venues
                List<Long> ids = new ArrayList<Long>(venues.size());
                for (NearbyCampaignVenue venue : venues) {
                    ids.add(venue.venueId);
                }
                accelerator.fetchVenueInfo(ids, new AcceleratorClient.VenueInfoListener() {
                    @Override
                    public void onVenueInfoFetched(List<VenueInfo> venues) {
                        // handle venue information...
                        for (VenueInfo venueInfo: venues) {
                            LatLng position = new LatLng(venueInfo.latitude, venueInfo.longitude);
                            venueMapFragment.plotNearbyVenue(position, venueInfo.name, venueInfo.venueId);
                        }
                    }
                    @Override
                    public void onVenueInfoError(int errorCode) {
                        // handle fetch venue info error...
                        Integer n = 0;

                    }
                });
            }
            @Override
            public void onNearbyMonitoredVenuesError(int errorCode) {
                // handle fetch venue info error...
                Integer n = 0;
            }
        });
    }


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        verifyPermissions(this);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (venueMapFragment != null) {
                    String groundTruth = venueMapFragment.getGroundTruth();
                    scanFragment.writeGroundTruth(groundTruth);
                    Snackbar.make(view, "Ground Truth Set at " + groundTruth, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        accelerator = new AcceleratorClient(this, VENUELOCK, this, this);
        accelerator.connect();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            fileName = scanFragment.getFileName();
            if (fileName != null) {
                new UploadLogTask().execute(fileName);
            } else {
                scanFragment.scanTextView.setText("No Scans created");
            }
            return true;
        }
        else if (id == R.id.refresh_accelerator_venues) {
            fetchNearbyMonitoredVenues();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    @Override
    protected void onDestroy() {
        if (accelerator != null) {accelerator.disconnect();
        }
        super.onDestroy();
    }

    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onConnected() {

        Intent intent = new Intent(this, AcceleratorIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        accelerator.registerForCampaignMonitoring(pendingIntent, this);

        IntentFilter filter = new IntentFilter(ResponseReceiver.UPDATE_WALKING);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDisconnected() {
        unregisterReceiver(receiver);
    }

    @Override
    public void onConnectionFailed(int i) {
        i = 9;
    }

    @Override
    public void onRegisterForCampaignMonitoringResult(int i, PendingIntent pendingIntent) {
        //accelerator.stopMonitoringForAllCampaigns(this);
        //accelerator.startMonitoringForAllCampaigns(this);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    private final static String ACCELERATOR_KEY = "eJwVwUEKACAIBMBzjxHUUttjYH4q-ns0I034GxBvJ7GtZyzyzUozTKl6gHQ4Yi6rBN8HEUsLFA";
    private final static String ALEX_KEY = "eJwVwUsOABAMBcC1wzShXj-WRHspcXcx00qrXx-QclTgcy0hjhRyqBMjJyXUNodZDb8PEdQLLQ";
    private final static String VENUELOCK = "eJwVwUEKACAIBMBzjxEsk3aPhfap6O_RTC1VP4f2clyxNyOFICQVJhZ0YZuINXsO2H0TQAs6";
    private final static String VENUELOCKCMEPORTAL = "eJwNwcENACEIAMG3xZC4GII81UBT5no_Z2j0h-5Ou740pxri5JEZZZKmSI0NteKMqO8HD-kLKw";

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return ScanFragment.newInstance();
                case 1:
                    return VenueMapFragment.newInstance(1);
                case 2:
                    return BlankFragment.newInstance("i", "i");
            }
            return ScanFragment.newInstance();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    scanFragment = (ScanFragment) createdFragment;
                    break;
                case 1:
                    venueMapFragment = (VenueMapFragment) createdFragment;
                    break;
                case 2:
                    blankFragment = (BlankFragment) createdFragment;
                    break;

            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Scans";
                case 1:
                    return "Map";
                case 2:
                    return "Venues";
            }
            return null;
        }
    }

    public void verifyPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //// show explanation asynchronously
            }
            else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    accelerator.startMonitoringForAllCampaigns(this);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int PERMISSION_ALL = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    class UploadLogTask extends AsyncTask<String, Void, Response> {
        protected Response doInBackground(String... filename) {
            String url = "http://contextdev3.skyhookwireless.com/venuelock-research-server/rest/upload";
            File textFile = new File(Environment.getExternalStorageDirectory(), filename[0]);

            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("multipart/form-data;");
                RequestBody body = RequestBody.create(mediaType, textFile);

                MultipartBody m = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", textFile.getName(), body)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(m)
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println(response.toString());
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return null;

            }
        }

        @Override
        protected void onPostExecute(Response r) {
            if (r != null) {
                if (r.code() != 200) {
                    scanFragment.scanTextView.setText("Error when syncing\n Response code: " + r.code() + "\nCould not sync file");
                } else {
                    scanFragment.scanTextView.setText(fileName + "\n Succesfully synced");
                }
            } else {
                scanFragment.scanTextView.setText("Error when syncing\nCould not sync file\nNo response");
            }
        }
    }

    private ScanFragment scanFragment;
    private VenueMapFragment venueMapFragment;
    private BlankFragment blankFragment;
    private AcceleratorClient accelerator;
    private String fileName;
    private Boolean mLocationPermissions;
    private ResponseReceiver receiver;
    public GoogleApiClient mApiClient;
    public Boolean walking;
}
