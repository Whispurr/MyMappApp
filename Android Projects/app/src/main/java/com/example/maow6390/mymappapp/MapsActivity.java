package com.example.maow6390.mymappapp;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private EditText editSearch;
    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private boolean isGPS = true;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final float My_LOC_ZOOM_FACTOR = 17.0f;
    private LatLng userLocation;
    private boolean isTrack = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editSearch = (EditText) findViewById(R.id.editText_Search);
        //initializing edit search here, and then loading the map
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void toggleView(View view) {
        int holder = mMap.getMapType();
        // if the map type integer is 1 (aka normal) it switches it to satillete else it turns it to normal (because its satillete)
        if (holder == 1) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void searchPOI(View view) throws IOException {
        Geocoder myGeo = new Geocoder(this.getApplicationContext());
        //using geocoder, searchers for all matching thins to the edit search input, within 5 miles square (aka 0.07 degrees of lattitude/longetude)
        if(mLastLocation != null && editSearch.getText() !=null) {
            List<Address> holder = myGeo.getFromLocationName(editSearch.getText().toString(), 3, mLastLocation.getLatitude() - .07246, mLastLocation.getLongitude() - .07246, mLastLocation.getLatitude() + .07246, mLastLocation.getLongitude() + .07246);
            for (int i = 0; i < holder.size(); i++) {
                LatLng poi = new LatLng(holder.get(i).getLatitude(), holder.get(i).getLongitude());
                mMap.addMarker(new MarkerOptions().position(poi).title(holder.get(i).getAddressLine(0)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi, My_LOC_ZOOM_FACTOR));

            }
            Toast.makeText(this.getApplicationContext(), "Search Completed; Markers added", Toast.LENGTH_SHORT).show();
            Log.d("MyMapApp", "searching POI");
        }
    }
    public void clearMarkers(View view){
        Log.d("MyMapApp", "markers cleared");
        mMap.clear();
        //cleared all markers
        LatLng birthplace = new LatLng(39.1836, -96.5717);
        //add back birth marker
        mMap.addMarker(new MarkerOptions().position(birthplace).title("Born here"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //adds the brithplace once the map is done loading (By dropping a marker)
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng birthplace = new LatLng(39.1836, -96.5717);
        mMap.addMarker(new MarkerOptions().position(birthplace).title("Born here"));
        editSearch = (EditText) findViewById(R.id.editText_Search);
    }

    public void getLocation() {
        //tries permissions
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled)
                Log.d("MyMapsApp", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled)
                Log.d("MyMapsApp", "getLocation: Network is enabled");

            if (!isGPSenabled && !isNetworkEnabled)
                Log.d("MyMapsApp", "getLocation: No Provider is enabled");
            else {
                canGetLocation = true;
                if (isGPSenabled) {
                    // does GPS first
                    Log.d("MyMapsApp", "getLocation: GPS enabled - requesting location updates");
                    isGPS = true;
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();

                }
                // does network 2nd
                if (isNetworkEnabled) {
                    Log.d("MyMapsApp", "getLocation: Network enabled - requesting location updates");
                    isGPS = false;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();

                }
            }
        } catch (Exception e) {

            Log.d("My Maps", "Caught an exception in getLocation");

        }
    }

    public void dropMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        if (isGPSenabled) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            Log.d("MapsActivity", "Dropped GPS Marker");
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            Log.d("MapsActivity", "Dropped Network Marker" + location.getAccuracy());
            // if the marker is GPS it sets the color to BLUE, otherwise it set the location to MAGENTA. The bool is flipped each GPS/Network
        }
        mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, My_LOC_ZOOM_FACTOR));
    }



    public void trackMe(View view) {
        //called on button press. Toggles location on if off and off if on
        if (isTrack) {
            isTrack = false;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
        Log.d("MyMapsApp","trackMe Permission failed");
                return;
            }
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);


        }
        else {
            Log.d("MyMapsApp","trackMe calling getLocation");
            getLocation();
            Toast.makeText(this, "tracking", Toast.LENGTH_SHORT).show();
            isTrack = true;
        }
    }

    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            //drops marker at the location, only called if the location is different. Also performs a permissions check first
            dropMarker(location);
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
    // a switch statement/ If GPS is available it uses GPS. If it any other result or the default is returned it logs unavialbe and returns
            switch (status) {

                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp", "Location Provider is available");

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMapsApp", "Location Provider is temp unavailable");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp", "Location Provider is out of service");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                default:
                    Log.d("MyMapsApp", "Default called");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;

            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // if location has changed the marker is dropped (according to network)
            mLastLocation = location;
            dropMarker(location);
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}