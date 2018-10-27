package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of products data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        TextView productNameView = view.findViewById(R.id.product_name);
        TextView productPriceView = view.findViewById(R.id.price);
        TextView productQuantityView = view.findViewById(R.id.quantity);
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.NewEntry.COLUMN_PRODUCT_NAME));
        int productPrice = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.NewEntry.COLUMN_PRICE));
        int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.NewEntry.COLUMN_QUANTITY));
        Button button = view.findViewById(R.id.button_sell);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.NewEntry._ID));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.NewEntry.COLUMN_QUANTITY));
                Uri currentUri = ContentUris.withAppendedId(ProductContract.NewEntry.CONTENT_URI, id);
                if (quantity > 1) {
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.NewEntry.COLUMN_QUANTITY, quantity - 1);
                    context.getContentResolver().update(currentUri, values, null, null);
                } else {
                    // If quantity is less than zero then show the toast message that this was the last product
                    context.getContentResolver().delete(currentUri, null, null);
                    Toast.makeText(context, R.string.last_product_message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        productNameView.setText(productName);
        productPriceView.setText(String.format("Price : Rs. %s", String.valueOf(productPrice)));
        productQuantityView.setText(String.format("Available : %s", String.valueOf(productQuantity)));
    }
}
