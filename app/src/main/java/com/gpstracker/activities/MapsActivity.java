package com.gpstracker.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpstracker.CustomTextView;
import com.gpstracker.MyApplication;
import com.gpstracker.R;
import com.gpstracker.TrackingService;
import com.gpstracker.data_clases.LatLngDatabase;
import com.gpstracker.data_clases.LatLong;
import com.gpstracker.data_clases.RunContentProvider;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by RGarai on 18.8.2016.
 */

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION_CONNECTED = 2;
    // variables

    private String mUsername;

    private GoogleMap mMap;
    private Location mLastLocation;
    private LatLng mLastLatLng;
    private Marker marker;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private double mDistance = 0;
    private TextView distanceTV;
    private TextView distanceTextView;
    private TextView dateOfRunTextView;
    private TextView speedTextView;
    private TextView altitudeTextView;
    private TextView timestampTextView;
    private StorageReference spaceRef;
    private String mySringOfLatLongFromFirebaseJson;

    private ResideMenu resideMenu;
    private Intent trackingService;
    List<LatLng> routePoints;

    private float mLastBearing;
    public Chronometer runChronometer;
    public long timestamp;
    Gson myGson;
    boolean firstLocation;

    //snackbar
    private Snackbar snackbar;
    private View snackbarView;
    private TextView snackbarTextView;

    //google analytics
    public Tracker mTracker;
    String category;
    String action;

    //firebase database variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;

    private BroadcastReceiver mTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "onReceive mDownloadDataFinishedReceiver");
            timestamp = intent.getLongExtra("timestamp", 0);
            if (timestampTextView != null) {
                timestampTextView.setText(String.valueOf(timestamp));
            }
            boolean locationIsPrepared = intent.getBooleanExtra("location", false);
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            long date = intent.getLongExtra("date", 0);
            float speed= intent.getFloatExtra("speed", 0.0f);
            double altitude = intent.getDoubleExtra("altitude", 0);
            float bearing = intent.getFloatExtra("bearing", 0.0f);
            mDistance = intent.getDoubleExtra("distance", 0);
            String route = intent.getStringExtra("route");

            //for arraylist of latlng json is send by broadcast
            Type listOfMyLatLngObjects = new TypeToken<List<LatLng>>() {}.getType();
            routePoints= myGson.fromJson(route, listOfMyLatLngObjects);



            if (locationIsPrepared) {
                if (firstLocation ) {
                    firstLocation = false;
                    findAndTrackMe();
                }
                updateMap(latitude, longitude, date, timestamp, speed, altitude, bearing, mDistance, routePoints);
            }
//                if (distanceTextView != null) {
//                    distanceTextView.setText(String.valueOf(timestamp));
//                }
//
//                if (dateOfRunTextView != null) {
//                    dateOfRunTextView.setText(String.valueOf(timestamp));
//                }
//                dateOfRunTextView = (TextView) findViewById(R.id.myDateOfRun);
//                speedTextView = (TextView) findViewById(R.id.myVelocityOfRun);
//                altitudeTextView = (TextView) findViewById(R.id.myAltitudeOfRun);
//                timestampTextView = (TextView) findViewById(R.id.myTraveledTime);
//
//            }
        }
    };

    CustomTextView buttonStopTracking;
    CustomTextView buttonRestartTracking;
    CustomTextView buttonGoBack;

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if screens goes off still my app will measure and track
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "my maps still works");
        wl.acquire();
        //..app will stay running during this section..

        setContentView(R.layout.activity_maps);

        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        myGson = new Gson();
        firstLocation = true;

        //snackbar
        mySnackbar();

        //ACTIVITY BAR OR TOOL BAR or action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        //myToolbar.setVisibility(View.GONE);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_action_ic_menu_orange_24dp);

        //toolbar set color for text
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(myToolbar);

        // added options to open the reside menu by clicking on hamburger icon
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("nav button", "navigation clicked");
                // google analytics hit
                category = "NavigationHamburgerClick";
                action = "HamburgerToolbarAction";

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(category)
                        .setAction(action)
                        .build());
                //reside menu
                if (resideMenu != null) {
                    resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(TrackingService.ACTION_TICK);
        LocalBroadcastManager.getInstance(this).registerReceiver(mTickReceiver, filter);

        //ResideMenu
        setUpResideMenu();

        buttonStopTracking = (CustomTextView) findViewById(R.id.button_stop_tracking);
        buttonStopTracking.setOnClickListener(stopTracking);
//
//        buttonRestartTracking = (Button) findViewById(R.id.button_restart_tracking);
//        buttonRestartTracking.setOnClickListener(restartTracking);

//        buttonGoBack = (Button) findViewById(R.id.button_go_back);
//        buttonGoBack.setOnClickListener(goBack);
//
//        // output on screen the gathered info, default into text view
        distanceTextView = (TextView) findViewById(R.id.myTrackedDistance);
        dateOfRunTextView = (TextView) findViewById(R.id.myDateOfRun);
        speedTextView = (TextView) findViewById(R.id.myVelocityOfRun);
        altitudeTextView = (TextView) findViewById(R.id.myAltitudeOfRun);
        timestampTextView = (TextView) findViewById(R.id.myTraveledTime);


//        runChronometer = (Chronometer) findViewById(R.id.myTraveledTime);
//        runChronometer.start();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //firebase implementation
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://gpstracker-646ed.appspot.com");
        spaceRef = storageRef.child("myDataBaseOfRuns.txt");

        wl.release();

    }

    //onResume
    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "MapsActivity");
        mTracker.setScreenName("Image~" + "MapsActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if (!isAppServiceRunning(TrackingService.class)) {
            startService();
        }

//        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
//
//            buildGoogleApiClient();
//            mGoogleApiClient.connect();
//
//        }

    }

    //ResideMenu
    private void setUpResideMenu() {
        // attach to current activity;
        resideMenu = new ResideMenu(this);

        //set one static background
        resideMenu.setBackground(R.drawable.background8);


        resideMenu.attachToActivity(this);

        //set size of screen when menu is opened
        resideMenu.setScaleValue(0.7f);

        //disabling the right swipe gesture (from right to left side menu opening)
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        //ignore map for swipe and opening menu
        resideMenu.addIgnoredView(findViewById(R.id.map));

        //3d rotation
        resideMenu.setUse3D(true);

        // create menu items;
        //menu as it should be on deploy

        String titles[] = { getString(R.string.dashboard), getString(R.string.settings)};
        int icon[] = {R.drawable.run1, R.drawable.run1};

        for (int i = 0; i < titles.length; i++) {
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            final int position = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    category = "ResideMenuClick";
                    action = "";
                    switch (position) {
                        case 0: {
                            action = "DashboardAction";
                            Intent intent = new Intent(MapsActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                        break;

                        case 1: {
                            action = "SettingsAction";
                            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                        break;
                    }
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .build());
                }
            });
            resideMenu.addMenuItem(item, ResideMenu.DIRECTION_LEFT); // or  ResideMenu.DIRECTION_RIGHT

        }

    }


    /**
     * Using: isAppServiceRunning(MyService.class)
     *
     * @param serviceClass tested service class
     * @return is service running
     */
    public boolean isAppServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.i("valuesOfService", service.service.getClassName());

            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//    }

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
    }


    private void setUpMap() {
    }

//    @Override
//    public void onConnected(Bundle bundle) {
//
//        //set the interval of gathering the location data from gps location
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(200);
//        mLocationRequest.setFastestInterval(100);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        //mLocationRequest.setSmallestDisplacement(0.1F);
//
//        //for android 6 and newer
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_LOCATION_CONNECTED);
//
//
//        } else {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        }
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }

    // Method to start the service
    public void startService() {
        //reciever is not used anymore since version 1.4.5
//        MyResultReceiver theReceiver = new MyResultReceiver(new Handler());
//        theReceiver.setParentContext(this);
        trackingService = new Intent(this, TrackingService.class);
//        trackingService.putExtra("rec", theReceiver);
        startService(trackingService);
    }

    public int daylightSavinsResulter() {
        //for determination of daylight saving in specific timezones
        TimeZone tz = TimeZone.getDefault();
        boolean inDs = tz.inDaylightTime(new Date());
        Date now = new Date();
        int offsetFromUtc;
        if (inDs == true) {
            offsetFromUtc = tz.getOffset(now.getTime()) - 3600000;
        } else {
            offsetFromUtc = tz.getOffset(now.getTime()) - 7200000;
        }
        return offsetFromUtc;
    }

    //todo tu niekde vyrob dvojice rychlost v case plus nadmorsku vysku v case a storni to do db
    public void updateMap(double latitude, double longitude, long date, long timestamp, float speed, double altitude, float bearing, double distance, List<LatLng> routePoints ){
        // google analytics hit
        category = "UpdatingMap";
        action = "Running";

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());

        //distance
        distanceTextView.setText(String.format("%.2f m", (float) distance));

        //cas behu
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date mdate = new Date(timestamp - daylightSavinsResulter());
        String formattedTimestamp = format.format(mdate);
        timestampTextView.setText(formattedTimestamp);

        //datum behu
         format = new SimpleDateFormat("dd/MM/yyyy");
         mdate = new Date(date);
        String formattedDate = format.format(mdate);
        dateOfRunTextView.setText(formattedDate);

        //speed
        speedTextView.setText(String.format("%.2f m/s", (float) speed));
        //altitude
        altitudeTextView.setText(String.format("%.2f m", (float) altitude));

        //draw line after tracked trail
        trailLineDraw(routePoints);

        //remove previous current location Marker
        if (marker != null) {
            marker.remove();
        }

        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title("My Location").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));

    }

//    public void onLocationChanged(double latitude, double longitude, long time, float speed, double altitude, float bearing) {
//        if (mLastLatLng != null) {
//            LatLng aCoordinates = new LatLng(latitude, longitude);
//            LatLng bCoordinates = mLastLatLng;
//            double offset2 = CalculationByDistance(aCoordinates, bCoordinates) * 1000;
//
//
////zisti co je offset2 urgent
//            if ((offset2 > 0.2)
//                // && (Math.abs(location.getBearing() - mLastBearing) > 1.0f)
//                    ) {
//
//                mDistance = mDistance + offset2;
//                // long to seconds how to location.getTime
//                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//                Date date = new Date(time);
//                String formattedDate = format.format(date);
//
//
////set text into table of text view
//                if (speed > 0f) {
//                    distanceTextView.setText(String.format("%.2f m", (float) mDistance));
//                } else {
//                    mDistance = 0;
//                    distanceTextView.setText(String.format("%.2f m", (float) mDistance));
//                }
//                dateOfRunTextView.setText(formattedDate);
//                speedTextView.setText(String.format("%.2f m/s", (float) speed));
//                altitudeTextView.setText(String.format("%.2f m", (float) altitude));
//
//
////                travelledTimeTextView.setText(String.format("Track length: %.2f m \n Velocity: %.2f m/s " +
////                                "\n Accuracy: %.2f \n Altitude: %.2f m \n Time: %s seconds",
////                        (float) mDistance, location.getSpeed(), location.getAccuracy(), location.getAltitude(), formatted
////                        )
////                );
//
// //               routePoints.add(new LatLng(latitude, longitude));
//
//
//                //remove previous current location Marker
//                if (marker != null) {
//                    marker.remove();
//                }
//
//                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
//                        .title("My Location").icon(BitmapDescriptorFactory
//                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//                // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 8));
//
//
//            }
//
//        }
//        //mLastLocation = location;
//        mLastLatLng = new LatLng(latitude, longitude);
//        mLastBearing = bearing;
//
//        //draw line after tracked trail
////        trailLineDraw();
//    }

//    @Override
//    public void onLocationChanged(Location location) {
//        if (mLastLocation != null) {
//            LatLng aCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
//            LatLng bCoordinates = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            double offset2 = CalculationByDistance(aCoordinates, bCoordinates) * 1000;
//
//
////zisti co je offset2 urgent
//            if ((offset2 > 0.2)
//                // && (Math.abs(location.getBearing() - mLastBearing) > 1.0f)
//                    ) {
//
//                mDistance = mDistance + offset2;
//                // long to seconds how to location.getTime
//                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//                Date date = new Date(location.getTime());
//                String formattedDate = format.format(date);
//
//
////set text into table of text view
//                if (location.getSpeed() > 0f) {
//                    distanceTextView.setText(String.format("%.2f m", (float) mDistance));
//                } else {
//                    mDistance = 0;
//                    distanceTextView.setText(String.format("%.2f m", (float) mDistance));
//                }
//                dateOfRunTextView.setText(formattedDate);
//                speedTextView.setText(String.format("%.2f m/s", (float) location.getSpeed()));
//                altitudeTextView.setText(String.format("%.2f m", (float) location.getAltitude()));
//
//
////                travelledTimeTextView.setText(String.format("Track length: %.2f m \n Velocity: %.2f m/s " +
////                                "\n Accuracy: %.2f \n Altitude: %.2f m \n Time: %s seconds",
////                        (float) mDistance, location.getSpeed(), location.getAccuracy(), location.getAltitude(), formatted
////                        )
////                );
//
//                routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
//
//
//                //remove previous current location Marker
//                if (marker != null) {
//                    marker.remove();
//                }
//
//                double dLatitude = mLastLocation.getLatitude();
//                double dLongitude = mLastLocation.getLongitude();
//                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(dLatitude, dLongitude))
//                        .title("My Location").icon(BitmapDescriptorFactory
//                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//                // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 8));
//
//
//            }
//
//        }
//        mLastLocation = location;
//        mLastBearing = location.getBearing();
//
//        //draw line after tracked trail
//        trailLineDraw();
//    }


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
        //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Location location = getMyLocation();

        //update location
        //check this http://stackoverflow.com/questions/15997079/getlastknownlocation-always-return-null-after-i-re-install-the-apk-file-via-ecli
//        while ( location == null) {
//            waitUntil++;
//            location = getMyLocation();
//            if (waitUntil > 10000) {
//                break;
//
//            } else {

        if (location == null) {

            //here I need to wait until location found lat long data

        } else {
            zoomAndMoveCamera();
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
//
//                    CameraPosition cameraPosition = new CameraPosition.Builder()
//                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
//                            .zoom(17)                   // Sets the zoom
//                            .bearing(90)                // Sets the orientation of the camera to east
//                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                            .build();             // Creates a CameraPosition from the builder
//                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


        //draw line after tracked trail - this method is used in method OnLocationChange
        //    trailLineDraw();
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

    //draw line after tracked trail
    public void trailLineDraw(List<LatLng> routePoints) {
        Polyline route = mMap.addPolyline(new PolylineOptions()
                .width(10.0f)
                .color(0xff0000ff)
                .geodesic(true)
                .zIndex(1.0f));
        route.setPoints(routePoints);
    }

    View.OnClickListener stopTracking = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // google analytics hit
            category = "ButtonClick";
            action = "Stop&Store&GoBackAction";

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
            //stoping the service
            stopService(new Intent(MapsActivity.this, TrackingService.class));
            //Toast.makeText(MapsActivity.this, "Tracking stopped! =)", Toast.LENGTH_LONG).show();

            //snackbar
            snackbarTextView.setText(getString(R.string.tracking_stopped));
            snackbar.show();



//            runChronometer.stop();
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);

            //storing my data into local and also firebase database
            ContentValues runValues = new ContentValues();

            List<LatLong> mySimplifiedRoutePoints = new ArrayList<>();
            for (LatLng a : routePoints) {
                mySimplifiedRoutePoints.add(new LatLong((float) a.latitude, (float) a.longitude));
            }
            Gson myGson = new Gson();
            Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>() {
            }.getType();
            String myJsonSringOfLatLong = myGson.toJson(mySimplifiedRoutePoints, listOfMyLatLongObjects);

            //for determination of daylight saving in specific timezones
            TimeZone tz = TimeZone.getDefault();
            boolean inDs = tz.inDaylightTime(new Date());
            Date now = new Date();
            int offsetFromUtc;
            if (inDs == true) {
                offsetFromUtc = tz.getOffset(now.getTime()) - 3600000;
            } else {
                offsetFromUtc = tz.getOffset(now.getTime()) - 7200000;
            }

            //for calculation of elapsed chronometer time and date of beginning the run
//            long timeElapsed = SystemClock.elapsedRealtime() - runChronometer.getBase() - offsetFromUtc;
            long timeElapsed = timestamp - offsetFromUtc;
            long timeNow = System.currentTimeMillis();
            long dateToDatabase = timeNow - timeElapsed;
            Log.i("maptimeElapsed", "elapsed time is > " + timeElapsed + " " + " timeNow is > " + timeNow + "offset > " + offsetFromUtc);
            Log.i("storedDistance", "travelled distance stored in database > " + mDistance);

            //inserting data into mobile phone local database
            runValues.put(LatLngDatabase.COLUMN_LATLONG, myJsonSringOfLatLong);
            runValues.put(LatLngDatabase.COLUMN_LENGTH, mDistance / 1000);
            runValues.put(LatLngDatabase.COLUMN_DATE, dateToDatabase);
            runValues.put(LatLngDatabase.COLUMN_TIME, timeElapsed);

            getContentResolver().insert(RunContentProvider.CONTENT_URI, runValues);

            //upload data into firebase database
            mDatabase = FirebaseDatabase.getInstance();
            myRef = mDatabase.getReference();
            LatLngDatabase localDatabase = new LatLngDatabase(MapsActivity.this);
            long myMaxID = localDatabase.getMaxId();



            SharedPreferences prefs = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            mUsername = prefs.getString("username", "");
            myRef.child(mUsername).child("runs").child(String.valueOf(myMaxID + 1)).child("ID").setValue(myMaxID + 1);
            myRef.child(mUsername).child("runs").child(String.valueOf(myMaxID + 1)).child("DATE").setValue(dateToDatabase);
            myRef.child(mUsername).child("runs").child(String.valueOf(myMaxID + 1)).child("LENGTH").setValue(mDistance/1000);
            myRef.child(mUsername).child("runs").child(String.valueOf(myMaxID + 1)).child("TIME").setValue(timeElapsed);
            myRef.child(mUsername).child("runs").child(String.valueOf(myMaxID + 1)).child("LATLONGDATA").setValue(myJsonSringOfLatLong);


            //latlong
//            //upload stream of data to firebase storage
//                List<MyRun> allMyRuns = new ArrayList<>();
//                Cursor myCursor = getContentResolver().query(RunContentProvider.CONTENT_URI, null, null, null, null);
//                if (myCursor != null) {
//                    if (myCursor.moveToFirst()) {
//                        do {
//                            Date date = new Date(myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_DATE)));
//                            int id = myCursor.getInt(myCursor.getColumnIndex(LatLngDatabase.COLUMN_ID));
//                            float length = myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH));
//                            long timeMili = myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_TIME));
//                            String jsonOfRunPath = myCursor.getString(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LATLONG));
//
//                            mySimplifiedRoutePoints = myGson.fromJson(jsonOfRunPath, listOfMyLatLongObjects);
//
//                            MyRun run = new MyRun(date, id, length, mySimplifiedRoutePoints, timeMili);
//                            allMyRuns.add(run);
//                        } while (myCursor.moveToNext());
//                    }
//                    myCursor.close();
//                }
//                Type listOfMyRunObjects = new TypeToken<List<MyRun>>() {
//                }.getType();
//                String myLocalDatabase = myGson.toJson(allMyRuns, listOfMyRunObjects);
//                InputStream stream = new ByteArrayInputStream(myLocalDatabase.getBytes());
//
//                UploadTask uploadTask = spaceRef.putStream(stream);
//                uploadTask.addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Handle unsuccessful uploads
//                    }
//                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                    }
//                });

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //wait for switching the intent
                    startActivity(new Intent(MapsActivity.this, DashboardActivity.class));
                    MapsActivity.this.finish();                              }
            }, 1000);


        }
    };
//      deprecated method for restarting
//    View.OnClickListener restartTracking = new View.OnClickListener() {
//        @SuppressWarnings("MissingPermission")
//        @Override
//        public void onClick(View view) {
//            Toast.makeText(MapsActivity.this, "Tracking started! =)", Toast.LENGTH_LONG).show();
//            runChronometer.stop();
//            runChronometer.setBase(SystemClock.elapsedRealtime());
//            runChronometer.start();
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
//
//            // startActivity(new Intent(MapsActivity.this, DashboardActivity.class));
//        }
//    };

//    View.OnClickListener goBack = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            Toast.makeText(MapsActivity.this, "Loading data! =)", Toast.LENGTH_LONG).show();
//            startActivity(new Intent(MapsActivity.this, DashboardActivity.class));
//        }
//    };
//

    public void zoomAndMoveCamera() {
        if (mMap != null && routePoints.size() > 0) {
            //for (int routePointsPosition = 0, routePointsPosition < routePoints.size(), routePointsPosition++)
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

    public void downloadFile() {

        final long ONE_MEGABYTE = 1024 * 1024;
        spaceRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                // convert byte to string
                for (int i = 0; i < bytes.length; i++) {
                    mySringOfLatLongFromFirebaseJson += (char) bytes[i];
                    Gson myGson = new Gson();
                    Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>() {
                    }.getType();
                    List<LatLong> mySimplifiedRoutePoints = myGson.fromJson(mySringOfLatLongFromFirebaseJson, listOfMyLatLongObjects);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    //snackbar method
    public void mySnackbar(){
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.snackbar_default), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(MapsActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);
    }


}

