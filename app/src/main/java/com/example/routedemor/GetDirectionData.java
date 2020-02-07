package com.example.routedemor;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class GetDirectionData extends AsyncTask<Object, String, String>
{
    String directionData;
    GoogleMap mMap;
    String url;

    String distance;
    String duration;

    LatLng latLng;


    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        latLng = (LatLng) objects[2];
        FetchUrl fetchUrl = new FetchUrl();
        try
        {
            directionData = fetchUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return directionData;
    }


    @Override
    protected void onPostExecute(String s) {
        HashMap<String, String > distanceData = null;
        DataParser distanceParser = new DataParser();
        distanceData = distanceParser.parseDistance(s);

        distance = distanceData.get("duration");
        duration = distanceData.get("duration");

        mMap.clear();
        // create new marker with new title and Snippet
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Duration : " + duration)
                .snippet("Distance:" + distance);
        mMap.addMarker(options);

        if (MainActivity.directionRequested)
        {
            String[] directionsList;
            DataParser directionParser = new DataParser();
            directionsList = directionParser.parseDirections(s);
            displayDirections(directionsList);


        }

    }
    private void displayDirections(String[] directionsList)
    {
        int count = directionsList.length;
        for(int i= 0; i<count; i++)
        {
            PolylineOptions options = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .addAll(PolyUtil.decode(directionsList[i]));
            mMap.addPolyline(options);

        }

    }


}
