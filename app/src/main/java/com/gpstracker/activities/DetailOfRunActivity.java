package com.gpstracker.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpstracker.CustomTextView;
import com.gpstracker.MyApplication;
import com.gpstracker.R;
import com.gpstracker.data_clases.LatLngDatabase;
import com.gpstracker.data_clases.LatLong;
import com.gpstracker.data_clases.RunContentProvider;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RGarai on 13.9.2016.
 */

public class DetailOfRunActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION_CONNECTED = 2;
    // variables
    private GoogleMap mMap;
    List<LatLng> routePoints = new ArrayList<>();
    private Location mLastLocation;
    private Marker marker;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private double mDistance = 0;

    private TextView distanceTextView;
    private TextView dateOfRunTextView;
    private TextView travelledTimeTextView;

    //snackbar
    private Snackbar snackbar;
    private View snackbarView;
    private TextView snackbarTextView;

    //firebase storage variables
    private StorageReference spaceRef;
    private String mySringOfLatLongFromFirebaseJson = "";

    private List<LatLong> mySimplifiedRoutePoints;

    private float mLastBearing;

    //google analytics
    public Tracker mTracker;
    String category;
    String action;

    public Chronometer runChronometer;

    CustomTextView buttonGoBack;

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if screens goes off still my app will measure and track
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "my maps still works");
        wl.acquire();
        //..screen will stay on during this section..

        setContentView(R.layout.activity_detailofrun);

        //snackbar
        mySnackbar();

        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        buttonGoBack = (CustomTextView) findViewById(R.id.button_go_back);
        buttonGoBack.setOnClickListener(goBack);

        // output on screen the gathered info, default into text view
        distanceTextView = (TextView) findViewById(R.id.myTrackedDistance);
        dateOfRunTextView = (TextView) findViewById(R.id.myDateOfRun);
        travelledTimeTextView = (TextView) findViewById(R.id.myTraveledTime);

        //firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://gpstracker-646ed.appspot.com");
        spaceRef = storageRef.child("myDataBaseOfRuns.txt");

        //set text into table of text views
        Cursor myCursor;
        Bundle bundle = getIntent().getExtras();
        int myRunIdFromIntentBundle = bundle.getInt("requestedId");
        myCursor = getContentResolver().query(Uri.parse(RunContentProvider.URL + "/" + myRunIdFromIntentBundle), null, null, null, null);
        if (myCursor != null ) {
            if (myCursor.moveToFirst()) {
                float length = myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH));
                distanceTextView.setText(String.format("%.2f m", length*1000));

                long myDate = myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_DATE));
                DateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formatedDate = myFormat.format(myDate);
                dateOfRunTextView.setText(formatedDate);

                long myRunTimeLapse = myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_TIME));
                DateFormat myTimeFormat = new SimpleDateFormat("HH:mm:ss");
                String formatedTime = myTimeFormat.format(myRunTimeLapse);
                travelledTimeTextView.setText(formatedTime);

                //track
                String myJsonSringOfLatLong = myCursor.getString(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LATLONG));
                Gson myGson = new Gson();
                Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>(){}.getType();
                mySimplifiedRoutePoints = myGson.fromJson(myJsonSringOfLatLong, listOfMyLatLongObjects);

                zoomAndMoveCamera();
                for (LatLong a : mySimplifiedRoutePoints) {
                    routePoints.add(new LatLng(a.getLatitude(), a.getLongitude() ));
                }

                trailLineDraw();
                zoomAndMoveCamera();

            }
            myCursor.close();
        }

     //   downloadFile();    //this method belong to the firebase storage
     //   Log.i("myListOfDownloadedItems", mySringOfLatLongFromFirebaseJson);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        wl.release();
    }

    //onResume
    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "DetailOfRunActivity");
        mTracker.setScreenName("Image~" + "DetailOfRunActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        //google maps
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    //onPause
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    //google api client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i("Map", "onMapReady: ");
        zoomAndMoveCamera();
        trailLineDraw();
    }


    private void setUpMap() {
    }

    @Override
    public void onConnected(Bundle bundle) {

        //set the interval of gathering the location data from gps location
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(200);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //mLocationRequest.setSmallestDisplacement(0.1F);

        //for android 6 and newer
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION_CONNECTED);


        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        mLastBearing = location.getBearing();
    }

    //   @SuppressWarnings("MissingPermission")
    private void findAndTrackMe() {
        int waitUntil = 0;

        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Integer.parseInt(LOCATION_SERVICE));
            }
            return;
        }

        Location location = getMyLocation();

        if (location == null) {

            //here I need to wait until location found lat long data

        }else {
        }
    }

    @SuppressWarnings("MissingPermission")
    private Location getMyLocation() {
        // Get location from GPS if it's available
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Location wasn't found, check the next most accurate place for the current location
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // Finds a provider that matches the criteria
            String provider = lm.getBestProvider(criteria, true);
            // Use the provider to get the last known location
            myLocation = lm.getLastKnownLocation(provider);
        }

        return myLocation;
    }

//  maybe I will use something like this when redrawing the points from database
//    //draw line after tracked trail
    public void trailLineDraw() {
        if (mMap != null) {
            Polyline route = mMap.addPolyline(new PolylineOptions()
                    .width(10.0f)
                    .color(0xff0000ff)
                    .geodesic(true)
                    .zIndex(1.0f));
            route.setPoints(routePoints);
        }
    }

    public void zoomAndMoveCamera() {
        if (mMap != null && routePoints.size() > 0) {
            LatLng locationLatLng = routePoints.get(0);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 17));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(locationLatLng)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(180)                // Sets the orientation of the camera to east
                    .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                    .build();             // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    View.OnClickListener goBack = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // google analytics hit
            category = "ButtonClick";
            action = "GoBackAction";

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());

            //Toast.makeText(DetailOfRunActivity.this, "Loading data! =)", Toast.LENGTH_LONG).show();
            snackbarTextView.setText(getString(R.string.loading_data));
            snackbar.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //wait for switching the intent
                    startActivity(new Intent(DetailOfRunActivity.this, DashboardActivity.class));                              }
            }, 700);


        }
    };

    public void downloadFile(){

        final long ONE_MEGABYTE = 1024 * 1024;
        spaceRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                // convert byte to string
                for (int i = 0; i < bytes.length; i++) {
                    mySringOfLatLongFromFirebaseJson += (char) bytes[i];
                    Gson myGson = new Gson();
                    Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>(){}.getType();
                    mySimplifiedRoutePoints = myGson.fromJson(mySringOfLatLongFromFirebaseJson, listOfMyLatLongObjects);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    //snackbar method
    public void mySnackbar(){
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.snackbar_default), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(DetailOfRunActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);
    }
}

