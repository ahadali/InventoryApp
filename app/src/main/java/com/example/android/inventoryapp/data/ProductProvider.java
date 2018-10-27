package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class ProductProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the product table
     */
    public static final int PRODUCTS = 100;
    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    public static final int PRODUCT_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Addition of recognisable Uris to UriMatcher
    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    // Database Helper Object
    ProductDbHelper mProductDBHelper;

    @Override
    public boolean onCreate() {
        // Initialisation of Database Helper Object
        mProductDBHelper = new ProductDbHelper(getContext());
        return true;
    }

    // Read the Database
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Initialise readable database.
        SQLiteDatabase db = mProductDBHelper.getReadableDatabase();
        // cursor to store the result
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                // Read Table
                cursor = db.query(ProductContract.NewEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // Read a part of table
                selection = ProductContract.NewEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductContract.NewEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    // Return MIME type for uri
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                return ProductContract.NewEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.NewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI");
        }
    }

    // Delete data from table
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mProductDBHelper.getWritableDatabase();
        // Number of rows which will be deleted
        int numRowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                numRowsDeleted = db.delete(ProductContract.NewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductContract.NewEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = db.delete(ProductContract.NewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    // Update the data in table
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.NewEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported");
        }
    }

    // Insert new data in table
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalStateException("Insertion is not supported");
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        /**
         * Data Validation
         */
        if (values.getAsString(ProductContract.NewEntry.COLUMN_PRODUCT_NAME) == null)
            throw new IllegalArgumentException("Product Requires a name");
        if (values.getAsString(ProductContract.NewEntry.COLUMN_SUPPLIER_NAME) == null)
            throw new IllegalArgumentException("Product Supplier Requires a name");
        String phoneNumber = values.getAsString(ProductContract.NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (phoneNumber == null || phoneNumber.length() != 10)
            throw new IllegalArgumentException("Invalid Supplier Phone Number");
        Integer quantity = values.getAsInteger(ProductContract.NewEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity <= 0)
            throw new IllegalArgumentException("Invalid number of products");
        Integer cost = values.getAsInteger(ProductContract.NewEntry.COLUMN_PRICE);
        if (cost != null && cost <= 0)
            throw new IllegalArgumentException("Invalid cost of product");

        // initialise writable database
        SQLiteDatabase db = mProductDBHelper.getWritableDatabase();

        // insert data into table
        long id = db.insert(ProductContract.NewEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.v("insertProduct() method", "Insertion error");
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    // Update existing data in table
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        /**
         * Data Validation
         */
        if (values.containsKey(ProductContract.NewEntry.COLUMN_PRODUCT_NAME)) {
            if (values.getAsString(ProductContract.NewEntry.COLUMN_PRODUCT_NAME) == null)
                throw new IllegalArgumentException("Product Requires a name");
        }
        if (values.containsKey(ProductContract.NewEntry.COLUMN_SUPPLIER_NAME)) {
            if (values.getAsString(ProductContract.NewEntry.COLUMN_SUPPLIER_NAME) == null)
                throw new IllegalArgumentException("Product Supplier Requires a name");
        }
        if (values.containsKey(ProductContract.NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String phoneNumber = values.getAsString(ProductContract.NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (phoneNumber == null || phoneNumber.length() != 10)
                throw new IllegalArgumentException("Invalid Supplier Phone Number");
        }
        if (values.containsKey(ProductContract.NewEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductContract.NewEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity <= 0)
                throw new IllegalArgumentException("Invalid number of products");
        }
        if (values.containsKey(ProductContract.NewEntry.COLUMN_PRICE)) {
            Integer cost = values.getAsInteger(ProductContract.NewEntry.COLUMN_PRICE);
            if (cost != null && cost <= 0)
                throw new IllegalArgumentException("Invalid cost of product");
        }
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mProductDBHelper.getWritableDatabase();
        int rowsChanged = db.update(ProductContract.NewEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsChanged != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsChanged;
    }
}
