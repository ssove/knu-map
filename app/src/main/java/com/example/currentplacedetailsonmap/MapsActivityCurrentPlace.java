package com.example.currentplacedetailsonmap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback {

    public Context mContext;

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // A default location (Kyungpook National Univ.) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(35.890313, 128.611307); // coordinates of KNU
    private boolean mLocationPermissionGranted;

    private ArrayList<LatLng> locationList = new ArrayList<LatLng>();
    private Location mLastKnownLocation;
    private LocationListener locationListener = new LocationListener() {
        LatLng current;
        LatLng last;
        Marker marker = null;

        @Override
        public void onLocationChanged(Location location) {
            /**
             * Triggered when the device location is changed.
             * parameter location : the changed location
             */

            current = new LatLng(location.getLatitude(), location.getLongitude());

            /**
             * if (Cycle is detected):
             *      drawCapturedPolygon()
             *      reset locationList
             * else if (Collision is detected):
             *      reset locationList
             */
            if (!locationList.isEmpty()
                    && locationList.size() >= 1
                    && current.latitude == locationList.get(0).latitude
                    && current.longitude == locationList.get(0).longitude) {
                /**
                 * User selects color and name of polygon.
                 */
                mMap.clear();
                drawCapturedPolygon();
                locationList = new ArrayList<LatLng>();
                last = null;
            } else if (locationList.contains(current)) {
                mMap.clear();
                locationList = new ArrayList<LatLng>();
                last = null;
            }

            locationList.add(current);

            // Remove the existing marker.
            if (marker != null) {
                marker.remove();
            }

            // Set the new marker.
            MarkerOptions mMarkerOptions = new MarkerOptions().position(current);
            marker = mMap.addMarker(mMarkerOptions);

            // Draw the polyline of movement.
            if (last != null) {
                drawCapturingPolyline(last,current);
                //drawCapturedPolygon();
            }

            mLastKnownLocation = location;
            last = current;
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mContext = getApplicationContext();

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                // Get the permissions.
                if ((ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                        && (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
                    return false;
                }

                // Request to update the location periodically.
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MapsConstants.TIME_INTERVAL_FOR_UPDATING_LOCATION,
                        MapsConstants.LEAST_DISTANCE_FOR_UPDATING_LOCATION,
                        locationListener
                );

                return false;
            }
        });

        // Move the camera to default location/zoom before getting the permission.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, MapsConstants.DEFAULT_ZOOM));

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                // mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Draws polyline with the movement of device.
     * Returns the drawn polyline.
     */
    public PolylineOptions drawCapturingPolyline(LatLng last, LatLng current) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(MapsConstants.DEFAULT_STROKE_WIDTH);
        polylineOptions.color(MapsConstants.DEFAULT_POLYLINE_COLOR);
        polylineOptions.add(last);
        polylineOptions.add(current);
        mMap.addPolyline(polylineOptions);

        return polylineOptions;
    }

    /**
     * Draws polygon with the list of polylines.
     * Returns the drawn polygon.
     */
    public PolygonOptions drawCapturedPolygon() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(locationList);
        polygonOptions.strokeWidth(MapsConstants.DEFAULT_STROKE_WIDTH);
        polygonOptions.strokeColor(MapsConstants.DEFAULT_POLYGON_STROKE_COLOR);
        polygonOptions.fillColor(MapsConstants.DEFAULT_POLYGON_FILL_COLOR);
        mMap.addPolygon(polygonOptions);

        return polygonOptions;
    }
}
