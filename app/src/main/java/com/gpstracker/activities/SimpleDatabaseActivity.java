package com.gpstracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.gpstracker.CustomTextView;
import com.gpstracker.R;
import com.gpstracker.data_clases.RunDataBaseHelper;


/**
 * Created by RGarai on 30.8.2016.
 */
public class SimpleDatabaseActivity extends Activity {
    RunDataBaseHelper myDb;
    EditText editName, editSurname, editMarks;
    CustomTextView btnAddData;
    CustomTextView btnViewAll;
    CustomTextView buttonGoBack;

    //snackbar
    private Snackbar snackbar;
    private View snackbarView;
    private TextView snackbarTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simpledatabase);

        //snackbar
        mySnackbar();

        myDb = new RunDataBaseHelper(this);

        editName = (EditText) findViewById(R.id.editText_name);
        editSurname = (EditText) findViewById(R.id.editText_surname);
        editMarks = (EditText) findViewById(R.id.editText_Marks);

        btnAddData = (CustomTextView) findViewById(R.id.button_add);
        btnViewAll = (CustomTextView) findViewById(R.id.button_viewAll);

        buttonGoBack = (CustomTextView) findViewById(R.id.go_back);
        buttonGoBack.setOnClickListener(mGoBackListener);

        AddData();
    }

    public void viewAll() {
        btnViewAll.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Cursor res = myDb.getAllData();
                        if (res.getCount() == 0) {
                            //show message
                            showMessage("Error" , "Nothing found");
                            return;
                        }
                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("Id : " + res.getString(0) + "\n");
                            buffer.append("Name : " + res.getString(1) + "\n");
                            buffer.append("Surname : " + res.getString(2) + "\n");
                            buffer.append("Marks : " + res.getString(3) + "\n");
                        }

                        //show all data
                        showMessage("Data" , buffer.toString());
                    }
                }
        );
    }

    public void showMessage (String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void AddData() {
        btnAddData.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isInserted = myDb.insertData(editName.getText().toString(), editSurname.getText().toString(), editMarks.getText().toString());
                        if (isInserted = true) {
                            //Toast.makeText(SimpleDatabaseActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                            //snackbar
                            snackbarTextView.setText(getString(R.string.data_inserted));
                            snackbar.show();
                        } else {
                            //Toast.makeText(SimpleDatabaseActivity.this, "Data NOT Inserted", Toast.LENGTH_LONG).show();
                            //snackbar
                            snackbarTextView.setText(getString(R.string.data_not_inserted));
                            snackbar.show();
                        }
                    }
                }
        );
    }
    View.OnClickListener mGoBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(SimpleDatabaseActivity.this, DashboardActivity.class));
        }
    };

    //snackbar method
    public void mySnackbar(){
        //snackbar
        // Changing snackbar text color globaly
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.snackbar_default), Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(SimpleDatabaseActivity.this, R.color.colorAccent));
        // Changing snackbar text size
        snackbarTextView.setTextSize(20f);
        // Changing snackbar button-text color
        snackbar.setActionTextColor(Color.RED);
    }
}
