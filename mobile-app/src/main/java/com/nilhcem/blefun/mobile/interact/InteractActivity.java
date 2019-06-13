package com.nilhcem.blefun.mobile.interact;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.blefun.mobile.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class InteractActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_ADDRESS = "mAddress";

    private final GattClient mGattClient = new GattClient();
    private SeekBar seekBar;
    private Button bpmButton;
    private TextView bpmTextView;
    private List<Long> upTimeMillisQueue = new ArrayList<>();
    private double timeAverage = 500;


    private boolean bpmFeedbackIsRunning = false;
    BpmFeedback bpmFeedback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interact_activity);

        seekBar = findViewById(R.id.interact_seekbar);
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener());

        bpmButton = findViewById(R.id.interact_bpmButton);
        bpmButton.setEnabled(false);
        bpmButton.setOnClickListener(bpmButtonOnClickListener());

        bpmTextView = findViewById(R.id.interact_bpmText);
        bpmTextView.setEnabled(false);

        seekBar.setEnabled(true);
        bpmButton.setEnabled(true);
        bpmTextView.setEnabled(true);

        //bpmFeedback();

        bpmFeedbackIsRunning = true;
        bpmFeedback = new BpmFeedback();
        bpmFeedback.execute();




        String address = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        mGattClient.onCreate(this, address, new GattClient.OnCounterReadListener() {
            @Override
            public void onConnected(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setEnabled(success);
                        bpmButton.setEnabled(success);
                        bpmTextView.setEnabled(success);

                        if (!success) {
                            Toast.makeText(InteractActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGattClient.onDestroy();
    }

    protected class BpmFeedback extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Log.i("info", "Nuevo");
            runBpmFeedback();
            return null;
        }
    }

    protected void runBpmFeedback(){            //Worker used through an android Task
        while(bpmFeedbackIsRunning) {
            bpmTextView.setBackgroundColor(Color.BLACK);
            bpmTextView.setTextColor(Color.WHITE);

            long beforeDelay = SystemClock.uptimeMillis();

            /*
            while (bpmFeedbackIsRunning && SystemClock.uptimeMillis() - beforeDelay < timeAverage) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Log.e("ERR", "Thread refresh bpm feedback error");
                    Thread.currentThread().interrupt();
                    bpmFeedback.cancel(true);
                }
            }*/



            try {
                while (bpmFeedbackIsRunning && SystemClock.uptimeMillis() - beforeDelay < timeAverage) {
                    Thread.sleep(1);
                }
            }catch (InterruptedException ex) {
                Log.e("ERR", "Thread refresh bpm feedback error");
                bpmFeedbackIsRunning = false;
                bpmFeedback.cancel(true);
                Thread.currentThread().interrupt();
            }

            bpmTextView.setBackgroundColor(Color.TRANSPARENT);
            bpmTextView.setTextColor(Color.BLACK);

            beforeDelay = SystemClock.uptimeMillis();
            try {
                while (bpmFeedbackIsRunning && SystemClock.uptimeMillis() - beforeDelay < timeAverage) {
                    Thread.sleep(1);
                }
            }catch (InterruptedException ex) {
                Log.e("ERR", "Thread refresh bpm feedback error");
                bpmFeedbackIsRunning = false;
                bpmFeedback.cancel(true);
                Thread.currentThread().interrupt();
            }
        }
    }


/*
    private void bpmFeedback(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    bpmTextView.setBackgroundColor(Color.BLACK);
                    bpmTextView.setTextColor(Color.WHITE);

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException ex)
                    {
                        Log.e("ERR","Thread refresh bpm feedback error");
                        Thread.currentThread().interrupt();
                    }
                    bpmTextView.setBackgroundColor(Color.TRANSPARENT);
                    bpmTextView.setTextColor(Color.BLACK);

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException ex)
                    {
                        Log.e("ERR","Thread refresh bpm feedback error");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }
*/

    public SeekBar.OnSeekBarChangeListener seekBarChangeListener(){
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGattClient.writeInteractor(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    public View.OnClickListener bpmButtonOnClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upTimeMillisQueue.add(SystemClock.uptimeMillis());

                if(upTimeMillisQueue.size() == 5){
                    timeAverage = timeAverage();

                    refloat(upTimeMillisQueue);

                    bpmFeedbackIsRunning = false;
                    while(!bpmFeedback.isCancelled()){
                        bpmFeedback.cancel(true);
                    }

                    bpmFeedbackIsRunning = true;
                    bpmFeedback = new BpmFeedback();
                    bpmFeedback.execute();
                }

                bpmTextView.setText(Double.toString(60 / (timeAverage * 0.001)));
            }
        };
    }

    private static <T> void refloat(List<T> list){
        for(int i = 0 ; i < list.size() - 1; list.set(i, list.get(i++ + 1)));       //Move elements
        list.remove(list.size() - 1);       // Null the last element
    }

    private long timeAverage(){
        long res = 0;

        for(int i = 0; i < upTimeMillisQueue.size() - 1; i++){
            res += upTimeMillisQueue.get(i + 1) - upTimeMillisQueue.get(i);
        }

        return (upTimeMillisQueue.size() != 1)? res/(upTimeMillisQueue.size() - 1) : 120;
    }


}
