package com.gpstracker.data_clases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by RGarai on 11. 8. 2016.
 */
public class LatLngDatabase extends SQLiteOpenHelper {

    // strings for tablename, product and boolean check
    public static final String TABLE_NAME = "RunData";
    public static final String DATABASE_NAME = "run.db";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATLONG = "LATLONG";
    public static final String COLUMN_LENGTH = "LENGTH";
    public static final String COLUMN_DATE = "DATE";
    public static final String COLUMN_TIME = "TIME";

    //constructor
    //this create the database
    public LatLngDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    //now onCreate method creates the table inside database
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LATLONG + " TEXT, "
                + COLUMN_LENGTH + " REAL, "
                + COLUMN_DATE + " INTEGER, "
                + COLUMN_TIME + " INTEGER) "
        );
}

    //if my versions of tables are different this method will decide what to do with the older one
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
     //this is not okay for altered tables if altering you need to repair this part
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long getMaxId() {
        long result = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor = db.rawQuery("SELECT max(" + COLUMN_ID + ") as " + COLUMN_ID + " FROM " + TABLE_NAME, null);
        if (myCursor != null) {
            if (myCursor.moveToFirst()) {
                result = myCursor.getInt(myCursor.getColumnIndex(LatLngDatabase.COLUMN_ID));
            }
            myCursor.close();

        }
        return result;
    }
//inserting the data

    //add new data
//    public boolean insertData(String latlogn, float length, int date, int time) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COLUMN_LATLONG, latlogn);
//        contentValues.put(COLUMN_LENGTH, length);
//        contentValues.put(COLUMN_DATE, date);
//        contentValues.put(COLUMN_TIME, time);
//        long result = db.insert(TABLE_NAME, null, contentValues);
//        if (result == -1) {
//            return false;
//        } else {
//            return true;
//        }
//    }


//    public float getLength(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor myCursor = db.rawQuery("SELECT " + COLUMN_LENGTH + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
//        if (myCursor != null ) {
//            if (myCursor.moveToFirst()) {
//                float length = myCursor.getFloat(myCursor.getColumnIndex(LatLngDatabase.COLUMN_LENGTH));
//                return length;
//            }
//            myCursor.close();
//        }
//        return 0f;
//    }
}



