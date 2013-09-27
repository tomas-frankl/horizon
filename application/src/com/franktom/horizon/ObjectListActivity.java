
package com.franktom.horizon;

import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ObjectListActivity extends ListActivity {

    private static final String[] PROJECTION = new String[] {
        "locations.NAME",
        "locations.DESCRIPTION",
        "locations._id",
    };

    /*static*/ private String mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(LocationsProvider.CONTENT_ID_URI_LOCATION);
        }

        //getListView().setOnCreateContextMenuListener(this);

        setFilter(mFilter);
    }

    private void setFilter(String filterText)
    {
        //String[] mSelectionArgs = {""};
        Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");
        Cursor mCursor = getContentResolver().query(uri, PROJECTION, filterText!=null?"NAME LIKE '%" + filterText + "%'":null, null, "NAME");

        //Cursor mCursor = this.getContentResolver().query(LocationsProvider.CONTENT_ID_URI_BASE, null, null, null, null);
        //startManagingCursor(mCursor);
        @SuppressWarnings("deprecation")
		ListAdapter adapter = new android.widget.SimpleCursorAdapter(
                this,
                R.layout.object_list_layout,
                //R.layout.two_line_list_item,
                mCursor,
                PROJECTION,
                new int[] {R.id.text1, R.id.text2});
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_object_list, menu);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), getListAdapter().getItemId(position));

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }

    public boolean onFilterEnabled(MenuItem item){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Filter");
        alert.setMessage("Text");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          Editable value = input.getText();
          mFilter = value.toString();
          setFilter(mFilter);

/*          Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");
          Cursor mCursor = getContentResolver().query(uri, PROJECTION, "NAME LIKE '%" + value.toString() + "%'", null, null);


          //Cursor mCursor = this.getContentResolver().query(LocationsProvider.CONTENT_ID_URI_BASE, null, null, null, null);
          //startManagingCursor(mCursor);
          ListAdapter adapter = new SimpleCursorAdapter(
                  getApplicationContext(),
                  R.layout.object_list_layout,
                  //R.layout.two_line_list_item,
                  mCursor,
                  PROJECTION,
                  new int[] {R.id.text1, R.id.text2}, 0);
          setListAdapter(adapter);*/
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Canceled.
          }
        });

        alert.show();

/*        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Filter dialog");
        alertDialog.setMessage("Filter");
        alertDialog.show();*/
        return true;
    }
}
