package com.shahidfoy.phoneactivities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    private TextView mTextMessage;

    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean isTorchOn;
    private MediaPlayer mp;
    // private Button mTorchOnOffButton;
    private ImageView imageView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    try {
                        if(isTorchOn) {
                            turnOffFlashLight();
                            isTorchOn = false;
                        } else {
                            turnOnFlashLight();
                            isTorchOn = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    imageView.setImageResource(R.drawable.lightbulb);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    //toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
                    imageView.setImageResource(R.drawable.speaker);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);
                    imageView.setImageResource(R.drawable.phone);
                    return true;

                case R.id.navigation_bluetooth:
                    //mTextMessage.setText("Bluetooth");
                    //imageView.setImageResource(R.drawable.ic_bluetooth);
                    // add bluetooth stuff here
                    // finish();
                    Intent intent = new Intent(MainActivity.this, BluetoothMainActivity.class);
                    startActivity(intent);
                    // finish();
                    return true;


            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTorchOnOffButton = (Button) findViewById(R.id.mTorchOnOffButton);
        isTorchOn = true;


        imageView = (ImageView) findViewById(R.id.imageView);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Boolean isFlashAvailable = getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if(!isFlashAvailable) {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    System.exit(0);
                }
            });
            alert.show();
            return;
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        turnOnFlashLight();

        /*
        mTorchOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isTorchOn) {
                        turnOffFlashLight();
                        isTorchOn = false;
                    } else {
                        turnOnFlashLight();
                        isTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        */
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(isTorchOn){
            turnOffFlashLight();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isTorchOn){
            turnOffFlashLight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isTorchOn){
            turnOnFlashLight();
        }
    }
    


    public void turnOnFlashLight() {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
