
package com.franktom.horizon;

import org.xmlpull.v1.XmlPullParser;

//import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
//import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.IntentFilter.AuthorityEntry;
import android.database.Cursor;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import java.io.BufferedInputStream;
//import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*

        // Defines a string to contain the selection clause
        String mSelectionClause = null;


        String mSortOrder = null;
        final ContentResolver resolver = getContentResolver();
        final String[] projection = { "_id", "NAME", "DESCRIPTION" };
        final String sa1 = "%A%"; // contains an "A"
        cursor = resolver.query(LocationsProvider.AUTHORITY, projection, People.NAME + " LIKE ?",
           new String[] { sa1 }, null);

        Cursor mCursor = getContentResolver().query(
                LocationsProvider.AUTHORITY,  // The content URI of the words table
                mProjection,                       // The columns to return for each row
                mSelectionClause,                   // Either null, or the word the user entered
                mSelectionArgs,                    // Either empty, or the string the user entered
                mSortOrder);                       // The sort order for the returned rows

            // Some providers return null if an error occurs, others throw an exception
            if (null == mCursor) {

            // If the Cursor is empty, the provider found no matches
            } else if (mCursor.getCount() == 0) {
                AlertDialog alertDialog;
                alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Packing List");
                alertDialog.setMessage("done");
                alertDialog.show();
            }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onButton1Clicked(View theButton){
        // A "projection" defines the columns that will be returned for each row

        String[] mProjection =
        {
            "_id",
            "NAME",
            "DESCRIPTION"
        };
        // Initializes an array to contain selection arguments

        Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");
        Cursor cursor = getContentResolver().query(uri, mProjection, null, null, null);

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Objects count");
        alertDialog.setMessage("Records: "+ cursor.getCount());
        alertDialog.show();
    }

    public void onButton2Clicked(View theButton){
        startActivity(new Intent(Intent.ACTION_INSERT, LocationsProvider.CONTENT_ID_URI_LOCATION));
        /*
        Bundle bundle = new Bundle();
        Intent intent = new Intent(MainActivity.this, EditLocationActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, EditLocationActivity.REQUEST_CODE);
        */
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Req code : "+requestCode, "Req code : "+requestCode);
        Log.i("Result code : "+resultCode, "Result code : "+resultCode);

        if (resultCode != EditLocationActivity.RESULTCODE_OK)
            return;

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Button2");
        alertDialog.setMessage("Title: "+ data.getCharSequenceExtra("Title"));
        alertDialog.show();

        //Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");

        ContentValues values = new ContentValues();
        values.put("NAME", data.getStringExtra("Title"));
        values.put("DESCRIPTION", data.getStringExtra("Description"));
        values.put("LATITUDE", data.getFloatExtra("Latitude",0));
        values.put("LONGITUDE", data.getFloatExtra("Longitude",0));

//        Uri newRecUri = getContentResolver().insert(uri, values);
    }

    public void onButton3Clicked(View theButton){
        Intent intent = new Intent(MainActivity.this, ObjectListActivity.class);
        startActivity(intent);
    }

    public void onButton4Clicked(View theButton){
        Intent intent = new Intent(MainActivity.this, CompassControlsActivity.class);
        startActivity(intent);

//        Intent intent = new Intent(MainActivity.this, CompassActivity.class);
//        startActivity(intent);
    }

    public void onButton5Clicked(View theButton){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Import");
        alert.setMessage("Enter file name on external storage:");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          Editable value = input.getText();
          importGpx(value.toString());
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Canceled.
          }
        });

        alert.show();
    }

//        Intent intent = new Intent(MainActivity.this, OpenFileActivity.class);
//        startActivityForResult(intent, 0);

    public void importGpx(String gpxPath){

         try {

             //FileInputStream fis = new FileInputStream("/data/user/h.xml");
             FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/"+gpxPath);
             InputStream in = new BufferedInputStream(fis);

             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);

             while (parser.next() != XmlPullParser.END_TAG) {
                 if (parser.getEventType() != XmlPullParser.START_TAG) {
                     continue;
                 }
                 String name = parser.getName();
                 if (name.equals("wpt")) {
                     parser.require(XmlPullParser.START_TAG, null, "wpt");
                     String lat = parser.getAttributeValue(null, "lat");
                     String lon = parser.getAttributeValue(null, "lon");
                     String alt = "";
                     String title = "";

                     String mname = "";
                     //parser.nextTag();
                     while (parser.next() != XmlPullParser.END_TAG) {
                         if (parser.getEventType() != XmlPullParser.START_TAG) {
                             continue;
                         }
                         String name2 = parser.getName();
                         if (name2.equals("name")) {
                             if (parser.next() == XmlPullParser.TEXT) {
                                 mname = parser.getText();
                                 parser.nextTag();
                                 parser.require(XmlPullParser.END_TAG, null, "name");
                                 break;
                             }
                         }
                     }


                     Pattern p = Pattern.compile("(.*)\\(([0-9]{1,4}).*\\).*");
                     Matcher m = p.matcher(mname);
                     if (m.find() && m.groupCount()==2)
                     {
                        title = m.group(1).trim();
                        alt = m.group(2);
                     }
                     else
                     {
                         title = mname;
                         alt = "0";
                     }

                     Log.e("XML", "<" + title + "> Alt:" + alt + "m Lat:" + lat + "N Lon:" + lon + "E");

                     ContentValues values = new ContentValues();
                     values.put("NAME", title);
                     values.put("DESCRIPTION", "Elevation: " + alt + ", GPS: " + lat.toString() + " " + lon.toString());

                     try {
                         values.put("LATITUDE", Float.parseFloat(lat.toString().replace(',', '.')));
                         values.put("LONGITUDE", Float.parseFloat(lon.toString().replace(',', '.')));
                         values.put("ELEVATION", Float.parseFloat(alt.replace(',', '.')));

                         getContentResolver().insert(LocationsProvider.CONTENT_ID_URI_LOCATION, values);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }

                     while (parser.nextTag() != XmlPullParser.END_TAG || !parser.getName().equals("wpt")) {}
                     parser.require(XmlPullParser.END_TAG, null, "wpt");

                 }
             }
         } catch( Exception e) {
             e.printStackTrace();
             AlertDialog alertDialog;
             alertDialog = new AlertDialog.Builder(this).create();
             alertDialog.setTitle("Error");
             alertDialog.setMessage("Import of <" + Environment.getExternalStorageDirectory().getPath()+"/"+gpxPath + "> failed. See log for more details");
             alertDialog.show();
         }
     }

    public void onButton6Clicked(View theButton){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Set GPS");
        alert.setMessage("Enter file name on external storage:");

        // Set an EditText view to get user input
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText input1 = new EditText(this);
        input1.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input1.setText(String.format("%f", 0.0));
        layout.addView(input1);

        final EditText input2 = new EditText(this);
        input2.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input1.setText(String.format("%f", 0.0));
        layout.addView(input2);

        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          float lat = Float.parseFloat(input1.getText().toString().replace(',', '.'));
          float lon = Float.parseFloat(input2.getText().toString().replace(',', '.'));
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Canceled.
          }
        });

        alert.show();
    }
}
