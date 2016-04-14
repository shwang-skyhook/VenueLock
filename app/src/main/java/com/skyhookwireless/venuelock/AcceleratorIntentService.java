package com.skyhookwireless.venuelock;

import android.app.IntentService;
import android.content.Intent;

import com.skyhookwireless.accelerator.AcceleratorClient;
import com.skyhookwireless.accelerator.CampaignVenue;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class AcceleratorIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "com.skyhookwireless.venuelock.action.FOO";
    public static final String ACTION_BAZ = "com.skyhookwireless.venuelock.action.BAZ";

    // TODO: Rename parameters
    public static final String EXTRA_PARAM1 = "com.skyhookwireless.venuelock.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "com.skyhookwireless.venuelock.extra.PARAM2";

    public AcceleratorIntentService() {
        super("AcceleratorIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            if (AcceleratorClient.hasError(intent)) {
                int errorCode = AcceleratorClient.getErrorCode(intent);
                //handle error...
            } else {
                CampaignVenue venue = AcceleratorClient.getTriggeringCampaignVenue(intent);
                if (venue != null) {
                    if (AcceleratorClient.getCampaignVenueTransition(intent) == CampaignVenue.CAMPAIGN_VENUE_TRANSITION_ENTER) {
                        //process enter transition...
                    } else {
                        //process exit transition...
                    }
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
