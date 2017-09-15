package com.gpstracker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Lenovo on 04.10.2016.
 */

public class MyResultReceiver extends ResultReceiver {

    private Context context = null;

    public void setParentContext (Context context) {
        this.context = context;
    }

    public MyResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult (int resultCode, Bundle resultData) {
//        MapsActivity activity = (MapsActivity) context;
//        double latitude = resultData.getDouble("latitude");
//        double longitude = resultData.getDouble("longitude");
//        long time = resultData.getLong("time");
//        float speed= resultData.getFloat("speed");
//        double altitude = resultData.getDouble("altitude");
//        float bearing = resultData.getFloat("bearing");
//
//        activity.onLocationChanged(latitude, longitude, time, speed, altitude, bearing);
    }
}
