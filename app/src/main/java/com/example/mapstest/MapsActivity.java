package com.example.mapstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.example.mapstest.directionhelpers.FetchURL;
import com.example.mapstest.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Random;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, TaskLoadedCallback {

    private GoogleMap mMap;
    double latitude;
    double longitude;
    double distance;
    int maxDistance = 1000;
    boolean bool = true;
    LocationManager your_location;
    LatLng point_rnd;
    LatLng point_buffer = null;
    Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        your_location = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            your_location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 0, this);
    }

    @Override
    public void onLocationChanged(Location location){
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if(bool) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(49.7, 23.9)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
            bool = false;
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("you are her (inside black loop, maybe)"));
        mMap.addCircle(new CircleOptions().center(new LatLng(latitude, longitude)).radius(25));

        if (point_buffer == null) {
            getRandomLocation(new LatLng(latitude, longitude), maxDistance);
            mMap.addMarker(new MarkerOptions().position(point_rnd).title(String.valueOf(distance)));
            point_buffer = point_rnd;
        } else {

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(point_buffer);
            markerOptions.title("Distance: " + (int) Math.round(distance) + "\n"
                    + "Burned cal. for this run: " + (int) (distance * 0.5 * 70 * 0.001));
            markerOptions.snippet("Destination");

            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
            mMap.setInfoWindowAdapter(customInfoWindow);

            Marker locationMarker = mMap.addMarker(markerOptions);
            locationMarker.setDraggable(false);
            locationMarker.showInfoWindow();
        }
        new FetchURL(MapsActivity.this).execute(getUrl(new LatLng(latitude, longitude), point_buffer, "driving"), "driving");

    }

    public void getRandomLocation(LatLng point, int radius) {
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        double x0 = point.latitude;
        double y0 = point.longitude;

        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;
        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLatitude = new_x + x0;
        double foundLongitude = y + y0;
        LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
        point_rnd = randomLatLng;
        Location l1 = new Location("");
        l1.setLatitude(randomLatLng.latitude);
        l1.setLongitude(randomLatLng.longitude);
        distance = l1.distanceTo(myLocation);
}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object...values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}

