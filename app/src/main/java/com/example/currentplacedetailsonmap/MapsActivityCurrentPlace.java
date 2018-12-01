package com.example.currentplacedetailsonmap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback {

    public Context mContext;

    CharSequence[] colors = { "Red", "Bule", "Yellow", "Green", "Gray" };
    int selectedColor = -1;

    private boolean isDigging=false;
    private Marker marker = null;

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private List<LatLng> LastPolygonLatLangList;

    // A default location (Kyungpook National Univ.) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(35.890313, 128.611307); // coordinates of KNU
    private boolean mLocationPermissionGranted;

    private ArrayList<LatLng> locationList = new ArrayList<LatLng>();
    private Location mLastKnownLocation;
    private LocationListener locationListener = new LocationListener() {
        LatLng current;
        LatLng last;

        @Override
        public void onLocationChanged(Location location) {
            String TAG = "onLocationChanged";
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
            if(isDigging) {
                if (locationList.size()==1) {
                    last=locationList.get(0);
                }
                if (!locationList.isEmpty()
                        && locationList.size() >= 3
                        && current.latitude == locationList.get(0).latitude
                        && current.longitude == locationList.get(0).longitude) {
                    /**
                     * User selects color and name of polygon.
                     */
                    //setAreaName();
                    makeArea();
                    if (LastPolygonLatLangList != null)
                        Log.e(TAG, LastPolygonLatLangList.toString());
                    else
                        Log.e(TAG, "LastPolygon variable is null");
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
                    drawCapturingPolyline(last, current);
                    //drawCapturedPolygon();
                }

                last = current;
            }
            mLastKnownLocation = location;
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

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get the permissions.
        if ((ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
        }

        // Request to update the location periodically.
        lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MapsConstants.TIME_INTERVAL_FOR_UPDATING_LOCATION,
                MapsConstants.LEAST_DISTANCE_FOR_UPDATING_LOCATION,
                locationListener
        );

        Button startBtn=(Button)findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //status changed
                isDigging=true;

                LatLng current = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                locationList.add(current);

                // Set the new marker.
                MarkerOptions mMarkerOptions = new MarkerOptions().position(current);
                marker = mMap.addMarker(mMarkerOptions);
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            public void onPolygonClick(Polygon polygon) {
                LayoutInflater layoutinflater = LayoutInflater.from(MapsActivityCurrentPlace.this);
                View promptUserView = layoutinflater.inflate(R.layout.dialog_prompt_area_info, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivityCurrentPlace.this);

                alertDialogBuilder.setView(promptUserView);

                final TextView tagField = (TextView) promptUserView.findViewById(R.id.tagView);
                tagField.setText(String.valueOf(polygon.getTag()));
                final TextView usernameField = (TextView) promptUserView.findViewById(R.id.usernameView);
                usernameField.setText(String.valueOf("user"));

                alertDialogBuilder.setTitle("Polygon Info");

                // prompt for username
                alertDialogBuilder.setPositiveButton("close",null);

                // all set and time to build and show up!
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
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
    public Polyline drawCapturingPolyline(LatLng last, LatLng current) {
        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .width(MapsConstants.DEFAULT_STROKE_WIDTH)
                .color(MapsConstants.DEFAULT_POLYLINE_COLOR)
                .add(last)
                .add(current));

        return polyline;
    }

    /**
     * Draws polygon with the list of polylines.
     * Returns the drawn polygon.
     */
    public final Polygon drawCapturedPolygon(String areaname, int color) {
        isDigging=false;
        if(color == 0){
            color = 0xaaff0000;
        } else if (color == 1) {
            color = 0xaa0000ff;
        } else if (color == 2) {
            color = 0xaaffff00;
        } else if (color == 3) {
            color = 0xaa00ff00;
        } else if (color == 4) {
            color = 0xaa808080;
        }

        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .addAll(locationList)
                .strokeWidth(MapsConstants.DEFAULT_STROKE_WIDTH)
                .strokeColor(color)
                .fillColor(color));
        polygon.setTag(areaname);
        polygon.setClickable(true);
        Log.e("Create Polygon",areaname);
        locationList = new ArrayList<LatLng>();
        //locationList.add(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        return polygon;
    }

    public void makeArea() {
        setAreaName();
    }

    public void setAreaName() {
        LayoutInflater layoutinflater = LayoutInflater.from(MapsActivityCurrentPlace.this);
        View promptUserView = layoutinflater.inflate(R.layout.dialog_prompt_area, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivityCurrentPlace.this);

        alertDialogBuilder.setView(promptUserView);

        final EditText userAnswer = (EditText) promptUserView.findViewById(R.id.areaname);

        alertDialogBuilder.setTitle("Input Area name");

        // prompt for username
        alertDialogBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // and display the username on main activity layout
                setAreaColor(userAnswer.getText().toString());
            }
        });

        // all set and time to build and show up!
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void setAreaColor(final String areaname) {
        final String TAG = "Polygon LatLang list";

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityCurrentPlace.this);
        builder.setTitle("Choose Colors")
                .setSingleChoiceItems(colors,-1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedColor = i;
                    }
                })
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),  String.valueOf(selectedColor) + colors[selectedColor] +" Selected\n", Toast.LENGTH_SHORT).show();
                            mMap.clear();
                            LastPolygonLatLangList = drawCapturedPolygon(areaname, selectedColor).getPoints();
                        Log.e(TAG,LastPolygonLatLangList.toString());
                    }
                });
        //Creating dialog box
        AlertDialog dialog  = builder.create();
        dialog.show();
    }
}
