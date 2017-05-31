package com.example.maow6390.mymappapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;

    public void trackMe() {
        //toggles tracking, linked to button
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled)
                Log.d("MyMaps", "getLocation: GPS is enabled");
            //get Netowrk statu
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isNetworkEnabled)
                Log.d("MyMaps", "getLocation : network is enabled");

            if (!isNetworkEnabled || !isGPSEnabled) {
                Log.d("MyMaps", "getLocation : no provide enabled");
            } else {
                canGetLocation = true;
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS Enabled - reuqesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMaps", "getLocation: Network GPS update request success");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                }
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network Enabled - reuqesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMaps", "getLocation: Network update request success");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }
            }
        } catch (Exception e) {
            Log.d("MyMaps", "Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
    }


    public void toggleView(View view) {
        int tmp = mMap.getMapType();
        if (tmp == 2) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng birth = new LatLng(39.1836, -96.5717);
        mMap.addMarker(new MarkerOptions().position(birth).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birth));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "failed Permission check 1");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, 2});
        }

    }


    LocationListener locationListenerGps = new LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp", "OnLocationChanged");
            mLastLocation = location;
            if (mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
            }
            //drop a marker create a method called drop marker
            //dissable nettwork stuff
            //Place current location marker
            dropMarker(location);

            //stop location updates
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            }
            Toast.makeText(getApplicationContext(), "Using Network", Toast.LENGTH_SHORT);
        }

        public void dropMarker (Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mCurrLocationMarker = mMap.addMarker(markerOptions);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //setup a switch statement on status
            //case:locationProvider.AVAILABLE = output a message to log.d
            //case: locationProvider.OUT_OF_SERVREICE -> request from network provider
            // case: locationProvider.TEMPORARILY UNAVIALABLE - reuqest from network provider
            //case: default - request network provider
        }

        public void getOnProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }
    };
    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            //output
            //drop maker
            //reluanch request for network location updates
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output message in logd
        }

        public void getOnProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {


        }


    };
}