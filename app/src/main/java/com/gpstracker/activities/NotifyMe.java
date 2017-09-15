package com.gpstracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.gpstracker.CustomTextView;
import com.gpstracker.MyApplication;
import com.gpstracker.R;

/**
 * Created by RGarai on 11.10.2016.
 */

public class NotifyMe extends AppCompatActivity {

    CustomTextView buttonGoBack;
    CustomTextView buttonExit;
    TextView TextViewFirebase;

    //google analytics
    public Tracker mTracker;
    String category;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notifyme);

        //textview
        TextViewFirebase = (TextView) findViewById(R.id.textView_notifyMe_info);
        TextViewFirebase.setText(getIntent().getExtras().getString("MyFirebaseMessage"));

        // google analytics
        // Obtain the shared Tracker instance.
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        category = "";
        action = "";

        //buttons and edittext
        buttonGoBack = (CustomTextView) findViewById(R.id.go_to_app);
        buttonGoBack.setOnClickListener(mGoBackListener);

        buttonExit = (CustomTextView) findViewById(R.id.go_to_droid);
        buttonExit.setOnClickListener(mExitListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i("GoogleAnalytics", "Setting screen name: " + "NotifyMeActivity");
        // Set screen name.
        mTracker.setScreenName("Image~" + "NotifyMeActivity");
        // Send a screen view.
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    View.OnClickListener mGoBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // google analytics hit
            category = "ButtonClick";
            action = "GotoSplashScreenAction";

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
            startActivity(new Intent(NotifyMe.this, SplashScreen.class));
        }
    };
    View.OnClickListener mExitListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // google analytics hit
            category = "ButtonClick";
            action = "GotoDroidHomeScreenAction";

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    };

}
