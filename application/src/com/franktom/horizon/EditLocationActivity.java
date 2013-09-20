
package com.franktom.horizon;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditLocationActivity extends Activity {
    private Uri mUri;
    private int mState;
    private Cursor mCursor;

    EditText title, description, latitude, longitude, elevation, category;

    static int REQUEST_CODE = 0;
    static int RESULTCODE_CANCEL = 0;
    static int RESULTCODE_OK = 1;

    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private static final String[] PROJECTION =
            new String[] {
                "locations._id",
                "locations.NAME",
                "locations.DESCRIPTION",
                "locations.LATITUDE",
                "locations.LONGITUDE",
                "locations.ELEVATION",
                "locations.TYPE"
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_location);

        title = (EditText) findViewById(R.id.editTextLocationTitle);
        description = (EditText) findViewById(R.id.editTextLocationDescription);
        latitude = (EditText) findViewById(R.id.editTextLocationLatitude);
        longitude = (EditText) findViewById(R.id.editTextLocationLongitude);
        elevation = (EditText) findViewById(R.id.editTextLocationElevation);
        category = (EditText) findViewById(R.id.editTextLocationCategory);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_EDIT.equals(action)) {

            // Sets the Activity state to EDIT, and gets the URI for the data to be edited.
            mState = STATE_EDIT;
            mUri = intent.getData();

            mCursor = getContentResolver().query(mUri,PROJECTION,null, null, null);
            mCursor.moveToFirst();
            title.setText(mCursor.getString(1));
            description.setText(mCursor.getString(2));
            latitude.setText(String.format("%f", mCursor.getFloat(3)));
            longitude.setText(String.format("%f", mCursor.getFloat(4)));
            elevation.setText(String.format("%f", mCursor.getFloat(5)));
            category.setText(String.format("%d", mCursor.getInt(6)));
            // For an insert or paste action:
        } else if (Intent.ACTION_INSERT.equals(action)) {

            // Sets the Activity state to INSERT, gets the general note URI, and inserts an
            // empty record in the provider
            mState = STATE_INSERT;
            mUri = intent.getData();
            //mUri = getContentResolver().insert(intent.getData(), null);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_edit_location, menu);
        return true;
    }

    public void okButtonClicked(View theButton){
        ContentValues values = new ContentValues();
        values.put("NAME", title.getText().toString());
        values.put("DESCRIPTION", description.getText().toString());

        values.put("LATITUDE", Float.parseFloat(latitude.getText().toString().replace(',', '.')));
        values.put("LONGITUDE", Float.parseFloat(longitude.getText().toString().replace(',', '.')));
        values.put("ELEVATION", Float.parseFloat(elevation.getText().toString().replace(',', '.')));
        values.put("TYPE", Integer.parseInt(category.getText().toString()));

        if (mState==STATE_EDIT) {

            getContentResolver().update(
                    mUri,    // The URI for the record to update.
                    values,  // The map of column names and new values to apply to them.
                    null,    // No selection criteria are used, so no where columns are necessary.
                    null     // No where columns are used, so no where arguments are necessary.
                );
            finish();
        }
        else if (mState==STATE_INSERT) {
            getContentResolver().insert(mUri, values);
            finish();
        }
    }

    public void cancelButtonClicked(View theButton){
        setResult(RESULTCODE_CANCEL);
        finish();
    }
}
