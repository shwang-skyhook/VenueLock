package com.skyhookwireless.venuelock;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.io.Files;
import com.skyhookwireless.accelerator.AcceleratorClient;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.cert.CRL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ScanActivity extends AppCompatActivity
        implements  AcceleratorClient.OnConnectionFailedListener,
                    AcceleratorClient.ConnectionCallbacks,
                    AcceleratorClient.OnRegisterForCampaignMonitoringResultListener {

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
        verifyStoragePermissions(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (venueMapFragment != null) {
                    scanFragment.writeGroundTruth(venueMapFragment.getGroundTruth());
                }
                Snackbar.make(view, "Ground Truth Set", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        accelerator = new AcceleratorClient(this, ALEX_KEY, this, this);
        accelerator.connect();

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
            }
            else {
                scanFragment.scanTextView.setText("No Scans created");
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    @Override
    protected void onDestroy() {
        accelerator.disconnect();
        super.onDestroy();
    }

    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(int i) {

    }

    @Override
    public void onRegisterForCampaignMonitoringResult(int i, PendingIntent pendingIntent) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    private final static String ACCELERATOR_KEY = "eJwVwUEKACAIBMBzjxHUUttjYH4q-ns0I034GxBvJ7GtZyzyzUozTKl6gHQ4Yi6rBN8HEUsLFA";
    private final static String ALEX_KEY = "eJwVwUsOABAMBcC1wzShXj-WRHspcXcx00qrXx-QclTgcy0hjhRyqBMjJyXUNodZDb8PEdQLLQ";


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    return VenueMapFragment.newInstance(1);
                case 1:
                    return ScanFragment.newInstance();
                case 2:
                    return BlankFragment.newInstance("i", "i");
            }
            return VenueMapFragment.newInstance(position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    venueMapFragment = (VenueMapFragment) createdFragment;
                    break;
                case 1:
                    scanFragment = (ScanFragment) createdFragment;
                    //scanFragment.setFileName(blankFragment.getFileName());
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
                    return "Map";
                case 1:
                    return "Scans";
                case 2:
                    return "TODO";
            }
            return null;
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
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
                }
                else {
                    scanFragment.scanTextView.setText(fileName + "\n synced");
                }
            }
            else {
                scanFragment.scanTextView.setText("Error when syncing\n Could not sync file");
            }
        }
    }

    private ScanFragment scanFragment;
    private VenueMapFragment venueMapFragment;
    private BlankFragment blankFragment;
    private AcceleratorClient accelerator;
    private String fileName;
}
