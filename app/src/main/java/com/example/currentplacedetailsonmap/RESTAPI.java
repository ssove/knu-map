package com.example.currentplacedetailsonmap;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RESTAPI {
    public static String userName = "userName";


    public static void postPolygonToServer(Polygon polygon) {
        JSONObject requestBody = new JSONObject();
        JSONObject tag = new JSONObject();
        JSONObject l = new JSONObject();
        JSONArray pos = new JSONArray();
        String polygonTag;

        polygonTag = (String) polygon.getTag();
        Log.i("Start posting pg", MapsConstants.devisionLine);
        Log.i("Polygon points", polygon.getPoints().toString());
        Log.i("Polygon name", polygonTag);

        // Construct JSONObject to request to server.
        try {
            tag
                    .put("polygon_name", polygonTag)
                    .put("user_name", userName);

            for (LatLng location : polygon.getPoints()) {
                l
                        .put("latitude", location.latitude)
                        .put("longitude", location.longitude);
                pos
                        .put(l);
            }
            requestBody
                    .put("tag", tag)
                    .put("color", polygon.getFillColor())
                    .put("pos", pos);

        } catch (JSONException e) {
            Log.e("JSONObject : ", "error while constructing polygon JSONObject" + e.getMessage());
        }

        // POST the constructed JSONObject.
        HttpAsyncTask task = new HttpAsyncTask.Builder("POST", "polygons", new TypeToken<ResultBody<Polygon>>() {
        }.getType(),
                new MyCallBack() {
                    @Override
                    public void doTask(Object resultBody) {}})
                .requestBodyJson(requestBody) // POST requires a requestBodyJson.
                .build();

        task.execute();
        Log.i("End posting a polygon", MapsConstants.devisionLine);
    }


    public static void postPolylineToServer(Polyline polyline) {
        JSONObject requestBody = new JSONObject();
        JSONObject tag = new JSONObject();
        JSONObject l = new JSONObject();
        JSONArray pos = new JSONArray();

        Log.i("Start posting pg", MapsConstants.devisionLine);
        Log.i("Polyline points", polyline.getPoints().toString());

        // Construct JSONObject to request to server.
        try {
            tag
                    .put("user_name", userName);

            for (LatLng location : polyline.getPoints()) {
                l
                        .put("latitude", location.latitude)
                        .put("longitude", location.longitude);
                pos
                        .put(l);
            }
            requestBody
                    .put("tag", tag)
                    .put("status", "1")
                    .put("pos", pos);

        } catch (JSONException e) {
            Log.e("postPolylineToServer()", "error while constructing polyline JSONObject" + e.getMessage());
        }

        // POST the constructed JSONObject.
        HttpAsyncTask task = new HttpAsyncTask.Builder("POST", "polylines", new TypeToken<ResultBody<Polyline>>() {
        }.getType(),
                new MyCallBack() {
                    @Override
                    public void doTask(Object resultBody) {}
                })
                .requestBodyJson(requestBody) // POST requires a requestBodyJson.
                .build();

        task.execute();
        Log.i("End posting a polyline", MapsConstants.devisionLine);
    }

    public static ArrayList<Polygon> getPolygonsFromServer() {
        ArrayList<Polygon> polygonList = new ArrayList<>();

        HttpAsyncTask task = new HttpAsyncTask.Builder("GET", "polygons", new TypeToken<ResultBody<Polygon>>() {
        }.getType(),
                new MyCallBack() {
                    @Override
                    public void doTask(Object resultBody) {}
                })
                .build();

        task.execute();

        return polygonList;
    }
}
