package com.gpstracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import com.gpstracker.R;


/**
 * Created by User on 30.8.2016.
 */
public class ChronometerActivity extends AppCompatActivity {
    private Chronometer mChronometer;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonReset;
    private Button buttonGoBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        // Watch for button clicks.
        buttonStart = (Button) findViewById(R.id.start);
     //   buttonStart.setOnClickListener(mStartListener);

        buttonStart.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChronometer.start();
            }
        });



        buttonStop = (Button) findViewById(R.id.stop);
//        buttonStop.setOnClickListener(mStopListener);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChronometer.stop();
            }
        });

        buttonReset = (Button) findViewById(R.id.reset);
//        buttonReset.setOnClickListener(mResetListener);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChronometer.setBase(SystemClock.elapsedRealtime());
            }
        });

        buttonGoBack = (Button) findViewById(R.id.go_back);
//        buttonGoBack.setOnClickListener(mGoBackListener);

        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChronometerActivity.this, DashboardActivity.class));
            }
        });
    }


//
//    View.OnClickListener mStartListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            mChronometer.start();
//        }
//    };
//
//    View.OnClickListener mStopListener = new View.OnClickListener() {
//        public void onClick(View v) {
//            mChronometer.stop();
//        }
//    };
//
//    View.OnClickListener mResetListener = new View.OnClickListener() {
//        public void onClick(View v) {
//            mChronometer.setBase(SystemClock.elapsedRealtime());
//        }
//    };
//
////    View.OnClickListener mSetFormatListener = new View.OnClickListener() {
////        public void onClick(View v) {
////            mChronometer.setFormat("Formatted time (%s)");
////        }
////    };
////
////    View.OnClickListener mClearFormatListener = new View.OnClickListener() {
////        public void onClick(View v) {
////            mChronometer.setFormat(null);
////        }
////    };
//
//    View.OnClickListener mGoBackListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            startActivity(new Intent(ChronometerActivity.this, DashboardActivity.class));
//        }
//    };

}