package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbHelper extends SQLiteOpenHelper {

    // Name of the database file
    public static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Query which is to be executed.
        String SQL_CREATE_TABLE = "CREATE TABLE " + ProductContract.NewEntry.TABLE_NAME + " ("
                + ProductContract.NewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.NewEntry.COLUMN_PRODUCT_NAME + " TEXT, "
                + ProductContract.NewEntry.COLUMN_PRICE + " INTEGER, "
                + ProductContract.NewEntry.COLUMN_QUANTITY + " INTEGER, "
                + ProductContract.NewEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + ProductContract.NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";

        // Execute the sql statement
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
