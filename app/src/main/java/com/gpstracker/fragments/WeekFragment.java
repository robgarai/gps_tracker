package com.gpstracker.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpstracker.MyApplication;
import com.gpstracker.R;
import com.gpstracker.adapters.RunsAdapter;
import com.gpstracker.data_clases.LatLngDatabase;
import com.gpstracker.data_clases.LatLong;
import com.gpstracker.data_clases.MyRun;
import com.gpstracker.data_clases.RunContentProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by RGarai on 1.9.2016.
 */
public class WeekFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    public List<PieEntry> entries = new ArrayList<>();

    ListView mRunsListView;
    RunsAdapter mRunsAdapter;
    float sumOfKm = 0f;
    private PieChart mChart;
    private int mPage;

    private CallBacks mAttachedActivity;

    private float weekGoalDistanceKm;
    private float actualWeekDistanceKm;

    //firebase database variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    private String mUsername;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAttachedActivity = (CallBacks) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAttachedActivity = null;
    }

    public static WeekFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_of_dashboard, container, false);


        //week defined
        //milliseconds in day 86400000
        // milliseconds in week 604800000

        LocalDate now = new LocalDate();
        Log.i("timenow", now + "");

        //checkTheWeekNumber();

        LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
        DateTime thisMonday = monday.toDateTimeAtStartOfDay(DateTimeZone.getDefault());
        long secondsAtThisMonday = thisMonday.getMillis();
        Log.i("timemonday", secondsAtThisMonday + "");

        LocalDate sunday = now.withDayOfWeek(DateTimeConstants.SUNDAY);
        DateTime thisSunday = monday.toDateTimeAtStartOfDay(DateTimeZone.getDefault());
        long secondsAtThisSundayMorning = thisSunday.getMillis();
        long secondsAtThisSunday = secondsAtThisSundayMorning + 86400000;
        Log.i("timesunday", secondsAtThisSunday + "");


        // listview
        List<MyRun> myFilteredRuns = new ArrayList<>();
        mRunsListView = (ListView) view.findViewById(R.id.runsListView);
        Cursor myCursor;
        switch (mPage) {
            case 1:
                //myFilteredRuns = RunData.previousRuns;
                myCursor = getContext().getContentResolver().query(RunContentProvider.CONTENT_URI, null, null, null, null);
                if (myCursor != null ) {
                    if (myCursor.moveToFirst()) {
                        do {
                            // list.add(new Product(cursor.getString(0), Integer.parseInt(cursor.getString(1))));
                            Log.i("rundataa", "length " + myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH)));

                            Date date = new Date (myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_DATE)));
                            int id = myCursor.getInt(myCursor.getColumnIndex(LatLngDatabase.COLUMN_ID));
                            float length = myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH));
                            long timeMili = myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_TIME));
                            String jsonOfRunPath = myCursor.getString(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LATLONG));

                            Log.i("myJsonPath", jsonOfRunPath+"" );

                            Gson myGson = new Gson();
                            Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>(){}.getType();
                            List<LatLong> mySimplifiedRoutePoints = myGson.fromJson(jsonOfRunPath, listOfMyLatLongObjects);

                            myFilteredRuns.add(new MyRun(date, id, length, mySimplifiedRoutePoints, timeMili));

                        } while (myCursor.moveToNext());
                    }
                    myCursor.close();
                }

                break;
            case 2:
                myCursor = getContext().getContentResolver().query(RunContentProvider.CONTENT_URI, null, null, null, null);
                if (myCursor != null ) {
                    if (myCursor.moveToFirst()) {
                        do {
                            Log.i("rundataa", "length " + myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH)));

                            Date date = new Date (myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_DATE)));
                            int id = myCursor.getInt(myCursor.getColumnIndex(LatLngDatabase.COLUMN_ID));
                            float length = myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH));
                            long timeMili = myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_TIME));
                            String jsonOfRunPath = myCursor.getString(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LATLONG));

                            Log.i("myJsonPath", jsonOfRunPath+"" );

                            Gson myGson = new Gson();
                            Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>(){}.getType();
                            List<LatLong> mySimplifiedRoutePoints = myGson.fromJson(jsonOfRunPath, listOfMyLatLongObjects);

                            if (date.getTime() > secondsAtThisMonday) {
                                myFilteredRuns.add(new MyRun(date, id, length, mySimplifiedRoutePoints, timeMili));
                            }

                        } while (myCursor.moveToNext());
                    }
                    myCursor.close();
                }

                break;
            case 3:
//                for (MyRun run : RunData.previousRuns) {
//                    long cas = run.getDate().getTime();
//                    long cas1 = secondsAtThisMonday - 604800000;
//
//                    if (run.getDate().getTime() > (secondsAtThisMonday - 604800000) && run.getDate().getTime() < (secondsAtThisMonday - 1)) {
//                        myFilteredRuns.add(run);
//                    }
//                }

                myCursor = getContext().getContentResolver().query(RunContentProvider.CONTENT_URI, null, null, null, null);
                if (myCursor != null ) {
                    if (myCursor.moveToFirst()) {
                        do {
                            Log.i("rundataa", "length " + myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH)));

                            Date date = new Date (myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_DATE)));
                            int id = myCursor.getInt(myCursor.getColumnIndex(LatLngDatabase.COLUMN_ID));
                            float length = myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH));
                            long timeMili = myCursor.getLong(myCursor.getColumnIndex(LatLngDatabase.COLUMN_TIME));
                            String jsonOfRunPath = myCursor.getString(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LATLONG));

                            Log.i("myJsonPath", jsonOfRunPath+"" );

                            Gson myGson = new Gson();
                            Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>(){}.getType();
                            List<LatLong> mySimplifiedRoutePoints = myGson.fromJson(jsonOfRunPath, listOfMyLatLongObjects);

                            if (date.getTime() > (secondsAtThisMonday - 604800000) && date.getTime() < (secondsAtThisMonday - 1)) {
                                myFilteredRuns.add(new MyRun(date, id, length, mySimplifiedRoutePoints, timeMili));
                            }

                        } while (myCursor.moveToNext());
                    }
                    myCursor.close();
                }
                break;
        }
        mRunsAdapter = new RunsAdapter(getContext(), myFilteredRuns, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int myIdTag = (int) view.getTag();
                if (mAttachedActivity != null) {
                    mAttachedActivity.onLongClick(myIdTag);
                }
            }
        });

        mRunsListView.setAdapter(mRunsAdapter);
        mRunsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int possition, long l) {
                if (mAttachedActivity != null) {
                    MyRun clickedRun = (MyRun) adapterView.getItemAtPosition(possition);
                    mAttachedActivity.onClick(clickedRun.getId());
                }
            }
        });
        mRunsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int possition, long l) {
                if (mAttachedActivity != null) {
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.deletion_of_past_run)
                            .content(R.string.do_you_want_to_delete_db_data)
                            .positiveText(R.string.agree)
                            .negativeText(R.string.disagree)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    MyRun clickedRun = (MyRun) adapterView.getItemAtPosition(possition);
                                    mAttachedActivity.onLongClick(clickedRun.getId());
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                return true;
            }
        });

        // in this example, a LineChart is initialized from xml
        //final LineChart chart = (LineChart) findViewById(R.id.chart);

        // PieChart

        mChart = (PieChart) view.findViewById(R.id.chart);
//        mChart.notifyDataSetChanged();
//        mChart.invalidate(); // refresh

        // goal km for week
        SharedPreferences prefs = getContext().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        weekGoalDistanceKm = prefs.getFloat("goal", 0f);

        //sum of all runs length for one week
        //iterace pre vsetky prvky RunData triedy MyRun triedy
        sumOfKm = 0f;
        for (MyRun mRun : myFilteredRuns) {
            sumOfKm = sumOfKm + mRun.getLength();
        }

        //check if user finished his goal, sum of km runned is larger than goal
        if (sumOfKm < weekGoalDistanceKm) {

            //lime entry for completed runs
            entries.add(new PieEntry(sumOfKm, "Run"));
            //orange entry for unfinished goal
            entries.add(new PieEntry(weekGoalDistanceKm - sumOfKm, "Goal"));

            mChart.setDescriptionColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            mChart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

            myPieChart();

            // if user have run more than goal show it for him
        } else {

            //lime entry for completed runs
            entries.add(new PieEntry(sumOfKm, "Run"));
            //orange entry for unfinished goal
            //entries.add(new PieEntry(0f, "Goal"));

            mChart.setDescriptionColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            mChart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

            new MaterialDialog.Builder(getContext())
                    .title(R.string.congrats)
                    .content(R.string.keep_running)
                    .positiveText(R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            weekGoalDistanceKm = weekGoalDistanceKm*2;
//                            Bundle goalBundle = new Bundle();
//                            goalBundle.putFloat("goal", weekGoalDistanceKm);

//todo .. tieto hodnoty potrebujem dostat z fragmentu do dashboardu a potom spustit dashboard again. a tym padom potrebujem bundle.
                        }
                    })
                    .show();

            myPieChart();
        }


        return view;
    }

    public void myPieChart() {

        PieDataSet set = new PieDataSet(entries, "Election Results");

        // sets colors for the dataset, resolution of the resource name to a "real" color is done internally
        //  set.setColors(new int[]{R.color.green, R.color.yellow, R.color.red1, R.color.blue}, this);
        set.setColors(new int[]{R.color.lime, R.color.orange_accent}, getContext());

        //set numeric info out from the chart connected by line
        set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setValueLinePart1OffsetPercentage(80f);
        set.setValueTextSize(15f);
        set.setValueTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        set.setValueFormatter(new PercentFormatter());

        // set font for piechart info
        set.setValueTypeface(Typeface.MONOSPACE);

        //set small space between slices
        set.setSliceSpace(5f);

        //set description for piechart
        mChart.setDescription(String.format("Goal %.2f km", weekGoalDistanceKm));
        mChart.setDescriptionTypeface(Typeface.MONOSPACE);
        mChart.setDescriptionTextSize(15f);

        //mChart.setDescriptionPosition(1000f,20f);

        //text in the center of the piechart formating, and font
        mChart.setCenterText(String.format("%.2f\nkm", sumOfKm));
        mChart.setCenterTextTypeface(Typeface.MONOSPACE);
        mChart.setCenterTextSize(17f);   //not responsive for smaller screens


        //setting the fake data inside chart
        PieData data = new PieData(set);
        mChart.setData(data);
        mChart.setUsePercentValues(true);


        //font for labels of
        mChart.setEntryLabelTypeface(Typeface.MONOSPACE);

        mChart.animateXY(1200, 1200); //animation
        //mChart.animateY(3000, Easing.EasingOption.EaseInElastic); //animation

        mChart.notifyDataSetChanged();
        mChart.invalidate(); // refresh

    }

    public void checkTheWeekNumber() {

        final LocalDate now2 = new LocalDate();
        Log.i("timenow2", now2 + "");

        LocalTime now3 = new LocalTime();
        Log.i("timenow3", now3 + "");


        float weekNnumberNow = now2.getWeekOfWeekyear();
        Log.i("timenofweek", weekNnumberNow + "");

        float weekNnumberFirst = 42;
        Log.i("timenofweek", weekNnumberFirst + "");

        final List<Long> myRunsDates = new ArrayList<>();
        SharedPreferences prefs = getContext().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mUsername = prefs.getString("username", "");
        myRef.child(mUsername).child("runs").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values
                        // for each run get its date value and compare it with previous
                        for (DataSnapshot run : dataSnapshot.getChildren()) {
        //todo parse this fuck into milis and then chceck if it is larger then previous one .. and when i will found null then previous one is the lovest one and that will point onto correct week number
                            //                    Long date = now2.toDateTime().getMillis();

                            Long date1 = run.child("DATE").getValue(Long.class);      //same as Long date = (Long) run.child("DATE").getValue();
                            myRunsDates.add(date1);
                            Log.i("timenofweekall", myRunsDates + "");
                        }
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                });
    }

    public interface CallBacks {
        void onClick(int id);

        void onLongClick(int id);
    }
}
