package com.gpstracker.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gpstracker.MyApplication;
import com.gpstracker.R;
import com.gpstracker.adapters.WeeksPagerAdapter;
import com.gpstracker.data_clases.LatLngDatabase;
import com.gpstracker.data_clases.RunContentProvider;
import com.gpstracker.fragments.WeekFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by RGarai on 18.8.2016.
 */

/*
 * Dashboard shows graph on top and list of previous runs
 */
public class DashboardActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, WeekFragment.CallBacks {

    /*
     * Variables
     */
    private String mUsername;
    private String mEmail;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION_CONNECTED = 2;
    private GoogleMap mMap;
    private ViewPager viewPager;

    //snackbar
    private Snackbar snackbar;
    private View snackbarView;
    private TextView snackbarTextView;

    //firebase database variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;

    //google analytics
    public Tracker mTracker;
    String category;
    String action;

    //flags for restoration of the data
    public boolean flagDeleting = false;
    public boolean flagRestoring = false;


    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

//    Button buttonForShowOnMap;
//    ListView mRunsListView;
//    RunsAdapter mRunsAdapter;
//    float sumOfKm = 0f;

    private ResideMenu resideMenu;

    private Typeface typeface;


    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dashboard);
        mLayout = findViewById(R.id.root_dashboard_layout);
        Log.i("MyMessageOnLoginScreen", "now the app shows the Dashboard screen and waits until button is pressed");

        //snackbar
        mySnackbar();

        //firebase db
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();

        SharedPreferences prefs = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mUsername = (prefs.getString("username", ""));
        mEmail = (prefs.getString("email", ""));
        Log.i("credentials", mUsername + " " + mEmail + "");

        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new WeeksPagerAdapter(getSupportFragmentManager(),
                DashboardActivity.this));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(1, false);
        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.sliding_tabs);
        viewPagerTab.setViewPager(viewPager);


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


        // dealing with DATE and day of the week
        Date todaysDate = Calendar.getInstance().getTime();
        Log.i("mojdatum", todaysDate + "");


        Calendar c = Calendar.getInstance();
        c.setTime(todaysDate);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        Log.i("mojdatum", dayOfWeek + " " + new SimpleDateFormat("EE").format(todaysDate));

        // TODO: Consider time zones, calendars etc
        LocalDate now = new LocalDate();
        LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
        LocalDate sunday = now.withDayOfWeek(DateTimeConstants.SUNDAY);
        Log.i("mojdatum", "Monday in this week was at: " + monday + " And Sunday is: " + sunday + "");

        Calendar calen = Calendar.getInstance();
        calen.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Log.i("mojdatum ", c.getTime() + "");

        //ResideMenu
        setUpResideMenu();


        //fab - floatin action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

//        fab.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent moev){
//                if (moev.getAction() == MotionEvent.ACTION_DOWN){
//                    double oldXvalue = moev.getX();
//                    double oldYvalue = moev.getY();
//                    Log.i("fabmoved", "Action Down " + oldXvalue + "," + oldYvalue);
//                }else if (moev.getAction() == MotionEvent.ACTION_MOVE  ){
//                    CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams((int)(moev.getRawX() - (v.getWidth() / 2)), (int)(moev.getRawY() - (v.getHeight())));
//                    v.setLayoutParams(params);
//                }
//                return true;
//            }
//        });

//
//        @Override
//        public boolean onTouchEvent(MotionEvent e) {
//            // MotionEvent reports input details from the touch screen
//            // and other input controls. In this case, you are only
//            // interested in events where the touch position changed.
//
//            float x = e.getX();
//            float y = e.getY();
//
//            switch (e.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    mIsDown = true;
//                    break;
//                case MotionEvent.ACTION_MOVE:
//
//                    float dx = x - mPreviousX;
//                    float dy = y - mPreviousY;
//
//                    // Here you can try to detect the swipe. It will be necessary to
//                    // store more than the previous value to check that the user move constantly in the same direction
//                    detectSwipe(dx, dy);
//
//                case MotionEvent.ACTION_UP:
//                    mIsDown = false;
//                    break;
//            }
//
//            mPreviousX = x;
//            mPreviousY = y;
//            return true;
//        }

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // google analytics hit
                category = "FloatingButtonClick";
                action = "NewRunAction";

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(category)
                        .setAction(action)
                        .build());
                // Click action
                showMap();
//                Intent intent = new Intent(DashboardActivity.this, MapsActivity.class);
//                startActivity(intent);
            }
        });
//
//        //vypis vsetkych prvkov ktore mam v databaze
//        Cursor curs = getContentResolver().query(RunContentProvider.CONTENT_URI, null, null, null, null);
//        if (curs.moveToFirst()) {
//            do {
//               // list.add(new Product(cursor.getString(0), Integer.parseInt(cursor.getString(1))));
//                Log.i("rundataa", "length " + curs.getFloat(curs.getColumnIndex(LatLngDatabase.COLUMN_LENGTH)));
//            } while (curs.moveToNext());
//        }


        //checking goal if not exceded
//        //todo bundle
//        SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
//        editor.putFloat("goal", weekGoalDistanceKm).commit();
//        SharedPreferences prefs = getContext().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
//        float mGoal = (prefs.getFloat("goal", 0f));
//        myRef.child(mUsername).child("Goal").setValue(weekGoalDistanceKm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "DashboardActivity");
        mTracker.setScreenName("Image~" + "DashboardActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    //ResideMenu
    private void setUpResideMenu() {
        // attach to current activity;
        resideMenu = new ResideMenu(this);

        //set one static background
        resideMenu.setBackground(R.drawable.background8);

        //set header
        resideMenu.addMenuHeader(mUsername, mEmail, ResideMenu.DIRECTION_LEFT);

        resideMenu.attachToActivity(this);

        //set size of screen when menu is opened
        resideMenu.setScaleValue(0.7f);

        //disabling the right swipe gesture (from right to left side menu opening)
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        //adding a part of the screen view, where the reside menu swipe is ignored.
        resideMenu.addIgnoredView(findViewById(R.id.ingoredView));

        //3d rotation
        resideMenu.setUse3D(true);

        // create menu items;
        //old menu with chrono and simple DB
//        String titles[] = {"New Run", "Settings", "Chrono", "SimpleDB", "Log out"};
//        int icon[] = {R.drawable.run1, R.drawable.run1, R.drawable.run1, R.drawable.run1, R.drawable.run1};

        //menu as it should be on deploy
        String titles[] = {getString(R.string.settings), getString(R.string.logout)};
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
//                        case 0: {
//                            showMap();
//                            action = "ShowMapAction";

//                        }
//                        break;

                        case 0: {
                            action = "SettingsAction";
                            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                        break;

                        //old menu deprecated
//                        case 2: {
//                            Intent intent = new Intent(DashboardActivity.this, ChronometerActivity.class);
//                            startActivity(intent);
//                        }
//                        break;
//
//                        case 3: {
//
//                            Intent intent = new Intent(DashboardActivity.this, SimpleDatabaseActivity.class);
//                            startActivity(intent);
//                        }
//                        break;

                        case 1: {
                            action = "LogOutAction";
                            logOutAction();
                            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        break;
                        //default: position = "huhu"
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

    public void logOutAction() {
        //store data into shared preferences file on mobile drive
        SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
        editor.remove("goal").apply();
        editor.remove("username").apply();
        editor.remove("email").apply();
        editor.remove("pass").apply();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }


    //check permission if it exist
    // Assume thisActivity is the current activity
//    int permissionCheck = ContextCompat.checkSelfPermission(DashboardActivity.this,
//            Manifest.permission.ACCESS_FINE_LOCATION);

    public void showMap() {
        Log.i("permissionsmap", "Show new run button pressed. Chcecking permission.");
        // BEGIN_INCLUDE(map_permission)
        // Check if the Map permission is already available.
        if (ActivityCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Map permission has not been granted.

            requestMapPermission();

        } else {

            // Map permissions is already available, show the Map preview.
            Log.i("permissionsmap",
                    "Map permission has already been granted. Displaying map preview.");
            Intent intent = new Intent(DashboardActivity.this, MapsActivity.class);
            startActivity(intent);

        }
        // END_INCLUDE(map_permission)

    }

    /**
     * Requests the Map permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestMapPermission() {
        Log.i("permissionsmap", "Map permission has NOT been granted. Requesting permission.");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            AppCompatActivity appCompatActivityContext = (AppCompatActivity) DashboardActivity.this;

            appCompatActivityContext.requestPermissions(permissions, 2020);
        }

        // BEGIN_INCLUDE(Map_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("permissionsmap",
                    "Displaying map permission rationale to provide additional context.");

            Snackbar.make(mLayout, R.string.permission_map_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(DashboardActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Map permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        // END_INCLUDE(map_permission_request)
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2020 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(DashboardActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    @Override
    public void onClick(int id) {
        // google analytics hit
        category = "ListOfRunsClick";
        action = "ShowPreviousRunAction";

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());

        //change intent
        Intent intent = new Intent(DashboardActivity.this, DetailOfRunActivity.class);


//Create the bundle
        Bundle bundle = new Bundle();

//Add your data to bundle
        bundle.putInt("requestedId", id);

//Add the bundle to the intent
        intent.putExtras(bundle);

//Fire that second activity
        startActivity(intent);

    }

    public void askUserForUndoDelete(final int id) {


        //delete from local database
        getContentResolver().delete(Uri.parse(RunContentProvider.URL + "/" + id), null, null);

        //refresh my screen
        int myPage = viewPager.getCurrentItem();
        viewPager.setAdapter(new WeeksPagerAdapter(getSupportFragmentManager(),
                DashboardActivity.this));
        viewPager.setCurrentItem(myPage, false);

        //if no internet connection then show snackbar and inform the user that database is not synchronized.
        snackbarTextView.setText(getString(R.string.run_deleted));
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);
        snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final Snackbar snackbar1 = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.data_restored), Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar1.getView();
                TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                snackbarTextView.setTextColor(ContextCompat.getColor(DashboardActivity.this, R.color.colorAccent));
                // Changing snackbar text size
                snackbarTextView.setTextSize(20f);


                //delete from firebase database
                myRef.child(mUsername).child("runs").child(String.valueOf(id)).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot run) {
                                // Get user values

                                Long id = run.child("ID").getValue(Long.class);
                                Long date = run.child("DATE").getValue(Long.class);      //same as Long date = (Long) run.child("DATE").getValue();
                                Double length = run.child("LENGTH").getValue(Double.class);
                                Long time = run.child("TIME").getValue(Long.class);
                                String myJsonSringOfLatLong = run.child("LATLONGDATA").getValue(String.class);

                                //inserting into local database from firebase database
                                ContentValues runValues = new ContentValues();
                                runValues.put(LatLngDatabase.COLUMN_LATLONG, myJsonSringOfLatLong);
                                runValues.put(LatLngDatabase.COLUMN_ID, id);
                                runValues.put(LatLngDatabase.COLUMN_DATE, date);
                                runValues.put(LatLngDatabase.COLUMN_LENGTH, length);
                                runValues.put(LatLngDatabase.COLUMN_TIME, time);

                                getContentResolver().insert(RunContentProvider.CONTENT_URI, runValues);

                                snackbar1.show();


                            }

                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("error", "getUser:onCancelled", databaseError.toException());

                            }
                        });

            }
        });
        snackbar.show();
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                removeFromFirebaseDb(id);


            }
        });

//todo tu urob moznost UNDO mazanie z databazí oboch. nech to je taky oser ze uzivatel nech si mysli ze to zmazalo no v skutocnosti to zmaze az ked nestlaci undo.
//todo potrebujes este jedno miesto kde tie dva flaky nastavia rovnaku hodnotu a tou urcujes ze ci treba zmazat naozaj alebo nie.

        if ((flagDeleting = true) && (flagRestoring = false)) {
        }
    }


    @Override
    public void onLongClick(int id) {

        askUserForUndoDelete(id);
    }

    public void removeFromFirebaseDb(int id) {
        // google analytics hit
        category = "ListOfRunsLongClick";
        action = "DeleteOneRunAction";

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
//        //delete from local database
//        getContentResolver().delete(Uri.parse(RunContentProvider.URL + "/" + id), null, null);
//
//        //refresh my screen
//        int myPage = viewPager.getCurrentItem();
//        viewPager.setAdapter(new WeeksPagerAdapter(getSupportFragmentManager(),
//                DashboardActivity.this));
//        viewPager.setCurrentItem(myPage, false);

        //delete from firebase database
        myRef.child(mUsername).child("runs").child(String.valueOf(id)).removeValue();

        //snackbar
        snackbarTextView.setText(getString(R.string.run_deleted));
        snackbar.show();
    }

    //snackbar method
    public void mySnackbar() {
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.root_dashboard_layout), getString(R.string.snackbar_default), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(DashboardActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);

    }


}
