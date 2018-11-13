package com.example.currentplacedetailsonmap;

import android.graphics.Color;

/**
 * A class that manages the public static final value
 * related to MapsActivityCurrentPlace.java.
 */
public class MapsConstants {

    // Default values of shapes.
    public static final int DEFAULT_POLYLINE_COLOR = Color.BLACK;
    public static final int DEFAULT_POLYGON_STROKE_COLOR = Color.RED;
    public static final int DEFAULT_POLYGON_FILL_COLOR = Color.RED;
    public static final int DEFAULT_STROKE_WIDTH = 5;

    // Default time interval and distance for requestLocationUpdates().
    public static final long TIME_INTERVAL_FOR_UPDATING_LOCATION = 1000; // ms
    public static final float LEAST_DISTANCE_FOR_UPDATING_LOCATION = 1; // meter

    // Default values of camera.
    public static final int DEFAULT_ZOOM = 15;

}
