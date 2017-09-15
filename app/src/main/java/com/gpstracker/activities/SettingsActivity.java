package com.gpstracker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gpstracker.CustomTextView;
import com.gpstracker.MyApplication;
import com.gpstracker.R;

import static com.google.android.gms.wearable.DataMap.TAG;


/**
 * Created by RGarai on 30.8.2016.
 */
public class SettingsActivity extends Activity {

    //variables
    private String mUsername;

    private EditText addNumberOfKm;
    private TextView currentGoalOfKm;
    private TextView connectionInfo;
    private CustomTextView setKmButton;
    private CustomTextView buttonGoBack;

    //google analytics variables
    public Tracker mTracker;
    private String category;
    private String action;

    //firebase database variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;

    //snackbar
    private Snackbar snackbar;
    private View snackbarView;
    private TextView snackbarTextView;

    private float temporarySettings;

    //methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        //upload data into firebase database
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();


        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        //buttons and edittext
        //edit text for changing the goal
        addNumberOfKm = (EditText) findViewById(R.id.editText_set_goal_numberOfKm);

        //text view for showing how many km we currently have set in goal
        currentGoalOfKm = (TextView) findViewById(R.id.textView_current_goal_numberOfKm);
        connectionInfo = (TextView) findViewById(R.id.textView_connectionInfo);

        //button for set new goal
        setKmButton = (CustomTextView) findViewById(R.id.button_setkm);
        //button go back to dashboard
        buttonGoBack = (CustomTextView) findViewById(R.id.go_back);
        buttonGoBack.setOnClickListener(mGoBackListener);

        SharedPreferences prefs = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mUsername = (prefs.getString("username", ""));

        //snackbar starts
        mySnackbar();

        //call for goal from firebase database or shared preff dependding on connection and store it into temp
        updateTemporaryGoalSettings();

        setKmButton.setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // google analytics hit
                        category = "ButtonClick";
                        action = "SaveSPrefferencesAction";

                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(category)
                                .setAction(action)
                                .build());

                        //final SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                        try {
                            //getting the new goal number from edit text field
                            final float newNumberOfKm = Float.parseFloat(addNumberOfKm.getText().toString());

                            //revriting the firebase value of goal distance setting
                            myRef.child(mUsername).child("Goal").setValue(newNumberOfKm);

                            //putting the new goal data into SharedPreferences goal in phone memory
                            SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                            editor.putFloat("goal", newNumberOfKm).commit();

                            //Toast.makeText(SettingsActivity.this, "Goal distance data set", Toast.LENGTH_LONG).show();
                            //snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Goal distance data set", Snackbar.LENGTH_LONG);

                            //set the current goal into text view
                            String showGoalAsString = (addNumberOfKm.getText().toString());
                            currentGoalOfKm.setText(showGoalAsString);

                            //showing options for restoration the previous data set so undo button is shown
                            snackbarTextView.setText(getString(R.string.goal_data_set));
                            snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {

                                //when user want to set previous data into database then UNDO is clicked
                                // but not only firebase database need to be changed, also the SharedPreferencesi n phone memory
                                @Override
                                public void onClick(View view) {

                                    //restoring firebase goal
                                    myRef.child(mUsername).child("Goal").setValue(temporarySettings);

                                    //restoring SharedPreferences goal in phone memory by old goal data from temp
                                    SharedPreferences.Editor editor1 = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                                    editor1.putFloat("goal", temporarySettings).commit();

                                    //showing new snackbar telling that restoration was succesfull
                                    Snackbar snackbar1 = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.goal_settings_restored), Snackbar.LENGTH_SHORT);
                                    // Changing snackbar text color localy
                                    View snackbar1View = snackbar1.getView();
                                    TextView snackbar1TextView = (TextView) snackbar1View.findViewById(android.support.design.R.id.snackbar_text);
                                    snackbar1TextView.setTextColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
                                    // Changing snackbar text size
                                    snackbar1TextView.setTextSize(20f);
                                    snackbar1.show();

                                    //set the current goal into text view
                                    String showGoalAsString = Float.toString(temporarySettings);
                                    currentGoalOfKm.setText(showGoalAsString);

                                }
                            });
                            snackbar.show();

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            //Toast.makeText(SettingsActivity.this, "Insert number Please", Toast.LENGTH_LONG).show();
//                            snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Insert number Please", Snackbar.LENGTH_INDEFINITE);
                            snackbarTextView.setText(getString(R.string.insert_number_here));
                            snackbar.show();


                        }
                        ;

                        //                       editor.apply();
//                        editor.apply();

                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "SettingsActivity");
        // Set screen name.
        mTracker.setScreenName("Image~" + "SettingsActivity");
        // Send a screen view.
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    OnClickListener mGoBackListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // google analytics hit
            category = "ButtonClick";
            action = "GotoDashboardAction";

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
            startActivity(new Intent(SettingsActivity.this, DashboardActivity.class));
        }
    };

    private void updateTemporaryGoalSettings() {

        //check if the user is online
        if (isOnline()) {

            // if online then retrieve goal value from firebase database in km (float)
            myRef.child(mUsername).child("Goal").addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user values
                            //read and get goal distance settings
                            temporarySettings = dataSnapshot.getValue(Float.class);

                            //set the current goal into text view
                            String showGoalAsString = Float.toString(temporarySettings);
                            currentGoalOfKm.setTextColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
                            currentGoalOfKm.setText(showGoalAsString);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // ...
                        }
                    }
            );

            // if onfline then retrieve goal value from shared prefferences and show red warning
        } else {

            SharedPreferences prefs = getApplicationContext().getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            temporarySettings = prefs.getFloat("goal", 0f);

            //set the current goal into text view
            String showGoalAsString = Float.toString(temporarySettings);
            currentGoalOfKm.setTextColor(Color.RED);
            currentGoalOfKm.setText(showGoalAsString);

            connectionInfo.setTextColor(Color.RED);
            connectionInfo.setText(getString(R.string.internet_connection_failed) + " \n " + getString(R.string.data_not_synchronyzed));
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //snackbar method
    public void mySnackbar() {
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.insert_number), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);
    }
}
