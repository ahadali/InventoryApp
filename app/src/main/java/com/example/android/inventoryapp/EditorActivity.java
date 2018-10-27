package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.NewEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    EditText productName, productCost, supplierName, supplierNumber, productQuantity;
    Button increaseQuantity, decreaseQuantity;
    /**
     * User can navigate to this activity in two ways
     * to know, how user came here, using currentUri which will
     * basically store received data from intent.
     */
    Uri currentUri;
    private boolean ProductChanged = false;
    // To know that the existing product details are changes or not.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent in = getIntent();
        currentUri = in.getData();

        if (currentUri == null) {
            // Floating Action Button was clicked
            setTitle("Add a Product");
            invalidateOptionsMenu();
        } else {
            // A list item was clicked
            setTitle("Edit Product");
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Binding of views
        productName = findViewById(R.id.in_product_name);
        productCost = findViewById(R.id.in_product_cost);
        supplierName = findViewById(R.id.in_supplier_name);
        supplierNumber = findViewById(R.id.in_supplier_number);
        productQuantity = findViewById(R.id.in_quantity);
        increaseQuantity = findViewById(R.id.quantity_plus);
        decreaseQuantity = findViewById(R.id.quantity_minus);
        // Buttons which increase or decrease the product quantity in edittext
        increaseQuantity.setOnClickListener(this);
        decreaseQuantity.setOnClickListener(this);
        productName.setOnTouchListener(mTouchListener);
        productCost.setOnTouchListener(mTouchListener);
        supplierNumber.setOnTouchListener(mTouchListener);
        supplierNumber.setOnTouchListener(mTouchListener);
        productQuantity.setOnTouchListener(mTouchListener);
        // Fill initial product quantity to zero.
        productQuantity.setText("0");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // Remove delete product option, if user is adding new product
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                insertProduct();
                return true;
            case android.R.id.home:
                if (!ProductChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButton = new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showMyDialog(discardButton);
                return true;
            case R.id.contact_supplier:
                String supplier_number = supplierNumber.getText().toString().trim();
                if (supplier_number.length() < 10) {
                    Toast.makeText(this, R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
                } else {
                    Intent in = new Intent(Intent.ACTION_DIAL);
                    in.setData(Uri.parse("tel:" + supplier_number));
                    if (in.resolveActivity(getPackageManager()) != null)
                        startActivity(in);
                }
                return true;
            case R.id.delete_product:
                // Confirmation for deletion of product
                showConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_dialog_message);
        builder.setPositiveButton(R.string.confirm_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
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

    private void deleteProduct() {
        if (currentUri != null) {
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.delete_failed_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.delete_success_message, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!ProductChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButton =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        showMyDialog(discardButton);
    }

    private void showMyDialog(DialogInterface.OnClickListener discardButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_dialog_message);
        builder.setPositiveButton(R.string.unsaved_positive_response, discardButton);
        builder.setNegativeButton(R.string.unsaved_negative_response, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void insertProduct() {
        ContentValues values = new ContentValues();
        String mProductName, mSupplierName, mSupplierNumber;
        int mProductCost, mProductQuantity;
        try {
            mProductName = productName.getText().toString().trim();
            mSupplierName = supplierName.getText().toString().trim();
            mSupplierNumber = supplierNumber.getText().toString().trim();
            mProductCost = Integer.parseInt(productCost.getText().toString().trim());
            mProductQuantity = Integer.parseInt(productQuantity.getText().toString().trim());
        } catch (Exception e) {
            Toast.makeText(this, R.string.invalid_values_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mSupplierNumber.length() != 10) {
            Toast.makeText(this, R.string.invalid_number_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mProductCost < 0 || mProductQuantity == 0) {
            Toast.makeText(this, R.string.invalid_message, Toast.LENGTH_SHORT).show();
            return;
        }

        values.put(NewEntry.COLUMN_PRODUCT_NAME, mProductName);
        values.put(NewEntry.COLUMN_SUPPLIER_NAME, mSupplierName);
        values.put(NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER, mSupplierNumber);
        values.put(NewEntry.COLUMN_PRICE, mProductCost);
        values.put(NewEntry.COLUMN_QUANTITY, mProductQuantity);
        if (currentUri == null) {
            Uri newUri = getContentResolver().insert(NewEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.insert_product_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.insert_product_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.update_product_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_product_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                NewEntry._ID,
                NewEntry.COLUMN_PRODUCT_NAME,
                NewEntry.COLUMN_PRICE,
                NewEntry.COLUMN_SUPPLIER_NAME,
                NewEntry.COLUMN_SUPPLIER_NAME,
                NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                NewEntry.COLUMN_QUANTITY};
        return new CursorLoader(this,
                currentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;
        if (cursor.moveToFirst()) {
            // BlackBoxing to avoid extra variables
            productName.setText(cursor.getString(cursor.getColumnIndex(NewEntry.COLUMN_PRODUCT_NAME)));
            supplierName.setText(cursor.getString(cursor.getColumnIndex(NewEntry.COLUMN_SUPPLIER_NAME)));
            supplierNumber.setText(cursor.getString(cursor.getColumnIndex(NewEntry.COLUMN_SUPPLIER_PHONE_NUMBER)));
            productCost.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(NewEntry.COLUMN_PRICE))));
            productQuantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(NewEntry.COLUMN_QUANTITY))));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productName.setText("");
        supplierName.setText("");
        supplierNumber.setText("");
        productCost.setText("");
        productQuantity.setText("");
    }

    @Override
    public void onClick(View v) {
        int quantity = Integer.valueOf(productQuantity.getText().toString().trim());
        switch (v.getId()) {
            case R.id.quantity_plus:
                productQuantity.setText(String.valueOf(quantity + 1));
                break;
            case R.id.quantity_minus:
                if (quantity > 0)
                    productQuantity.setText(String.valueOf(quantity - 1));
                else
                    productQuantity.setText("0");
        }
    }
}