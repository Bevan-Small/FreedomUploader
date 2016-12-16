package nz.co.smallcode.freedomuploader;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Created by Bevan on 16-Dec-16.
 */

public class LocationService {

    private LocationManager mLocationManager;
    private Location mLocation;
    private Activity activity;

    private static LocationService instance = null;

    /**
     * Singleton implementation
     * @return
     */
    public static LocationService getLocationManager(Context context)     {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService( Context context )     {
        initLocationService(context);





    }

    /**
     * Handles fetching GPS data
     * @param context
     */
    private void initLocationService(Context context){



    }
}
