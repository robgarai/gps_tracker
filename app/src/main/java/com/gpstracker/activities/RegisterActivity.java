package com.gpstracker.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gpstracker.CustomTextView;
import com.gpstracker.SHA256Hash;
import com.gpstracker.MyApplication;
import com.gpstracker.R;
import com.gpstracker.data_clases.FirebaseUser;

/**
 * Created by RGarai on 18.8.2016.
 */

/*
 * Login screen for now just click login button and it will lead you to dashboard screen
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    /*
     * Variables
     */

    private CustomTextView buttonForRegistration;

    private CheckBox permaLoginCheckBox;
    private EditText editUserName, editEmail, editPassword, editPasswordDuplo;
    private FirebaseUser myUser;

    private String mUsername;
    private String mEmail;
    private String mPassword;
    private String mPasswordDuplo;
    private String mHash;

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


    /*1
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.i("MyMessageOnLoginScreen", "now the app shows the login screen and waits until button is pressed");

        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        //snackbar
        mySnackbar();

        //firebase db
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();

        editUserName = (EditText) findViewById(R.id.loginNameInput);
        editEmail = (EditText) findViewById(R.id.emailInput);
        editPassword = (EditText) findViewById(R.id.passwordInput);
        editPasswordDuplo = (EditText) findViewById(R.id.passwordInputDuplo);

        buttonForRegistration = (CustomTextView) findViewById(R.id.buttonRegistration);
        buttonForRegistration.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "RegisterActivity");
        // Set screen name.
        mTracker.setScreenName("Image~" + "RegisterActivity");
        // Send a screen view.
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    //this method is called when button register is called.
    @Override
    public void onClick(View v) {
        // google analytics hit
        category = "ButtonClick";
        action = "Register&Store&GotoDashboardAction";

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());

        Log.i("myRegisterButton", "klikol si na onclick");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        //creating hash and unique identifier for user
        createHash();

        //    if (editPassword equals editPasswordDuplo){
        //        myRef.child(editUserName.getText().toString()).child("Email").setValue(editEmail.getText().toString());
        //       myRef.child(editUserName.getText().toString()).child("Password").setValue(editPassword.getText().toString());
        //   }
    }


    //this method is called when button go back is called, inside xml file activity_register there is button with onclick parameter
    public void go_back(View v) {
        // Write a message to the database
        Log.i("myRegisterButton", "klikol si na go back button");
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));


    }

    //snackbar method
    public void mySnackbar() {
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.snackbar_default), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);
    }


    public String createHash() {
        mUsername = editUserName.getText().toString();
        mEmail = editEmail.getText().toString();
        mPassword = editPassword.getText().toString();
        mPasswordDuplo = editPasswordDuplo.getText().toString();

        editUserName.setError(null);
        editEmail.setError(null);
        editPassword.setError(null);
        editPasswordDuplo.setError(null);

        //checking if the edit text are not null
        if ((!mUsername.equals("")) && (!mEmail.equals("")) && (!mPassword.equals("")) && (!mPasswordDuplo.equals(""))) {

            //checking if passwords are correctly written
            if (mPassword.equals(mPasswordDuplo)) {

                //checking if email is valid
                if (isEmailValid(mEmail)) {
                    mHash = SHA256Hash.sha256(mUsername + mPassword);
                    //Log.i("hash", mHash);

                    //upload data into firebase database
                    chceckUserExistence();



                    //--start-- the examples 2

//                    public byte[] getHash(String myHash) {
//                        MessageDigest digest=null;
//                        try {
//                            digest = MessageDigest.getInstance("SHA-256");
//                        } catch (NoSuchAlgorithmException e1) {
//                            // TODO Auto-generated catch block
//                            e1.printStackTrace();
//                        }
//                        digest.reset();
//                        return digest.digest(myHash.getBytes());
//                    }
//                    static String bin2hex(byte[] data) {
//                        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));

//                    MessageDigest md = MessageDigest.getInstance("MD5");
//                    FileInputStream fis = new FileInputStream("c:\\loging.log");
//
//                    byte[] dataBytes = new byte[1024];
//
//                    int nread = 0;
//                    while ((nread = fis.read(dataBytes)) != -1) {
//                        md.update(dataBytes, 0, nread);
//                    };
//                    byte[] mdbytes = md.digest();
//
//                    //convert the byte to hex format method 1
//                    StringBuffer sb = new StringBuffer();
//                    for (int i = 0; i < mdbytes.length; i++) {
//                        sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
//                    }
//
//                    System.out.println("Digest(in hex format):: " + sb.toString());
//
//                    //convert the byte to hex format method 2
//                    StringBuffer hexString = new StringBuffer();
//                    for (int i=0;i<mdbytes.length;i++) {
//                        String hex=Integer.toHexString(0xff & mdbytes[i]);
//                        if(hex.length()==1) hexString.append('0');
//                        hexString.append(hex);
//                    }
//                    System.out.println("Digest(in hex format):: " + hexString.toString());

                    //--end-- the examples 2


                    //if not snackbar shows
                } else {
                    //snackbar
                    snackbarTextView.setText(getString(R.string.email_not_valid));
                    editEmail.setError(getString(R.string.email_not_valid));
                    snackbar.show();
                }

                //if not snackbar shows
            } else {
                //snackbar
                snackbarTextView.setText(getString(R.string.passwords_are_different));
                editPassword.setError(getString(R.string.passwords_are_different));
                editPasswordDuplo.setError(getString(R.string.passwords_are_different));
                snackbar.show();
            }

            //if not snackbar shows
        } else {
            //snackbar
            snackbarTextView.setText(getString(R.string.fill_all_edittexts));
            editUserName.setError(getString(R.string.required));
            editEmail.setError(getString(R.string.required));
            editPassword.setError(getString(R.string.required));
            editPasswordDuplo.setError(getString(R.string.required));
            snackbar.show();
        }
        return mHash;
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void chceckUserExistence() {
        editUserName.setError(null);

        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get username
                        //String username = dataSnapshot.getValue(String.class);
                        boolean nameFound = false;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {

                            if (mUsername.equals(child.getKey())) {
                                nameFound = true;
                                break;
                            }
                        }
                        if (nameFound == true) {
                            //if user name alredy exist it is necessary to change it
                            snackbarTextView.setText(getString(R.string.username_alredy_exist));
                            editUserName.setError(getString(R.string.username_alredy_exist));
                            snackbar.show();

                        } else {
                            snackbarTextView.setText(getString(R.string.syncing_data));
                            snackbar.show();
                            storeIntoFirebaseDatabase();
                        }
                    }

                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void storeIntoFirebaseDatabase() {

        myRef.child(mUsername).child("Email").setValue(mEmail);
        myRef.child(mUsername).child(mHash).child("boolean").setValue("jou:4d5");
        //myRef.child(mUsername).child("Password").setValue(mHash);
        myRef.child(mUsername).child("Goal").setValue(1);
        myRef.child(mUsername).child("runs");



        //store data into shared preferences file on mobile drive
        SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
        editor.putFloat("goal", 1.0f).apply();
        editor.putString("username", mUsername).apply();
        editor.putString("email", mEmail).apply();
        editor.putString("pass", mPassword).apply();

        //switch INTENT
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, 300);

        //this is orriginal way i was reading directly from editText fields
        //myRef.child(editUserName.getText().toString()).child("Email").setValue(editEmail.getText().toString());
        //myRef.child(editUserName.getText().toString()).child("Password").setValue(editPassword.getText().toString());
    }

}

