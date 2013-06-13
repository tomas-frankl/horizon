
package com.franktom.horizon;

import com.franktom.horizon.util.SystemUiHider;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class CompassControlsActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private CameraPreview mCameraPreview;
    private CompassView mSimulationView;

    private boolean isCameraStilled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_compass_controls);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        mCameraPreview = new CameraPreview(this);
        ((RelativeLayout)contentView).addView(mCameraPreview);

        mSimulationView = new CompassView(this);
        /*if (p.getHorizontalViewAngle()<360.0)
            mSimulationView.CAMERA_VIEW_ANGLE_HORIZONTAL = p.getHorizontalViewAngle();
        if (p.getVerticalViewAngle()<360.0)
            mSimulationView.CAMERA_VIEW_ANGLE_VERTICAL = p.getVerticalViewAngle();*/

        ((RelativeLayout)contentView).addView(mSimulationView);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;

                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate().translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        mSimulationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.buttonFreeze).setOnClickListener(new View.OnClickListener() {
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
                mSimulationView.setViewDirection(mSimulationView.getViewDirection()-1);
            }
        });
        findViewById(R.id.buttonScrollRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSimulationView.mFrozen = true;
                mSimulationView.setViewDirection(mSimulationView.getViewDirection()+1);
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
                }
            }
        });
        findViewById(R.id.buttonStillCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCameraStilled) {
                    mCameraPreview.camera.startPreview();
                    isCameraStilled = false;
                }
                else {
                    mCameraPreview.camera.stopPreview();
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
        delayedHide(100);
    }

    public void importGpx(String gpxPath, int category){

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_compass_controls, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.itemgpsauto:
                mSimulationView.updateCurrentLocation();
                return true;
            case R.id.itemgpsmanual:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Set GPS");
                alert.setMessage("Enter file name on external storage:");

                // Set an EditText view to get user input
                final LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText input1 = new EditText(this);
                input1.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input1.setText(String.format("%f", mSimulationView.getCurrentLocation().getLatitude()).replace('.', ','));
                layout.addView(input1);

                final EditText input2 = new EditText(this);
                input2.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input2.setText(String.format("%f", mSimulationView.getCurrentLocation().getLongitude()).replace('.', ','));
                layout.addView(input2);

                alert.setView(layout);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                  float lat = Float.parseFloat(input1.getText().toString().replace(',', '.'));
                  float lon = Float.parseFloat(input2.getText().toString().replace(',', '.'));

                  Location loc = new Location("");
                  loc.setLatitude(lat);
                  loc.setLongitude(lon);
                  mSimulationView.setCurrentLocation(loc);
                  }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                  }
                });

                alert.show();
                return true;
            case R.id.itemObjectAdd:
                startActivity(new Intent(Intent.ACTION_INSERT, LocationsProvider.CONTENT_ID_URI_LOCATION));
                return true;
            case R.id.itemObjectList:
                Intent intent = new Intent(CompassControlsActivity.this, ObjectListActivity.class);
                startActivity(intent);
                return true;
            case R.id.itemObjectsImport:
                AlertDialog.Builder importDlg = new AlertDialog.Builder(this);

                importDlg.setTitle("Import");
                importDlg.setMessage("Enter file name on external storage:");

                final LinearLayout importDlgLayout = new LinearLayout(this);
                importDlgLayout.setOrientation(LinearLayout.VERTICAL);

                String[] mTypesProjection = {"_id", "NAME"};
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
                return true;
            case R.id.itemObjectSummary:
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
                return true;
            default:
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

    Handler mHideHandler = new Handler();

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSimulationView.startSimulation();
        mCameraPreview.initCamera();
        mCameraPreview.camera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSimulationView.stopSimulation();
        mCameraPreview.camera.stopPreview();
    }

}
