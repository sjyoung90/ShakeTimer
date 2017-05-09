package com.example.sjyoung.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShakeActivity extends Activity implements SensorEventListener{

    public static SensorManager mSensorManager;
    public static Sensor mAccelerometer;

    CountDownTimer timer;
    TextView tCountTxt;
    TextView shakeValue;

    int mShakeCount = 0;
    int resultShakeCount;
    long timeLength = 11*1000;
    long COUNT_DOWN_INTERVAL = 1000;
    int timeCount = 10;
    Boolean isCheckingTime = false;
    Boolean isStopState = false;

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private static final int SHAKE_THRESHOLD = 800;
    private long mShakeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeValue = (TextView) findViewById(R.id.value);
        tCountTxt = (TextView)findViewById(R.id.count_down);

        Button startBtn = (Button) findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                if (!isCheckingTime) {
                    isCheckingTime = true;

                    if (isStopState) {
                        isStopState = false;

                        timer = new myTimer((timeCount+1)*1000, COUNT_DOWN_INTERVAL, mShakeCount);
                        timer.start();

                    } else {
                        if(mShakeCount > 0) {
                            mShakeCount = 0;
                            shakeValue.setText(String.valueOf(mShakeCount));
                        }
                        timeCount = 10;
                        timer = new myTimer(timeLength, COUNT_DOWN_INTERVAL);
                        timer.start();
                    }
                }
            }
        });

        Button stopBtn = (Button) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                if (isCheckingTime) {
                    isCheckingTime = false;
                    isStopState = true;
                    timer.cancel();
                }
            }
        });

        Button resetBtn = (Button) findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                if (!isCheckingTime) {
                    isStopState = false;

                    mShakeCount = 0;
                    timeCount = 10;
                    tCountTxt.setText(String.valueOf(timeCount));
                    shakeValue.setText(String.valueOf(mShakeCount));

                    timer = new myTimer(timeLength, COUNT_DOWN_INTERVAL);
                }
            }
        });

        Button quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                ShakeActivity.this.finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            timer.cancel();
        } catch (Exception e) {}
        timer = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onStop() {
        super.onStop();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged (Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        if (isCheckingTime) {
            TextView countValue = (TextView) findViewById(R.id.value);
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                long currentTime = System.currentTimeMillis();
                long gabOfTime = (currentTime - lastTime);
                if (gabOfTime > 100) {
                    lastTime = currentTime;
                    speed = Math.abs(axisX + axisY + axisZ - lastX - lastY - lastZ) / gabOfTime * 10000;

                    if (speed > SHAKE_THRESHOLD) {
                        mShakeTime = currentTime;
                        mShakeCount++;
                        resultShakeCount = mShakeCount / 2;
                        countValue.setText(String.valueOf(resultShakeCount));
                    }

                    lastX = event.values[0];
                    lastY = event.values[1];
                    lastZ = event.values[2];
                }
            }
        }
    }

    public class myTimer extends CountDownTimer{
        public myTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public myTimer(long millisInFuture, long countDownInterval, int curShakeCount) {
            super(millisInFuture, countDownInterval);
            mShakeCount = curShakeCount;
        }

        @Override
        public void onFinish() {
            tCountTxt = (TextView)findViewById(R.id.count_down);
            tCountTxt.setText(String.valueOf(timeCount));

            isCheckingTime = false;

            AlertDialog.Builder alertDlg = new AlertDialog.Builder(ShakeActivity.this);
            alertDlg.setTitle("Result");
            alertDlg.setMessage("\n" + "shake count = " + resultShakeCount);
            alertDlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            alertDlg.show();
            timeCount = 10;
        }

        @Override
        public void onTick(long millisInFuture) {
            isCheckingTime = true;

            tCountTxt = (TextView)findViewById(R.id.count_down);
            tCountTxt.setText(String.valueOf(timeCount));
            if (timeCount >= 1) {
                timeCount--;
            }
        }
    }
}
