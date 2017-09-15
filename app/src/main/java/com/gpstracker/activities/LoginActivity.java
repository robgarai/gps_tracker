package com.gpstracker.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gpstracker.CustomTextView;
import com.gpstracker.MyApplication;
import com.gpstracker.R;
import com.gpstracker.SHA256Hash;
import com.gpstracker.data_clases.LatLngDatabase;
import com.gpstracker.data_clases.RunContentProvider;

/**
 * Created by RGarai on 18.8.2016.
 */

/*
 * Login screen for now just click login button and it will lead you to dashboard screen
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    /*
     * Variables
     */

    private CustomTextView buttonForLogin;
    private CustomTextView buttonForRegistration;
    private LoginButton loginButton;
    CheckBox permaLoginCheckBox;
    private EditText editUserName, editPassword;

    private String mUsername;
    private String mEmail;
    private String mPassword;
    private String myHash;

    //snackbar
    private Snackbar snackbar;
    private View snackbarView;
    private TextView snackbarTextView;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    CallbackManager callbackManager = CallbackManager.Factory.create();
    private Handler handler = new Handler();
    //google analytics
    public Tracker mTracker;
    String category;
    String action;

    //firebase database variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;


    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.i("MyMessageOnLoginScreen", "now the app shows the login screen and waits until button is pressed");

        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        //firebase
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();

        //snackbar
        mySnackbar();

        //all buttons
        buttonForLogin = (CustomTextView) findViewById(R.id.buttonLogin);
        buttonForLogin.setOnClickListener(this);

        buttonForRegistration = (CustomTextView) findViewById(R.id.buttonRegistration);
        buttonForRegistration.setOnClickListener(this);

        permaLoginCheckBox = (CheckBox) findViewById(R.id.check_box_login_remember);
        permaLoginCheckBox.setOnClickListener(this);

        editUserName = (EditText) findViewById(R.id.loginNameInput);
        editPassword = (EditText) findViewById(R.id.passwordInput);

        //FB login
        loginButton = (LoginButton) findViewById(R.id.buttonFBLogin);
        loginButton.setReadPermissions("email");

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("fbreaction", "fb is connected");
                //todo finish this
                mUsername = loginResult.getAccessToken().getUserId();
                Log.i("fbreaction", mUsername + "");

                //if facebook login succes then wait 2 second and simultaneously show snackbar with message
                // and then jump to the next intent dashboardactivity and simultaniously send event handler to the google analytics
                //snackbar connection failed
                snackbarTextView.setText(getString(R.string.faceoobk_connected));
                snackbar.show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        category = "ButtonClick";
                        action = "FacebookLogInAction";
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(category)
                                .setAction(action)
                                .build());

                    }
                }, 1000);
            }

            @Override
            public void onCancel() {
                // App code
                //snackbarInternetConnectionFail();

            }

            @Override
            public void onError(FacebookException exception) {
                //snackbarInternetConnectionFail();
            }
        });

        //checking permamnent login
//        if permaLoginCheckBox.isChecked() {
//            startActivity(new Intent(this, DashboardActivity.class));
//        }


        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        // [END customize_button]


    }

    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "LoginActivity");
        // Set screen name.
        mTracker.setScreenName("Image~" + "LoginActivity");
        // Send a screen view.
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onClick(View view) {
        category = "ButtonClick";
        action = "";
        switch (view.getId()) {
            case R.id.sign_in_button:
                action = "SignInAction";
                signIn();
                break;
            case R.id.sign_out_button:
                action = "SignOutAction";
                signOut();
                break;
            case R.id.disconnect_button:
                action = "DisconetAction";
                revokeAccess();
                break;
            case R.id.buttonLogin:
                action = "LogInAction";
                checkValidityOfCredentials();

                break;
            case R.id.buttonRegistration:
                action = "RegistrationAction";
                startActivity(new Intent(this, RegisterActivity.class));
                Log.i("MyMessageOnLoginSucess", "now the app shows the login screen and waits until button is pressed");
                break;
        }
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }


    public boolean checkValidityOfCredentials() {
        editUserName.setError(null);
        editPassword.setError(null);

        mUsername = editUserName.getText().toString();
        mPassword = editPassword.getText().toString();


        if (mUsername.isEmpty()) {
            editUserName.setError(getString(R.string.required));
        }

        if (mPassword.isEmpty()) {
            editPassword.setError(getString(R.string.required));
        }

        myHash = SHA256Hash.sha256(mUsername + mPassword);

        //show snackbar for user
        if (isOnline()) {
            //if connected then continue with hash validation
             retrieveHashDataFromDB();
        } else {
            //if not connected then show snackbar
            //snackbar connection failed
            snackbarTextView.setText(getString(R.string.internet_connection_failed));
            snackbar.show();
        }

        return true;
    }


    public void retrieveHashDataFromDB() {

        // retrieve goal value from firebase database in km (double)
        myRef.child(mUsername).child(myHash).addListenerForSingleValueEvent(

                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values
                        // get validation secreat value and decide if user can continue
                        String validationString = dataSnapshot.child("boolean").getValue(String.class);
                        if ((validationString != null) && (validationString.equals("jou:4d5"))) {
                            //pokracuj v ziskavani dat
                            Log.i("validation", "validation succeded");

                            //synchronyzing the database
                            synchronizeFirebaseDB();

                            //startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            Log.i("MyMessageOnLoginSucess", "now the app shows the login screen and waits until button is pressed");
                        } else {
                            //vyhod hlasku ze je nieco zle
                            Log.i("validation", "validation error");
                            //snackbar wrong login data username or password
                            snackbarTextView.setText(getString(R.string.bad_credentials));
                            snackbar.show();

                        }


                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    public void synchronizeFirebaseDB() {
        // retrieve connection value from firebase database in km (double)
        myRef.child(mUsername).child("Goal").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values

                        int goal = dataSnapshot.getValue(Integer.class);
                        SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                        editor.putFloat("goal", (float) goal).apply();

                        //when retrieveng the connection data is finishet then start retrievieng the rest of the data
                        retrieveRunsDataFromDB();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("connectionProblem", "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                }
        );

        // retrieve connection value from firebase database in km (double)
        myRef.child(mUsername).child("Email").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values

                        mEmail = dataSnapshot.getValue(String.class);
                        SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                        editor.putString("email", mEmail).apply();

                        //when retrieveng the connection data is finishet then start retrievieng the rest of the data
                        retrieveRunsDataFromDB();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("connectionProblem", "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                }
        );

    }

    public void retrieveRunsDataFromDB() {

        //snackbar connection failed
        snackbarTextView.setText(getString(R.string.loading_data));
        snackbar.show();

        // retrieve all previous runs values from firebase database
        /*
         * get other data exactly:
         * >runs
         *     >>0
         *     >>1
         *     >>2
         *       >>>id: long
         *       >>>date: long
         *       >>>length: double
         *       >>>time: long
         *       >>>LatLongData: JSON
         */

        //first delete local database on phone drive
        getContentResolver().delete(RunContentProvider.CONTENT_URI, null, null);

        myRef.child(mUsername).child("runs").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values
                        for (DataSnapshot run : dataSnapshot.getChildren()) {

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

                            //store data into shared preferences file on mobile drive
                            SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                            editor.putString("username", mUsername).apply();
                            editor.putString("email", mEmail).apply();
                            editor.putString("pass", mPassword).apply();
                        }

                        //starting the dashboard activity
                        startDashboardActivity();
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                });

    }

    public void startDashboardActivity() {

        //start dashboard activity
        Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(i);

        //animation transition between activities
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);


    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);

            Log.i("googlereaction", "google plus is connected");

            //if google plus login succes then wait 2 second and simultaneously show snackbar with message
            // and then jump to the next intent dashboardactivity and simultaniously send event handler to the google analytics
            //snackbar connection google plus succes and connected
            snackbarTextView.setText(getString(R.string.google_plus_connected));
            snackbar.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    category = "ButtonClick";
                    action = "GooglePlusLogInAction";
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .build());

                }
            }, 2000);

        } else {
            // Google plus Signed out, show unauthenticated UI.
            updateUI(false);
            //snackbarInternetConnectionFail();
            //snackbar connection failed
            //  snackbarTextView.setText(getString(R.string.google_connection_failed));
            //  snackbar.show();
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        //snackbar connection failed
        snackbarTextView.setText(getString(R.string.internet_connection_failed));
        snackbar.show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            //for automaticaly switch to the dashboard after login into google account.
//                startActivity(new Intent(this, DashboardActivity.class));
//                Log.i("MyMessageOnLoginSucess", "now the app shows the login screen and waits until button is pressed");
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

//    public void snackbarInternetConnectionFail() {
//        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.internet_connection_failed), Snackbar.LENGTH_INDEFINITE);
//        View snackbarView = snackbar.getView();
//        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
//        snackbarTextView.setTextColor(Color.RED);
//        // Changing snackbar text size
//        snackbarTextView.setTextSize(20f);
//
//        snackbar.show();
//    }

    //snackbar method
    public void mySnackbar() {
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.snackbar_default), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);

    }
}







