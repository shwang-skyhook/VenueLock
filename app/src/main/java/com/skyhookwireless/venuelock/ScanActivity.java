package com.skyhookwireless.venuelock;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.skyhookwireless.accelerator.AcceleratorClient;

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
                    return "Files";
            }
            return null;
        }
    }
    private ScanFragment scanFragment;
    private VenueMapFragment venueMapFragment;
    private AcceleratorClient accelerator;

}
