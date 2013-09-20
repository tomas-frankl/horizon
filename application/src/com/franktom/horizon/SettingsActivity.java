
package com.franktom.horizon;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    public static int REQUESTCODE   = 1002;
    public static int RESULTCODE_OK = 1;

    private SeekBar mElevation, mMinDistance, mMaxDistance, mHorizontalAngle, mVerticalAngle;
    private TextView mElevationValue, mDistanceRange, mHorizontalAngleValue, mVerticalAngleValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mElevation = (SeekBar) findViewById(R.id.sbElevation);
        mMinDistance = (SeekBar) findViewById(R.id.sbMinDistance);
        mMaxDistance = (SeekBar) findViewById(R.id.sbMaxDistance);
        mElevationValue =  (TextView) findViewById(R.id.valueElevation);
        mDistanceRange =  (TextView) findViewById(R.id.valueDistance);
        mHorizontalAngle = (SeekBar) findViewById(R.id.sbHorizontalAngle);
        mVerticalAngle = (SeekBar) findViewById(R.id.sbVerticalAngle);
        mHorizontalAngleValue = (TextView) findViewById(R.id.valueHorizontalAngle);
        mVerticalAngleValue = (TextView) findViewById(R.id.valueVerticalAngle);

        mElevation.setOnSeekBarChangeListener(this);
        mMinDistance.setOnSeekBarChangeListener(this);
        mMaxDistance.setOnSeekBarChangeListener(this);
        mHorizontalAngle.setOnSeekBarChangeListener(this);
        mVerticalAngle.setOnSeekBarChangeListener(this);

        int minElevation = getIntent().getExtras().getInt("minElevation");
        int maxElevation = (int)Math.ceil(getIntent().getExtras().getInt("maxElevation")/100.0)*100;
        int minDistance = getIntent().getExtras().getInt("minDistance");
        int maxDistance = getIntent().getExtras().getInt("maxDistance");
        float horizontalAngle = getIntent().getExtras().getFloat("horizontalAngle");
        float verticalAngle = getIntent().getExtras().getFloat("verticalAngle");

        mElevation.setMax((int)(Math.ceil(maxElevation/100.0)*100));
        mElevation.setProgress(minElevation);
        mMinDistance.setProgress(minDistance);
        mMaxDistance.setProgress(maxDistance);
        mHorizontalAngle.setProgress((int)((horizontalAngle-55f)*10));
        mVerticalAngle.setProgress((int)((verticalAngle-25f)*10));

        ((TextView)findViewById(R.id.valueMaxElevation)).setText(""+maxElevation+" m");
        ((TextView)findViewById(R.id.valueHalfMaxElevation)).setText(""+maxElevation/2+" m");
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sbElevation) {
            mElevationValue.setText(""+(int)(Math.ceil(progress/100)*100)+" m");
        } else if( seekBar.getId() == R.id.sbMinDistance ) {
            if (mMaxDistance.getProgress() - mMinDistance.getProgress() < 1)
                mMaxDistance.setProgress(mMinDistance.getProgress()+1);
            mDistanceRange.setText(""+mMinDistance.getProgress()+"-"+mMaxDistance.getProgress()+" km");
        } else if( seekBar.getId() == R.id.sbMaxDistance ) {
            if (mMaxDistance.getProgress() - mMinDistance.getProgress() < 1)
                mMinDistance.setProgress(mMaxDistance.getProgress()-1);
            mDistanceRange.setText(""+mMinDistance.getProgress()+"-"+mMaxDistance.getProgress()+" km");
        } else if( seekBar.getId() == R.id.sbHorizontalAngle ) {
            mHorizontalAngleValue.setText(String.format("%.1f dg", progress/10.0+55));
        } else if( seekBar.getId() == R.id.sbVerticalAngle ) {
            mVerticalAngleValue.setText(String.format("%.1f dg", progress/10.0+25));
        }
    }
    public void onStartTrackingTouch(SeekBar seekBar) {}
    public void onStopTrackingTouch(SeekBar seekBar) {}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }

    public void onOkButtonClicked(View v) {
        Intent result = new Intent();
        result.putExtra("minElevation", mElevation.getProgress());
        result.putExtra("minDistance", mMinDistance.getProgress());
        result.putExtra("maxDistance", mMaxDistance.getProgress());
        float ha = mHorizontalAngle.getProgress()/10.0f+55f;
        result.putExtra("horizontalAngle", ha);
        float va = mVerticalAngle.getProgress()/10.0f+25f;
        result.putExtra("verticalAngle", va);

        setResult(RESULTCODE_OK, result);
        finish();
    }

    public void onCancelButtonClicked(View v) {
        finish();
    }

}
