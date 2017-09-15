package com.gpstracker.data_clases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by RGarai on 8.9.2016.
 */
public class RunDataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "student.db";
    public static final String TABLE_NAME = "student_table";
    public static final String COLLUMN_1 = "ID";
    public static final String COLLUMN_2 = "NAME";
    public static final String COLLUMN_3 = "SURNAME";
    public static final String COLLUMN_4 = "MARKS";
    /*
     *  constructor
     */
    public RunDataBaseHelper(Context context) {
        super (context, DATABASE_NAME, null, 1);
     }

    /*
     * methods
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, SURNAME TEXT, MARKS INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_NAME);
        onCreate(db);
    }


    //add new data
    public boolean insertData(String name, String surname, String marks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLLUMN_2, name);
        contentValues.put(COLLUMN_3, surname);
        contentValues.put(COLLUMN_4, marks);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }


    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_NAME , null);
        return results;
    }

}
