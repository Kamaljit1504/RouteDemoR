package com.example.routedemor;


import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private final int Request_code = 1;

    // variables to get the user location
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    //latitude, longitude
    double latitude;
    double longitude;
    double dest_Lat, dest_lng;
    final int radius = 1500;

    public static boolean directionRequested;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMap();
        getUserLocation();
        if(!checkPermission())
            requestPermission();
        else
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }
    private void initMap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getUserLocation()
    {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        setHomeMarker();
    }

    private void setHomeMarker()
    {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations())
                {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    latitude = userLocation.latitude;
                    longitude = userLocation.longitude;

                    CameraPosition cameraPosition = CameraPosition.builder()
                            .target(userLocation)
                            .zoom(15)
                            .bearing(0)
                            .tilt(45)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mMap.addMarker(new MarkerOptions().position(userLocation)
                            .title("Your Location")
                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.marker)));

                }
            }
        };
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId)
    {
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorDrawableResourceId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private boolean checkPermission()
    {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_code);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Request_code)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                setHomeMarker();
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //mMap.setMapStyle(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Location location = new Location("Your Destination");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                dest_Lat = latLng.latitude;
                dest_lng = latLng.longitude;
                //set marker
                setMarker(location);

            }
        });


    }

    public void setMarker(Location location)
    {
        LatLng userLatlng  = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLatlng)
                .title("Your Destination")
                .snippet("on the way")
                .draggable(true)
                 );
    }

    public  void btnClick(View view)
    {
        Object[] dataTransfer;
        String url;

        switch (view.getId())
        {

            case R.id.btn_restaurant:
                //get the url from place api
                 url = getUrl(latitude, longitude, "restaurant");
                dataTransfer = new Object[2];
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                GetNearByPlacesData getNearByPlaceData = new GetNearByPlacesData();
                getNearByPlaceData.execute(dataTransfer);
                Toast.makeText(this,"Restaurants", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_distance:
            {
                url = getDirectionURL();
                dataTransfer = new Object[3];
                dataTransfer[0] = mMap;
                //dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(dest_Lat, dest_lng);

                GetDirectionData getDirectionData = new GetDirectionData();
                //Exceute ASYN
                Toast.makeText(this,"Restaurants", Toast.LENGTH_SHORT).show();
                getDirectionData.execute(dataTransfer);

                if (view.getId() == R.id.btn_direction)
                {
                    directionRequested = true;
                }
                else {
                    directionRequested = false;
                }

                break;

            }


        }

    }
    private String getDirectionURL()
    {
        StringBuilder directionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?=AIzaSyDK2Du7rvxW4d4NQmKg8qAyxaZ0dGgaY5");
        directionUrl.append("origin="+ latitude +","+ longitude);
        directionUrl.append("&destination=" + dest_Lat +  "," + dest_lng);
        directionUrl.append("&key" + getString(R.string.api_key));
        return directionUrl.toString();

    }

    private String getUrl(double latitude, double longitude, String nearByPlace)
    {
        StringBuilder placeuUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");
        placeuUrl.append("location=" + latitude + "," + longitude);
        placeuUrl.append("&radius=" + radius);
        placeuUrl.append("&type=" + nearByPlace);
        placeuUrl.append("&key=" + getString(R.string.api_key));


        return placeuUrl.toString();
    }
}
