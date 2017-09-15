package com.gpstracker;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrackingService extends Service implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final String ACTION_TICK = "tick";
    //Variables
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    public long timestamp;
    public Timer myTimer;

    public Location mLastLocation;
    private double mDistance;

    List<LatLng> routePoints;
//    List<VelTime> velocityInTime;
//    List<AltTime> altitudeInTime;

    Gson myGson;

    //Constructor

//    public TrackingService() {
//        super(TrackingService.class.getSimpleName());
//    }


    //Methods

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myGson = new Gson();
        mDistance = 0;
        timestamp = 0;
        routePoints = new ArrayList<>();
        mLastLocation = null;
        myTimer = new Timer();

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(ACTION_TICK);
                intent.putExtra("timestamp", timestamp);
                intent.putExtra("location", mLastLocation != null);
                if (mLastLocation != null) {
                    intent.putExtra("latitude", mLastLocation.getLatitude());
                    intent.putExtra("longitude", mLastLocation.getLongitude());
                    intent.putExtra("date", mLastLocation.getTime());
                    intent.putExtra("speed", mLastLocation.getSpeed());
                    intent.putExtra("altitude", mLastLocation.getAltitude());
                    intent.putExtra("bearing", mLastLocation.getBearing());
                    intent.putExtra("distance", mDistance);
                    //for arraylist of latlng json is send by broadcast
                    Type listOfMyLatLngObjects = new TypeToken<List<LatLng>>() {}.getType();
                    String myJsonSringOfLatLng = myGson.toJson(routePoints, listOfMyLatLngObjects);
                    intent.putExtra("route", myJsonSringOfLatLng);

                }
                LocalBroadcastManager.getInstance(TrackingService.this).sendBroadcast(intent);
                timestamp += 1000;

            }

        }, 0, 1000);


        Log.i("StartingIntentService", "StartingIntentService");

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }


        return START_NOT_STICKY;
    }

    //    @SuppressWarnings("MissingPermission")
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        resRec =  intent.getParcelableExtra("rec");
//
//
////        runChronometer = new Chronometer(this);
//
//        timestamp = 0;
//        Timer myTimer = new Timer();
//        myTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                    Intent intent = new Intent(ACTION_TICK);
//                    intent.putExtra("timestamp", timestamp);
//                    LocalBroadcastManager.getInstance(TrackingService.this).sendBroadcast(intent);
//                    timestamp += 1000;
//
//            }
//
//        }, 0, 1000);
//
//        Log.i("StartingIntentService", "StartingIntentService");
//
//        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
//
//            buildGoogleApiClient();
//            mGoogleApiClient.connect();
//
//        }
//
//        while (true){
//
//        }
//    }

    //google api client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

//todo preco je toto zakomentovane? ved tu mam pouzivat predsa nejaky bundle
//    @Override
//    public void onLocationChanged(Location location) {
//        mLastLocation = location;
//        Bundle resultBundle = new Bundle();
//        resultBundle.putDouble("latitude", location.getLatitude());
//        resultBundle.putDouble("longitude", location.getLongitude());
//        resultBundle.putLong("time", location.getTime());
//        resultBundle.putFloat("speed", location.getSpeed());
//        resultBundle.putDouble("altitude", location.getAltitude());
//        resultBundle.putFloat("bearing", location.getBearing());
//        resRec.send(12345, resultBundle);
//
//        Log.i("StartingIntentService", "OnLocationChanged");
//
//    }

    //povodny locationchanged overridnuty a z mapsactivity vytiahnuty ten musim upravit

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation != null) {
            LatLng aCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng bCoordinates = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            double offset2 = CalculationByDistance(aCoordinates, bCoordinates) * 1000;


//zisti co je offset2 urgent
            if ( (offset2 > 0.2) && (location.getSpeed() > 0f) )
                // && (Math.abs(location.getBearing() - mLastBearing) > 1.0f)
             {
                mDistance = mDistance + offset2;

                //na mape zobrazuj
                // long to seconds how to location.getTime
//                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//                Date date = new Date(location.getTime());
//                String formattedDate = format.format(date);

//toto musim riesit v maps activity
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


//                travelledTimeTextView.setText(String.format("Track length: %.2f m \n Velocity: %.2f m/s " +
//                                "\n Accuracy: %.2f \n Altitude: %.2f m \n Time: %s seconds",
//                        (float) mDistance, location.getSpeed(), location.getAccuracy(), location.getAltitude(), formatted
//                        )
//                );

                routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));


//marker budem riesit az na mape
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


            }

        }
        mLastLocation = location;
  //deprecated      mLastBearing = location.getBearing();

        //dories pozdejsie
//        //draw line after tracked trail
//        trailLineDraw();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //set the interval of gathering the location data from gps location
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(200);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //mLocationRequest.setSmallestDisplacement(0.1F);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onDestroy() {
        myTimer.cancel();
        mGoogleApiClient.disconnect();
        super.onDestroy();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // Let it continue running until it is stopped.
//        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
//    }

    //method for spherical calculus
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

}
