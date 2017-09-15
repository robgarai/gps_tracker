package com.gpstracker.data_clases;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by RGarai on 12.9.2016.
 * this class is here because of creation of the table and database also
 */
public class RunContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.gpstracker.unicornsystems.eu.gpstracker.RunContentProvider";
    public static final String URL = "content://" + PROVIDER_NAME + "/runs";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    static final int RUNS = 1;
    static final int RUN_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "runs", RUNS);
        uriMatcher.addURI(PROVIDER_NAME, "runs/#", RUN_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
//    static final String DATABASE_NAME = "College";
//    static final String STUDENTS_TABLE_NAME = "students";
//    static final int DATABASE_VERSION = 1;
//    static final String CREATE_DB_TABLE =
//            " CREATE TABLE " + STUDENTS_TABLE_NAME +
//                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    " name TEXT NOT NULL, " +
//                    " grade TEXT NOT NULL);";

    @Override
    public boolean onCreate() {
        Context context = getContext();
        LatLngDatabase dbHelper = new LatLngDatabase(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(	LatLngDatabase.TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        if (rowID >= 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(LatLngDatabase.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case RUNS:
                break;

            case RUN_ID:
                qb.appendWhere( LatLngDatabase.COLUMN_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case RUNS:
                count = db.delete(LatLngDatabase.TABLE_NAME, selection, selectionArgs);
                break;

            case RUN_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( LatLngDatabase.TABLE_NAME, LatLngDatabase.COLUMN_ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case RUNS:
                count = db.update(LatLngDatabase.TABLE_NAME, values, selection, selectionArgs);
                break;

            case RUN_ID:
                count = db.update(LatLngDatabase.TABLE_NAME, values, LatLngDatabase.COLUMN_ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case RUNS:
                return "vnd.android.cursor.dir/vnd.example.students";

            /**
             * Get a particular student
             */
            case RUN_ID:
                return "vnd.android.cursor.item/vnd.example.students";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }


}