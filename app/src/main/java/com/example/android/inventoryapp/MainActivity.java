package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.ProductContract.NewEntry;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    FloatingActionButton fab;
    ListView listView;
    ProductCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), EditorActivity.class);
                startActivity(in);
            }
        });

        listView = findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.emptyView);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        listView.setEmptyView(emptyView);
        adapter = new ProductCursorAdapter(this, null);
        listView.setAdapter(adapter);
        // start loader in background thread
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent in = new Intent(getApplicationContext(), EditorActivity.class);
                // Launch editor activity on item click
                Uri currentUri = ContentUris.withAppendedId(NewEntry.CONTENT_URI, id);
                // Pass the data to know which item was clicked
                in.setData(currentUri);
                startActivity(in);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all:
                showConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_all_delete_message);
        builder.setPositiveButton(R.string.confirm_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(NewEntry.CONTENT_URI, null, null);
            }
        });
        builder.setNegativeButton(R.string.confirm_dialog_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Read the data which is necessary
        // so, using projection
        String[] projection = {
                NewEntry._ID,
                NewEntry.COLUMN_PRODUCT_NAME,
                NewEntry.COLUMN_PRICE,
                NewEntry.COLUMN_QUANTITY};
        return new CursorLoader(this,
                NewEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
