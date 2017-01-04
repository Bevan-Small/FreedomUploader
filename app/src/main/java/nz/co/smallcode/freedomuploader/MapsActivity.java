package nz.co.smallcode.freedomuploader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static final String EXTRA_LATITUDE = "latitude";
    static final String EXTRA_LONGITUDE = "longitude";

    private GoogleMap mMap;
    private LatLng mPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Adds a marker in centre of New Zealand by default
     * Sets a long click listener, which clears away old marker and adds a new one at click location
     * @param googleMap map to be handled
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in NZ and move the camera
        mPoint = new LatLng(-40.9198965, 172.9632746);
        mMap.addMarker(new MarkerOptions().position(mPoint).title("New Activity Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mPoint));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point){
                // Remove previous marker
                mMap.clear();

                mPoint = point;
                // Add marker at clicked point
                mMap.addMarker(new MarkerOptions()
                        .position(mPoint)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        });

    }

    public void usePinnedLocation(View view){
        // TODO send latlng back to main activity
        Toast.makeText(this,"Button pressed",Toast.LENGTH_LONG).show();
        // TODO Add extras or a data URI to this intent as appropriate.
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LATITUDE, Double.toString(mPoint.latitude));
        resultIntent.putExtra(EXTRA_LONGITUDE, Double.toString(mPoint.longitude));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void useMyLocation(View view){
        // TODO send latlng back to main activity
        Toast.makeText(this,"Button pressed 1",Toast.LENGTH_LONG).show();
    }

}
