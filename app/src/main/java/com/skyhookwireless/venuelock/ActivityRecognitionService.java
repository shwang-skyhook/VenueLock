package com.skyhookwireless.venuelock;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ActivityRecognitionService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String UPDATE_WALKING = "com.skyhookwireless.venuelock.action.UPDATE_WALKING";
    private static final String ACTION_BAZ = "com.skyhookwireless.venuelock.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.skyhookwireless.venuelock.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.skyhookwireless.venuelock.extra.PARAM2";

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    public ActivityRecognitionService(String name) {
        super(name);
    }
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        intent.setAction(UPDATE_WALKING);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if(ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                handleDetectedActivities( result.getProbableActivities() );
            }
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        StringBuilder sb = new StringBuilder();
        for( DetectedActivity activity : probableActivities ) {
            Log.d( "ActivityRecogition", activity.toString());
            sb.append("\n");
            sb.append(activity.toString());
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ScanActivity.ResponseReceiver.UPDATE_WALKING);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(UPDATE_WALKING, sb.toString());
        sendBroadcast(broadcastIntent);
    }

}
