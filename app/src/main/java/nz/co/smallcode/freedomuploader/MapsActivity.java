package nz.co.smallcode.freedomuploader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static nz.co.smallcode.freedomuploader.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String BRS_LOG_TAG = "BRS Test";
    static final String EXTRA_LATITUDE = "latitude";
    static final String EXTRA_LONGITUDE = "longitude";
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 100;
    private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mPoint;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }

        mapFragment.getMapAsync(this);
    }

    /////////////////////////////////// Activity Lifecycle /////////////////////////////////////////

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /////////////////////////////////// Map Handling ///////////////////////////////////////////////

    /**
     * Adds a marker in centre of New Zealand by default
     * Sets a long click listener, which clears away old marker and adds a new one at click location
     *
     * @param googleMap map to be handled
     */
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Display device location
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }


        // Add a marker in NZ and move the camera
        mPoint = new LatLng(-40.9198965, 172.9632746);
        mMap.addMarker(new MarkerOptions().position(mPoint).title("New Activity Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mPoint));

        // Set listener for new marker placement
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                // Remove previous marker
                mMap.clear();

                // Add marker at clicked point
                mPoint = point;
                mMap.addMarker(new MarkerOptions()
                        .position(mPoint)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                Log.e(BRS_LOG_TAG,"Point added");
            }
        });

    }

    /**
     * Sends pinned location back to MainActivity
     *
     * @param view Button
     */
    public void usePinnedLocation(View view) {

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LATITUDE, Double.toString(mPoint.latitude));
        resultIntent.putExtra(EXTRA_LONGITUDE, Double.toString(mPoint.longitude));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Sends user location back to MainActivity
     *
     * @param view Button
     */
    public void useMyLocation(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LATITUDE, Double.toString(mCurrentLocation.getLatitude()));
        resultIntent.putExtra(EXTRA_LONGITUDE, Double.toString(mCurrentLocation.getLongitude()));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    /////////////////////////////////// GoogleApiClient Handling ///////////////////////////////////

    /**
     * Builds the api client and creates the location request
     * Api client is automanaged
     */
    private synchronized  void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/* FragmentActivity */,this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    /**
     * Client callback for failed connection to google play services
     * @param result contains info on connection failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO work with error codes to log errors better
        Log.d(BRS_LOG_TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Client callback for successful connection to google play services
     * gets device location
     * @param connectionHint contains info on connection
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        getDeviceLocation();
    }

    /**
     * Client callback for suspension of google play services
     * @param i cause of failure
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(BRS_LOG_TAG, "Play services connection suspended. Cause code: " + i);
    }

    /**
     * Callback for change of location
     * @param location new location
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    /**
     * Sets mLocationRequest to a new location request.
     * Sets update intervals and priority of request
     */
    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();

        // Sets desired update interval (interval will not be exact)
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets fastest update interval (interval is exact)
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Sets device location as last location from google play services
     * Requests updates
     */
    @SuppressWarnings({"MissingPermission"})
    private void getDeviceLocation() {

        if (checkLocationPermission()){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }

    }

    /////////////////////////////////// Permissions Handling ///////////////////////////////////////

    /**
     * Checks for fine location permission. Requests permission if not granted
     *
     * @return true if permission granted or API 22 or below
     */
    private boolean checkLocationPermission() {

        // Permission is granted automatically if sdk version is less than 23
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        // Check that the permission to access fine location has been granted. If not, asks for permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);

        }

        // Returns true if permission granted
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

}
