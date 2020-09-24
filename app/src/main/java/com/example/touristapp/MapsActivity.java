package com.example.touristapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.location.Location;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    FusedLocationProviderClient myFusedLocationProviderClient;
  
    private String myBaseUrl = "https://api.mapbox.com/directions/v5/mapbox/driving/";
    private String myAccessToken = "?access_token=pk.eyJ1IjoiYW5oYnJlbiIsImEiOiJja2R4OWc2dXowbWJhMnlveGlwM3FxNmZoIn0.CZkcNK5sfQ1dm91cGEFCFQ";
    private JsonObjectRequest jsonObjectRequest;
    private GoogleMap myMap;
    private Landmark myLandMark;
    private RequestQueue myQueue;
    private TextToSpeech myTextToSpeech;
    private boolean isTextToSpeech = true;
    private Marker myMarker;
    public String TAG = "AAA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loadData();
        initComponent();
    }

    public void initComponent() {
        myTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                isTextToSpeech = true;
            }
        });
    }

    public String buildDirectionURI(LatLng origin, LatLng destination) {
        String originString = String.valueOf(origin.longitude) + ',' + String.valueOf(origin.latitude);
        String destinationString = String.valueOf(destination.longitude) + ',' + String.valueOf(destination.latitude);
        return myBaseUrl + originString + ';' + destinationString + myAccessToken;
    }

    private void reqDirect(String url) {
        myQueue = Volley.newRequestQueue(this);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray routeArray = response.getJSONArray("routes");
                    JSONObject routeObject = routeArray.getJSONObject(0);
                    ArrayList<LatLng> listPointRoute = decodePolyLines(routeObject.getString("geometry"));
                    drawPolyline(listPointRoute);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.v(TAG, "ERROR: " + e);
                }
            }
        }, new Response.ErrorListener () {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "ERROR: " + error);
            }
        });
        checkInternetPermission();
    }

    public void addReqToQue() {
        myQueue.add(jsonObjectRequest);
    }

    public void checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            addReqToQue();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    1); // 1 = PERMISSIONS_REQUEST_ACCESS_INTERNET
            addReqToQue();
        }
    }

    public void loadData() {
        Intent intent = getIntent();
        myLandMark = new Landmark(intent.getStringExtra("name"),
                intent.getStringExtra("des"),
                intent.getIntExtra("logoID", 0),
                intent.getDoubleExtra("lat", 0),
                intent.getDoubleExtra("long", 0));
    }
    public void drawPolyline(ArrayList<LatLng> points) {
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng src = points.get(i);
            LatLng dest = points.get(i + 1);
            Polyline line = myMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude, dest.longitude)
                    ).color(Color.RED).geodesic(true)
            );
        }
    }

    public ArrayList<LatLng> decodePolyLines(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        // Add a marker in Sydney and move the camera

        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (isTextToSpeech) {
                    myTextToSpeech.speak(myLandMark.getDescription(),
                            TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(getApplicationContext(),
                            myLandMark.getDescription(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
                return false;
            }
        });
        displayMarkers();
    }

    private void displayMarkers() {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), myLandMark.getLogoID());
        bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth()/4, bmp.getHeight()/4, false);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bmp);
        myMarker = myMap.addMarker(new MarkerOptions()
                .position(myLandMark.getLatLng())
                .icon(bitmapDescriptor)
                .title(myLandMark.getName())
                .snippet(myLandMark.getDescription()));
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(myLandMark.getLatLng()) // Sets the center of the map to Mountain View
                .zoom(15)                      // Sets the zoom
                .bearing(90)                   // Sets the orientation of the camera to east
                .tilt(30)                      // Sets the tilt of the camera to 30 degrees
                .build();
        myMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
            myMap.getUiSettings().setMyLocationButtonEnabled(true);
            getDeviceLocation();
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1); // 1 = PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //myMap.setMyLocationEnabled(true);
                    myMap.getUiSettings().setMyLocationButtonEnabled(true);
                    getDeviceLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getDeviceLocation() {
        try {
            Task<Location>[] locationResult = new Task[]{myFusedLocationProviderClient.getLastLocation()};
            locationResult[0].addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            myMap.setMyLocationEnabled(true);
                            LatLng curLocation = new LatLng(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude());
                            myMap.addMarker(new MarkerOptions().position(curLocation).title("You are here"));
                            CameraPosition newCameraPosition = new CameraPosition.Builder()
                                    .target(curLocation) // Sets the center of the map to Mountain View
                                    .zoom(15)                      // Sets the zoom
                                    .bearing(90)                   // Sets the orientation of the camera to east
                                    .tilt(30)                      // Sets the tilt of the camera to 30 degrees
                                    .build();
                            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                            String url = buildDirectionURI(curLocation, myLandMark.getLatLng());
                            reqDirect(url);
                        }
                        else {
                            Log.v(TAG, "ERROR: lastKnownLocation is null" + " line: 282");
                        }
                    } else {
                        Log.v(TAG, "ERROR: isSuccessful() is null" + " line: 285");
                    }
                }
            });
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void onClick_direction(View view) {
        checkPermission();
    }

   
}