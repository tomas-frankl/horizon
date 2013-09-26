
package com.franktom.horizon;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class CompassControlsActivity extends Activity implements CompassView.ZoomListener {
    private CameraPreview mCameraPreview;
    private CompassView mSimulationView;
    private Compass mCompass;

    private boolean isCameraStilled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_compass_controls);

        //final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        //controlsView.setVisibility(View.GONE);

        mCameraPreview = new CameraPreview(this);
        ((RelativeLayout)contentView).addView(mCameraPreview);

        mSimulationView = new CompassView(this);
        //mSimulationView.setClickable(true);

        ((RelativeLayout)contentView).addView(mSimulationView);

        mCompass = new Compass(this);
        mCompass.registerAzimutListener(mSimulationView);
        mSimulationView.registerZoomListener(this);

        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        mSimulationView.setMinDistance(sp.getFloat("min_distance", (float)mSimulationView.getMinDistance()));
        mSimulationView.setMaxDistance(sp.getFloat("max_distance", (float)mSimulationView.getMaxDistance()));
        mSimulationView.setMinElevation(sp.getFloat("min_elevation", (float)mSimulationView.getMinElevation()));
        mSimulationView.setHorizontalAngle(sp.getFloat("horizontal_angle", mSimulationView.getHorizontalAngle()));
        mSimulationView.setVerticalAngle(sp.getFloat("vertical_angle", mSimulationView.getVerticalAngle()));


        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.

//        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
//        mSystemUiHider.setup();
//        mSystemUiHider
//                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
//                    // Cached values.
//                    int mControlsHeight;
//
//                    int mShortAnimTime;
//
//                    @Override
//                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//                    public void onVisibilityChange(boolean visible) {
//                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//                            // If the ViewPropertyAnimator API is available
//                            // (Honeycomb MR2 and later), use it to animate the
//                            // in-layout UI controls at the bottom of the
//                            // screen.
//                            if (mControlsHeight == 0) {
//                                mControlsHeight = controlsView.getHeight();
//                            }
//                            if (mShortAnimTime == 0) {
//                                mShortAnimTime = getResources().getInteger(
//                                        android.R.integer.config_shortAnimTime);
//                            }
//                            controlsView.animate().translationY(visible ? 0 : mControlsHeight)
//                                    .setDuration(mShortAnimTime);
//                        } else */{
//                            // If the ViewPropertyAnimator APIs aren't
//                            // available, simply show or hide the in-layout UI
//                            // controls.
//                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
//                        }
//
//                        if (visible && AUTO_HIDE) {
//                            // Schedule a hide().
//                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
//                        }
//                    }
//                });
//
//        // Set up the user interaction to manually show or hide the system UI.
//        mSimulationView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (TOGGLE_ON_CLICK) {
//                    mSystemUiHider.toggle();
//                } else {
//                    mSystemUiHider.show();
//                }
//            }
//        });

/*
        mSimulationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.widget.Toast.makeText(getApplicationContext(), "aaa", android.widget.Toast.LENGTH_SHORT).show();
            }
        });*/

        mSimulationView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (isCameraStilled) {
                  mCameraPreview.resume();
                  isCameraStilled = false;
              }
              else {
                  mCameraPreview.pause();
                  isCameraStilled = true;
              }
          }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        /*findViewById(R.id.buttonFreeze).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSimulationView.mFrozen) {
                    mSimulationView.mFrozen = false;
                } else {
                    mSimulationView.mFrozen = true;
                }
            }
        });

        findViewById(R.id.buttonScrollLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSimulationView.mFrozen = true;
                mSimulationView.setViewDirection(mSimulationView.getViewDirection()-(mSimulationView.mCameraViewAngleHorizontal/10.0));
            }
        });
        findViewById(R.id.buttonScrollRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSimulationView.mFrozen = true;
                mSimulationView.setViewDirection(mSimulationView.getViewDirection()+(mSimulationView.mCameraViewAngleHorizontal/10.0));
            }
        });

        findViewById(R.id.buttonZoomIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCameraStilled) return;
                Camera.Parameters cp = mCameraPreview.camera.getParameters();
                if (cp.getZoom()+1 <= cp.getMaxZoom()) {
                    mSimulationView.setZoomRatio(cp.getZoomRatios().get(cp.getZoom()+1));
                    mCameraPreview.camera.startSmoothZoom(cp.getZoom()+1);
                    mCameraPreview.focus();
                }
            }
        });
        findViewById(R.id.buttonZoomOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCameraStilled) return;
                Camera.Parameters cp = mCameraPreview.camera.getParameters();
                if (cp.getZoom()-1 >= 0) {
                    mSimulationView.setZoomRatio(cp.getZoomRatios().get(cp.getZoom()-1));
                    mCameraPreview.camera.startSmoothZoom(cp.getZoom()-1);
                    mCameraPreview.focus();
                }
            }
        });*/
        findViewById(R.id.buttonStillCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCameraStilled) {
                    mCameraPreview.resume();
                    isCameraStilled = false;
                }
                else {
                    mCameraPreview.pause();
                    isCameraStilled = true;
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    private void onShowSummary()
    {
        String[] mTypesProjection = {"_id", "NAME"};
        String[] mProjection = {"locations._id", "locations.NAME" };
        Uri typesUri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "types");
        Cursor cursorTypes = getContentResolver().query(typesUri, mTypesProjection, null, null, null);
        cursorTypes.moveToFirst();
        String s = new String();

        while (!cursorTypes.isAfterLast()) {
            Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");
            int category = cursorTypes.getInt(0);
            Cursor cursor = getContentResolver().query(uri, mProjection, "TYPE="+category, null, null);
            s = s + cursorTypes.getString(1) + ": " + cursor.getCount() + "\n";
            cursorTypes.moveToNext();
        }

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Objects count");
        alertDialog.setMessage(s);
        alertDialog.show();
    }

    private float parseGPS(String s) {

        double f = 0;

        Pattern p = Pattern.compile("([0-9]{2,2}):([0-9]{2,2}):([0-9]{2,2})");
        Matcher m = p.matcher(s);
        if (m.find() && m.groupCount()==3)
        {
           f = Float.parseFloat(m.group(1))+(Float.parseFloat(m.group(2))+(Float.parseFloat(m.group(3))/60.0))/60.0;
        }

        return (float)f;
    }

    public void importGpx(String gpxPath, int category) {

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
                    String alt = "0";
                    String title = "";

                    String mname = "";
                    //parser.nextTag();
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        String name2 = parser.getName();
                        if (name2.equals("ele")) {
                            if (parser.next() == XmlPullParser.TEXT) {
                                alt = parser.getText();
                                parser.nextTag();
                                parser.require(XmlPullParser.END_TAG, null, "ele");
                            }
                        }

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
                    }

                    Log.e("XML", "<" + title + "> Alt:" + alt + "m Lat:" + lat + "N Lon:" + lon + "E");

                    ContentValues values = new ContentValues();
                    values.put("NAME", title);
                    values.put("DESCRIPTION", "Elevation: " + alt + ", GPS: " + lat.toString() + " " + lon.toString());

                    try {
                        if (lat.contains(":")) {
                            values.put("LATITUDE", parseGPS(lat));
                        } else {
                            values.put("LATITUDE", Float.parseFloat(lat.toString().replace(',', '.')));
                        }
                        if (lon.contains(":")) {
                            values.put("LONGITUDE", parseGPS(lon));
                        } else {
                            values.put("LONGITUDE", Float.parseFloat(lon.toString().replace(',', '.')));
                        }

                        values.put("ELEVATION", Float.parseFloat(alt.replace(',', '.')));
                        values.put("TYPE", category);


                        getContentResolver().insert(LocationsProvider.CONTENT_ID_URI_LOCATION, values);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //while (parser.nextTag() != XmlPullParser.END_TAG || !parser.getName().equals("wpt")) {}
                    while (parser.next() != XmlPullParser.END_TAG || !parser.getName().equals("wpt")) {}


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

    private void onSetGpsPositionManually() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Set GPS");
        alert.setMessage("Enter file name on external storage:");

        SharedPreferences sp = getPreferences(MODE_PRIVATE);

        // Set an EditText view to get user input
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText input1 = new EditText(this);
        input1.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input1.setText(String.format(Locale.US, "%f", sp.getFloat("position_lat", 0.0f)));
        layout.addView(input1);

        final EditText input2 = new EditText(this);
        input2.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input2.setText(String.format(Locale.US, "%f", sp.getFloat("position_lon", 0.0f)));
        layout.addView(input2);

        final EditText input3 = new EditText(this);
        input3.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input3.setText(String.format(Locale.US, "%f", sp.getFloat("position_ele", 0.0f)));
        layout.addView(input3);

        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {

          float lat = Float.parseFloat(input1.getText().toString());
          float lon = Float.parseFloat(input2.getText().toString());
          float ele = Float.parseFloat(input3.getText().toString());

          Location loc = new Location("");
          loc.setLatitude(lat);
          loc.setLongitude(lon);
          loc.setAltitude(ele);
          mSimulationView.setCurrentLocation(loc);

          SharedPreferences sp = getPreferences(MODE_PRIVATE);
          SharedPreferences.Editor spe = sp.edit();
          spe.putFloat("position_lat", lat);
          spe.putFloat("position_lon", lon);
          spe.putFloat("position_ele", ele);
          spe.commit();
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Canceled.
          }
        });

        alert.show();
    }

     private void onImportGpx() {
        AlertDialog.Builder importDlg = new AlertDialog.Builder(this);

        importDlg.setTitle("Import");
        importDlg.setMessage("Enter file name on external storage:");

        final LinearLayout importDlgLayout = new LinearLayout(this);
        importDlgLayout.setOrientation(LinearLayout.VERTICAL);

        String[] mTypesProjection = {"_id", "NAME", "VISIBLE"};
        Uri typesUri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "types");
        Cursor cursorTypes = getContentResolver().query(typesUri, mTypesProjection, null, null, null);
        final LocationTypesCombo combo = new LocationTypesCombo(this, cursorTypes);
        combo.setSuggestionSource("NAME");
        importDlgLayout.addView(combo);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        importDlgLayout.addView(input);


        importDlg.setView(importDlgLayout);
        importDlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            int id = combo.getId();
            if (id>=0)
            {
              Editable value = input.getText();
              importGpx(value.toString(), id);
              mSimulationView.reloadObjects(true);
            }
          }
        });

        importDlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Canceled.
          }
        });

        importDlg.show();
    }

    private void onReleaseCompass() {
        if (mSimulationView.mFrozen) {
            mSimulationView.mFrozen = false;
        } else {
            mSimulationView.mFrozen = true;
        }
    }

    private void onSettings() {
        /*AlertDialog.Builder settingsDlg = new AlertDialog.Builder(this);

        settingsDlg.setTitle("Settings");
        settingsDlg.setMessage("Minimum and maximum distance");

        final LinearLayout dlgLayout = new LinearLayout(this);
        dlgLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView t1 = new TextView(this);
        t1.setText("Min");
        dlgLayout.addView(t1);
        final NumberPicker np1 = new NumberPicker(this);
        np1.setMinValue(0);
        np1.setMaxValue(50);
        np1.setValue((int)mSimulationView.getMinDistance()/1000);
        dlgLayout.addView(np1);

        TextView t2 = new TextView(this);
        t2.setText("Max");
        dlgLayout.addView(t2);
        final NumberPicker np2 = new NumberPicker(this);
        np2.setMinValue(0);
        np2.setMaxValue(50);
        np2.setValue((int)mSimulationView.getMaxDistance()/1000);
        dlgLayout.addView(np2);

        TextView t3 = new TextView(this);
        t3.setText("Elev");
        dlgLayout.addView(t3);
        final SeekBar sb3 = new SeekBar(this);
        sb3.setMax(3000);
        sb3.setProgress((int)mSimulationView.getMinElevation());
        dlgLayout.addView(sb3);

        settingsDlg.setView(dlgLayout);

        settingsDlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            int min = np1.getValue();
            int max = np2.getValue();
            int ele = sb3.getProgress();
            mSimulationView.setMinDistance(min*1000);
            mSimulationView.setMaxDistance(max*1000);
            mSimulationView.setMinElevation((float)ele);
            mSimulationView.reloadObjects(true);
          }
        });

        settingsDlg.show();*/

        Intent intent = new Intent(CompassControlsActivity.this, SettingsActivity.class);
        intent.putExtra("minElevation", (int)mSimulationView.getMinElevation());
        intent.putExtra("maxElevation", (int)mSimulationView.getMaxElevation());
        intent.putExtra("minDistance", (int)(mSimulationView.getMinDistance()/1000));
        intent.putExtra("maxDistance", (int)(mSimulationView.getMaxDistance()/1000));
        intent.putExtra("horizontalAngle", mSimulationView.getHorizontalAngle());
        intent.putExtra("verticalAngle", mSimulationView.getVerticalAngle());

        startActivityForResult(intent, SettingsActivity.REQUESTCODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SettingsActivity.REQUESTCODE)
        {
            if (resultCode == SettingsActivity.RESULTCODE_OK)
            {
                int minElevation = data.getExtras().getInt("minElevation");
                int minDistance = data.getExtras().getInt("minDistance");
                int maxDistance = data.getExtras().getInt("maxDistance");
                float horizontalAngle = data.getExtras().getFloat("horizontalAngle");
                float verticalAngle = data.getExtras().getFloat("verticalAngle");

                mSimulationView.setMinElevation(minElevation);
                mSimulationView.setMinDistance(minDistance*1000);
                mSimulationView.setMaxDistance(maxDistance*1000);
                mSimulationView.setHorizontalAngle(horizontalAngle);
                mSimulationView.setVerticalAngle(verticalAngle);
                mSimulationView.setZoomRatio(100);
                //onZoomChange(0); //zoom to default value

                mSimulationView.reloadObjects(false);

                SharedPreferences sp = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();
                spe.putFloat("min_distance", (float)mSimulationView.getMinDistance());
                spe.putFloat("max_distance", (float)mSimulationView.getMaxDistance());
                spe.putFloat("min_elevation", (float)mSimulationView.getMinElevation());
                spe.putFloat("horizontal_angle", mSimulationView.getHorizontalAngle());
                spe.putFloat("vertical_angle", mSimulationView.getVerticalAngle());
                spe.commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_compass_controls, menu);
        Menu filterMenu = menu.addSubMenu("Filter");

        String[] mTypesProjection = {"_id", "NAME", "VISIBLE"};
        Uri typesUri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "types");
        Cursor cursorTypes = getContentResolver().query(typesUri, mTypesProjection, null, null, null);
        //final LocationTypesCombo combo = new LocationTypesCombo(this, cursorTypes);

        cursorTypes.moveToFirst();
        while (!cursorTypes.isAfterLast()) {
            MenuItem item = filterMenu.add(0, 1230 + cursorTypes.getInt(0), 0, cursorTypes.getString(1));
            item.setCheckable(true);
            item.setChecked(cursorTypes.getInt(2)!=0);
            cursorTypes.moveToNext();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.itemReleaseCompass:
                onReleaseCompass();
                return true;
            case R.id.itemSettings:
                onSettings();
                return true;
            case R.id.itemgpsauto:
                mSimulationView.updateCurrentLocation();
                return true;
            case R.id.itemgpsmanual:
                onSetGpsPositionManually();
                return true;
            case R.id.itemObjectAdd:
                startActivity(new Intent(Intent.ACTION_INSERT, LocationsProvider.CONTENT_ID_URI_LOCATION));
                return true;
            case R.id.itemObjectList:
                Intent intent = new Intent(CompassControlsActivity.this, ObjectListActivity.class);
                startActivity(intent);
                return true;
            case R.id.itemObjectsImport:
                onImportGpx();
                return true;
            case R.id.itemObjectSummary:
                onShowSummary();
                return true;
            default:
                if (item.getItemId()>=1230 && item.getItemId()<1250)
                {
                    item.setChecked(item.isChecked()?false:true);
                    Uri typeUri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/types/" + (item.getItemId()-1230));
                    ContentValues values = new ContentValues();
                    values.put("VISIBLE", item.isChecked()?1:0);
                    getContentResolver().update(typeUri, values, null, null);
                    mSimulationView.reloadObjects(true);
                }

                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    /*View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };*/

    /*Handler mHideHandler = new Handler();

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };*/

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    /*private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }*/


    @Override
    protected void onResume() {
        super.onResume();
        mCompass.start();
        mCameraPreview.initCamera();
        mCameraPreview.camera.startPreview();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mCompass.stop();
        mCameraPreview.camera.stopPreview();
    }

    public void onZoomChange(double zoom) {

        Camera.Parameters cp = mCameraPreview.camera.getParameters();
        Iterator<Integer> supportedZoomRatio = cp.getZoomRatios().iterator();

        Integer z = 0;
        Integer idx = 0;
        while(supportedZoomRatio.hasNext()) {
            z = supportedZoomRatio.next();
            if (zoom <= z) {
                break;
            }
            idx++;
        }

        if (idx >= cp.getZoomRatios().size()) {
            idx = cp.getZoomRatios().size()-1;
        }

        mSimulationView.setZoomRatio(z);
        mCameraPreview.camera.startSmoothZoom(idx);
        mCameraPreview.focus();

    }
}



