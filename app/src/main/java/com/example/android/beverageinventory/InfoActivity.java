package com.example.android.beverageinventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.beverageinventory.data.ProductContract;
import com.example.android.beverageinventory.data.ProductDbHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.view.View.GONE;
import static com.example.android.beverageinventory.data.ProductDbHelper.LOG_TAG;

public class InfoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;

    private Uri mUriOfUploadedPic;

//    5 textviews responding to database value
    private TextView mNameField;
    private TextView mPriceField;
    private TextView mQuantityField;
    private TextView mSupplierField;

//    2 textviews related to change quantity
    private TextView mIncreseField;
    private TextView mDecreaseField;

//    2 views related to image
    private ImageView mImageField;
    private TextView mImageHint;

    private Uri mUriOfClickedProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Find all relevant views that we will need to read user input from
        mNameField = (TextView) findViewById(R.id.info_nameField);
        mPriceField = (TextView) findViewById(R.id.info_priceField);
        mQuantityField = (TextView) findViewById(R.id.info_quantityField);
        mSupplierField = (TextView) findViewById(R.id.info_supplierField);
        mIncreseField = (TextView) findViewById(R.id.info_increseField);
        mDecreaseField = (TextView) findViewById(R.id.info_decreseField);
        mImageField = (ImageView) findViewById(R.id.info_imageField);
        mImageHint = (TextView) findViewById(R.id.info_imageHint);

        mUriOfClickedProd = getIntent().getData();
        // if CatalogActivity didnt send any uri
        // then start InfoActivity in add mode
        if (mUriOfClickedProd == null){
            setTitle(R.string.title_add_prod);

            //
            LinearLayout updateField = (LinearLayout) findViewById(R.id.info_updateField);
            updateField.setVisibility(GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        // else CatalogActivity did send an uri
        // start EditorActivity in edit mode
        // and get details of clicked pet using CursorLoader
        else {
            setTitle(R.string.title_edit_prod);
            Log.i(LOG_TAG, "uri of the clicked pet is: " + mUriOfClickedProd);
        }

        // to select a image
        RelativeLayout imageField = (RelativeLayout) findViewById(R.id.image_field);
        imageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
//            // Respond to a click on the "Delete" menu option
//            case R.id.action_delete:
//                // Do nothing for now
//                showDeleteConfirmationDialog();
//                return true;
//            // Respond to a click on the "Up" arrow button in the app bar
//            case android.R.id.home:
//                // If the pet hasn't changed, continue with navigating up to parent activity
//                // which is the {@link CatalogActivity}.
//                if (!mPetHasChanged) {
//                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
//                    return true;
//                }

//                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
//                // Create a click listener to handle the user confirming that
//                // changes should be discarded.
//                DialogInterface.OnClickListener discardButtonClickListener =
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                // User clicked "Discard" button, navigate to parent activity.
//                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
//                            }
//                        };
//
//                // Show a dialog that notifies the user they have unsaved changes
//                showUnsavedChangesDialog(discardButtonClickListener);
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct(){
        String nameString = mNameField.getText().toString().trim();
        String priceString = mPriceField.getText().toString().trim();
        String quantityString = mQuantityField.getText().toString().trim();
        String supplierString = mSupplierField.getText().toString().trim();
        String uriString = mUriOfUploadedPic.toString();

        // TODO: sanity check

        // Create a new map of values, where column names are the keys
        ContentValues value = new ContentValues();
        value.put(ProductContract.ProductEntry.COLUMN_NAME, nameString);
        value.put(ProductContract.ProductEntry.COLUMN_PRICE, priceString);
        value.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantityString);
        value.put(ProductContract.ProductEntry.COLUMN_SUPPLIER, supplierString);
        value.put(ProductContract.ProductEntry.COLUMN_PICURI, uriString);


        ProductDbHelper mDbHelper = new ProductDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, value);
    }



    /**
     * I just take these 3 methods as blackbox
     * don't know anything bout how things work
     */
    // method to open image selector
    private void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // method to retrieve uri of the selected image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUriOfUploadedPic = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUriOfUploadedPic.toString());

                mImageField.setImageBitmap(getBitmapFromUri(mUriOfUploadedPic));
                mImageHint.setVisibility(View.INVISIBLE);
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }

    // method to set imageField to the selected pic using its uri
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageField.getWidth();
        int targetH = mImageField.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


}
