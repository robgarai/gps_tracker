package com.gpstracker.data_clases;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by RGarai on 29/04/16.
 */

/*
 * Class that defines how each run stores data and which data are stored
 */
public class MyRun {
    //variables
    private int id;         //id of the one run
    private Date date;      //date of the begining of one run
    private float length;   //length of one run
    private long time;     //time of one run
    private float speed;
    private float altitude;
    private List<LatLong> oneRunTrackData = Arrays.asList(); //latitude and longitude coordinates for repeating and generating the track

    //constructors
    public MyRun(Date date, float length, long time) {
        this.date = date;
        this.length = length;
        this.time = time;
    }

    public MyRun(Date date, int id, float length, long time) {
        this.date = date;
        this.id = id;
        this.length = length;
        this.time = time;
    }

    public MyRun(Date date, int id, float length, List<LatLong> oneRunData, long time) {
        this.date = date;
        this.id = id;
        this.length = length;
        this.oneRunTrackData = oneRunData;
        this.time = time;
    }

    public MyRun(int id, Date date, float length, long time, float speed, float altitude, List<LatLong> oneRunTrackData) {
        this.id = id;
        this.date = date;
        this.length = length;
        this.time = time;
        this.speed = speed;
        this.altitude = altitude;
        this.oneRunTrackData = oneRunTrackData;
    }

    //getters setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<LatLong> getOneRunTrackData() {
        return oneRunTrackData;
    }

    public void setOneRunTrackData(List<LatLong> oneRunTrackData) {
        this.oneRunTrackData = oneRunTrackData;
    }

    //new data must be implemented and used in graph in detail of run
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }
}
