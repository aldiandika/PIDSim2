package com.aldi_andika.pidsimrpm;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SimulasiRpm extends AppCompatActivity {

    public static BluetoothAdapter mBluetooth = null;
    public static double nilaiRedaman;
    public int nilaiSp,nilaiKp,nilaiKi,nilaiKd,nilaiKpegas,nilaiKredaman,
            posSp,posKp,posKi,posKd,konversi;
    public static double lastError,error, setRpm, lastRpm,nilaiPos,ambilData;
    public static double P,D,I,nilaiPID = 0,lastPID=0,posisi;
    public int infinity = Integer.MAX_VALUE;
    public double x=0,itung,clearToSend,tampong;
    public boolean flagSim=false, flagProgress=false;
    public String PREFS_NAME = "simpanSementara",strDate;
    public LineGraphSeries<DataPoint> series,series2,series3;

    View decorView;
    DecimalFormat df;
    RotateAnimation r;
    SeekBar sp,Kp,Ki,Kd;
    RadioGroup toggle;
    RadioButton errorBtn,rpmBtn;
    EditText tampilSp,tampilKp,tampilKi,tampilKd;
    TextView tampilError,tampilRpm;
    GraphView graph,graph2;
    ImageButton start,stop,refresh,kirimData;
    Thread mulaiSim, mulaiAnim;
    InputStream mmInputStream;
    ToggleButton loger;
    ImageView wheel;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    public Thread workerThread;
    File gpxfile;
    int jancuk=0;
    private Toast mToastToShow;
    public int  startDegree=-1, endDegree=0;
    public double Arpm, rpm, speed = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_simulasi_rpm);
        decorView = getWindow().getDecorView();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        df = new DecimalFormat("#.00");

        wheel = (ImageView) findViewById(R.id.wheel);

        tampilError = (TextView)findViewById(R.id.error);
        tampilRpm = (TextView)findViewById(R.id.txt_rpm);

        start = (ImageButton)findViewById(R.id.start);
        stop = (ImageButton)findViewById(R.id.stop);
        refresh = (ImageButton)findViewById(R.id.reset);
        kirimData = (ImageButton)findViewById(R.id.kirimData) ;
        loger = (ToggleButton)findViewById(R.id.log);

        toggle = (RadioGroup)findViewById(R.id.toggle);
        errorBtn = (RadioButton)findViewById(R.id.errorBtn);
        rpmBtn = (RadioButton)findViewById(R.id.rpmBtn);

        sp = (SeekBar)findViewById(R.id.seekSp);
        Kp = (SeekBar)findViewById(R.id.seekKp);
        Ki = (SeekBar)findViewById(R.id.seekKi);
        Kd = (SeekBar)findViewById(R.id.seekKd);
        tampilSp = (EditText)findViewById(R.id.editSp);
        tampilKp = (EditText)findViewById(R.id.editKp);
        tampilKi = (EditText)findViewById(R.id.editKi);
        tampilKd = (EditText)findViewById(R.id.editKd);

        rpmBtn.setChecked(true);
        stop.setEnabled(false);

        SharedPreferences parameter = getSharedPreferences(PREFS_NAME,0);
        posSp = parameter.getInt("saveSp",nilaiSp); //nilai yang disimpen
        posKp = parameter.getInt("saveKp",nilaiKp);
        posKi = parameter.getInt("saveKi",nilaiKd);
        posKd = parameter.getInt("saveKd",nilaiKi);

        sp.setProgress(posSp);
        Kp.setProgress(posKp);
        Ki.setProgress(posKi);
        Kd.setProgress(posKd);

        tampilSp.setText(String.valueOf(posSp));
        tampilSp.setSelection(tampilSp.getText().length());

        tampilKp.setText(String.valueOf(posKp));
        tampilKp.setSelection(tampilKp.getText().length());

        tampilKi.setText(String.valueOf(posKi));
        tampilKi.setSelection(tampilKi.getText().length());

        tampilKd.setText(String.valueOf(posKd));
        tampilKd.setSelection(tampilKd.getText().length());

        loger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    showToast("Log saved in storage/PIDRPMSimu/" +gpxfile.getName(), 6000);
                } else {
                    msg("Log started");
                }
            }
        });

        sp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){



            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
//                double tampong;
                tampilSp.setText(String.valueOf(progress));
                speed = 1.7*progress;
//                Toast.makeText(Simulation.this, ""+posSp, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                if(Bluetooth.isBtConnected && (!start.isEnabled())){
                    new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
                }
            }
        });
        Kp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                tampilKp.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                if(Bluetooth.isBtConnected && (!start.isEnabled())){
                    new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
                }
            }
        });
        Ki.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                tampilKi.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                if(Bluetooth.isBtConnected && (!start.isEnabled())){
                    new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
                }
            }
        });
        Kd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                tampilKd.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                if(Bluetooth.isBtConnected && (!start.isEnabled())){
                    new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
                }
            }
        });

        tampilSp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((tampilSp.getText().toString()).matches("")){
                    sp.setProgress(0);
                }else {
                    nilaiSp = Integer.parseInt(tampilSp.getText().toString());
                    sp.setProgress(nilaiSp);
                }
                tampilSp.setSelection(tampilSp.getText().length());
                speed = 1.7*nilaiSp;
            }
        });
        tampilKp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((tampilKp.getText().toString()).matches("")){
                    Kp.setProgress(0);
                }else {
                    nilaiKp = Integer.parseInt(tampilKp.getText().toString());
                    Kp.setProgress(nilaiKp);
                }
                tampilKp.setSelection(tampilKp.getText().length());
            }
        });
        tampilKi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((tampilKi.getText().toString()).matches("")){
                    Ki.setProgress(0);
                }else {
                    nilaiKi = Integer.parseInt(tampilKi.getText().toString());
                    Ki.setProgress(nilaiKi);
                }
                tampilKi.setSelection(tampilKi.getText().length());
            }
        });
        tampilKd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((tampilKd.getText().toString()).matches("")){
                    Kd.setProgress(0);
                }else {
                    nilaiKd = Integer.parseInt(tampilKd.getText().toString());
                    Kd.setProgress(nilaiKd);
                }
                tampilKd.setSelection(tampilKd.getText().length());
            }
        });

        sp.setMax(120);
//        speed = 1.7*nilaiSp;

        graph = (GraphView)findViewById(R.id.graf);
        graph2 = (GraphView)findViewById(R.id.graf2);
        series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series2.setColor(Color.GREEN);
        series3 = new LineGraphSeries<>();
        series3.setColor(Color.RED);
        mulaiSim = new Thread();

        graph.getGridLabelRenderer().setNumHorizontalLabels(11);
        graph.getGridLabelRenderer().setNumVerticalLabels(11);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Waktu(s)");
        graph.getGridLabelRenderer().setVerticalAxisTitle("RPM(rad/m)");
        graph.getGridLabelRenderer().setLabelVerticalWidth(60);
        graph.getGridLabelRenderer().setLabelsSpace(5);

        graph2.getGridLabelRenderer().setNumHorizontalLabels(11);
        graph2.getGridLabelRenderer().setNumVerticalLabels(11);
        graph2.getGridLabelRenderer().setHorizontalAxisTitle("Waktu(s)");
        graph2.getGridLabelRenderer().setVerticalAxisTitle("RPM(rad/m)");
        graph2.getGridLabelRenderer().setLabelVerticalWidth(75);
        graph2.getGridLabelRenderer().setLabelsSpace(5);

        rpmBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    graph.setVisibility(View.VISIBLE);
                    graph2.setVisibility(View.GONE);
                    graph.addSeries(series);
                    graph.addSeries(series2);

                    graph.getGridLabelRenderer().setNumHorizontalLabels(11);
                    graph.getGridLabelRenderer().setNumVerticalLabels(11);
                    graph.getGridLabelRenderer().setHorizontalAxisTitle("Waktu(s)");
                    graph.getGridLabelRenderer().setVerticalAxisTitle("RPM(rad/m)");
                    graph.getGridLabelRenderer().setLabelVerticalWidth(60);
                    graph.getGridLabelRenderer().setLabelsSpace(5);
                    Viewport viewport = graph.getViewport();
                    viewport.setXAxisBoundsManual(true);
                    viewport.setMinX(0);
                    viewport.setMaxX(100);
                    viewport.setScalable(true);
                    viewport.setScrollableY(true);
                    viewport.setScrollable(true);
                }
            }
        });

        errorBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    graph2.setVisibility(View.VISIBLE);
                    graph.setVisibility(View.GONE);
                    graph2.addSeries(series3);
                    graph2.getGridLabelRenderer().setNumHorizontalLabels(11);
                    graph2.getGridLabelRenderer().setNumVerticalLabels(11);
                    graph2.getGridLabelRenderer().setHorizontalAxisTitle("Waktu(s)");
                    graph2.getGridLabelRenderer().setVerticalAxisTitle("RPM(rad/m)");
                    graph2.getGridLabelRenderer().setLabelVerticalWidth(75);
                    graph2.getGridLabelRenderer().setLabelsSpace(5);
                    Viewport viewport = graph2.getViewport();
                    viewport.setXAxisBoundsManual(true);
                    viewport.setMinX(0);
                    viewport.setMaxX(100);
                    viewport.setScalable(true);
                    viewport.setScrollableY(true);
                    viewport.setScrollable(true);
                }
            }
        });

        if(Bluetooth.isBtConnected){
            refresh.setEnabled(false);
            loger.setEnabled(true);
            Log.d("stat= ","yes");
        }
        else{
            refresh.setEnabled(false);
            loger.setEnabled(false);

        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        strDate = sdf.format(c.getTime());

        r = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setRepeatCount(Animation.INFINITE);
        r.setInterpolator(new LinearInterpolator());

    }

    public void kirimKirimData(View view){
        Intent activityDT = new Intent(SimulasiRpm.this,Bluetooth.class);
        startActivity(activityDT);
    }

    private void addEntry(){
        if(Bluetooth.isBtConnected) {
            try {
                nilaiSp = Integer.parseInt(tampilSp.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            setRpm = ambilData;
            error = nilaiSp - setRpm;
            if (loger.isChecked()){
                note(this, "Log "+strDate, "posisi = " + ambilData + " error = " + error);
            }

        }
        else {
            pid();
            setRpm = Double.parseDouble(df.format(rpm));
        }

        if(setRpm >= 120){setRpm = 120;}
        if(setRpm <= 0){setRpm = 0;}

//        error = Double.parseDouble(df.format(error));
        if(x <= 100){
            series.appendData(new DataPoint(x, setRpm),false,infinity);
            series2.appendData(new DataPoint(x,nilaiSp),false,infinity);
            series3.appendData(new DataPoint(x,error),false,infinity);
        }else{
            series.appendData(new DataPoint(x, setRpm),true,infinity);
            series2.appendData(new DataPoint(x,nilaiSp),true,infinity);
            series3.appendData(new DataPoint(x,error),true,infinity);
        }

        tampilError.setText(Double.toString(error));
        tampilRpm.setText(""+ setRpm);

        Log.d("x",""+x);
        x++;
        if(x ==  Integer.MAX_VALUE){
            x = 0;
        }

    }

    public void pid(){

        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
        nilaiKp = Integer.parseInt(tampilKp.getText().toString());
        nilaiKi = Integer.parseInt(tampilKi.getText().toString());
        nilaiKd = Integer.parseInt(tampilKd.getText().toString());

        error = nilaiSp - rpm;

        P = (nilaiKp* error)/100;

        D = ((nilaiKd*(error - lastError))/100);

        I = ((nilaiKi*(error + lastError))/100);
        lastError = error;

        nilaiPID = (P + I + D);
//        if(nilaiPID >= 100){nilaiPID = 100;}
//        if(nilaiPID <= 0){nilaiPID = 0;}

        Log.d("P",""+P);
        Log.d("error",""+error);
        Log.d("pid",""+lastPID);
        Log.d("prifat",""+nilaiPID);

        speed =lastPID;
        rpm = speed/3.6;

        rpm = Double.parseDouble(df.format(rpm));
        lastPID = lastPID + nilaiPID;
    }

    public void animasiA (){
        if(flagSim){
            tampilRpm.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Arpm = Double.parseDouble(tampilRpm.getText().toString());
                    r.setDuration((long)(1000*60/Arpm));
                }

                @Override
                public void afterTextChanged(Editable s) {
                    r.setDuration((long)(1000*60/Arpm));
                }
            });

            wheel.startAnimation(r);

        }
        else{
            r.cancel();
            r.reset();
        }

    }
    public void mulaiSimulasi(View view){
        if(rpmBtn.isChecked()){
            graph.setVisibility(View.VISIBLE);
            graph2.setVisibility(View.GONE);
            graph.addSeries(series);
            graph.addSeries(series2);
            graph.getGridLabelRenderer().setNumHorizontalLabels(11);
            graph.getGridLabelRenderer().setNumVerticalLabels(11);
            graph.getGridLabelRenderer().setHorizontalAxisTitle("Waktu");
            Viewport viewport = graph.getViewport();
            viewport.setXAxisBoundsManual(true);
            viewport.setMinX(0);
            viewport.setMaxX(100);
            viewport.setScalable(true);
            viewport.setScrollableY(true);
            viewport.setScrollable(true);
        }
        else
        {
            graph2.setVisibility(View.VISIBLE);
            graph.setVisibility(View.GONE);
            graph2.addSeries(series3);
            graph2.getGridLabelRenderer().setNumHorizontalLabels(11);
            graph2.getGridLabelRenderer().setNumVerticalLabels(11);
            graph2.getGridLabelRenderer().setHorizontalAxisTitle("Waktu");
            Viewport viewport = graph2.getViewport();
            viewport.setXAxisBoundsManual(true);
            viewport.setMinX(0);
            viewport.setMaxX(100);
            viewport.setScalable(true);
            viewport.setScrollableY(true);
            viewport.setScrollable(true);
        }

        if (Bluetooth.isBtConnected) {
            sendData("start\n");
            new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
            getData();
//            Log.d("stat= ","yes");
        }
        flagSim= true;
        mulaiSim = new Thread(new Runnable() {
            @Override
            public void run() {
                // we add 100 new entries
                while(flagSim){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            if(Bluetooth.isBtConnected){
//
////                                new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
//
////                                new get().execute();
//                            }
                            //addEntry();
//                            pid();
                            addEntry();

                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(200);//5
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        });
        mulaiSim.start();

        animasiA();


//        mulaiAnim = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(flagSim && !Bluetooth.isBtConnected){
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            if(Bluetooth.isBtConnected){
////
//////                                new send(tampilSp, tampilKp, tampilKi, tampilKd).execute();
////
//////                                new get().execute();
////                            }
//                            //addEntry();
////                            pid();
//                            animasiOffline();
//                        }
//                    });
//
//                    // sleep to slow down
//                    try {
//                        Thread.sleep(10);//5
//                    } catch (InterruptedException e) {
//                        // manage error ...
//                    }
//                }
//            }
//        });
//        mulaiAnim.start();

        view.setEnabled(false);
        stop.setEnabled(true);
        kirimData.setEnabled(false);
        refresh.setEnabled(false);
    }



    public void stopSimulasi(View view){
        flagSim = false;
        if (Bluetooth.isBtConnected) {
            stopWorker = true;
            sendData("stop\n");
            try {
                if(workerThread.isAlive())
                {
                    workerThread.join();
                }
                if(mulaiSim.isAlive()) {
                    mulaiSim.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            if(mulaiSim.isAlive()) {
                mulaiSim.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        animasiA();

        view.setEnabled(false);
        start.setEnabled(true);
        stop.setEnabled(false);
        refresh.setEnabled(true);
    }

    public void refreshing(View view){
        start.setEnabled(true);
        flagSim = false;
        tampilError.setText(""+0);
        tampilRpm.setText(""+0);
        try {
            if(mulaiSim.isAlive()) {
                mulaiSim.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        graph.removeAllSeries();
        graph2.removeAllSeries();
        graph = new GraphView(this);
        graph2 = new GraphView(this);
        graph2 = (GraphView)findViewById(R.id.graf2);
        graph = (GraphView)findViewById(R.id.graf);
        series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series2.setColor(Color.GREEN);
        series3 = new LineGraphSeries<>();
        series3.setColor(Color.RED);
        x=0;
        rpm=0;
        lastPID=0;
        if (Bluetooth.isBtConnected) {
            sendData("reset\n");
            stop.setEnabled(false);
//            stopWorker = true;
//            try {
//                if(workerThread.isAlive())
//                {
//                    workerThread.join();
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        kirimData.setEnabled(true);
        stop.setEnabled(false);
        refresh.setEnabled(false);
    }

    private void getData()
    {
        if (Bluetooth.btSocket!=null)
        {
            try
            {
//                if (btSocket.getInputStream().available();== ) {
//                    btSocket.getInputStream().av
//                }
                mmInputStream = Bluetooth.btSocket.getInputStream();
                final Handler handler = new Handler();
                final byte delimiter = 10; //This is the ASCII code for a newline character

                stopWorker = false;
                readBufferPosition = 0;
                readBuffer = new byte[1024];
                workerThread = new Thread(new Runnable()
                {
                    public void run()
                    {
                        while(!Thread.currentThread().isInterrupted() && !stopWorker)
                        {
                            try
                            {
                                int bytesAvailable = mmInputStream.available();
                                if(bytesAvailable > 0)
                                {
                                    byte[] packetBytes = new byte[bytesAvailable];
                                    mmInputStream.read(packetBytes);
                                    for(int i=0;i<bytesAvailable;i++)
                                    {
                                        byte b = packetBytes[i];
                                        if(b == delimiter)
                                        {
                                            byte[] encodedBytes = new byte[readBufferPosition];
                                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                            final String data = new String(encodedBytes, "US-ASCII");
                                            readBufferPosition = 0;


                                            handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
//                                                    try {
                                                    ambilData = Double.parseDouble(data);
                                                    Log.d("Ambil", ""+ambilData);
//                                                        Thread.sleep(500);
//                                                    } catch (InterruptedException e) {
//                                                        e.printStackTrace();
//                                                    }
                                                }
                                            });
                                        }
                                        else
                                        {
                                            readBuffer[readBufferPosition++] = b;
                                        }
                                    }
                                }
                            }
                            catch (IOException ex)
                            {
                                stopWorker = true;
                            }
                        }
                    }
                });

                workerThread.start();
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private   void sendData(String s)
    {

        if (Bluetooth.btSocket!=null)
        {
            try
            {
                Bluetooth.btSocket.getOutputStream().write(s.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void Disconnect()
    {
        if (Bluetooth.btSocket!=null) //If the btSocket is busy
        {
            try
            {
                Bluetooth.btSocket.close(); //close connection
//                msg("Bluetooth Disable");
            }
            catch (IOException e)
            { msg("Cannot Disconnected");}
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public void showToast(String s, int toastDurationInMilliSeconds) {
        // Set the toast and duration
        mToastToShow = Toast.makeText(this, s, Toast.LENGTH_LONG);

        // Set the countdown to display the toast
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
                mToastToShow.show();
            }
            public void onFinish() {
                mToastToShow.cancel();
            }
        };

        // Show the toast and starts the countdown
        mToastToShow.show();
        toastCountDown.start();
    }

    public void note(Context context, String sFileName, String sBody) {
        try {
//            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            File root = new File(Environment.getExternalStorageDirectory(), "PIDRPMSimu");
            if (!root.exists()) {
                root.mkdirs();
            }
            gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(sBody+"\n\n");
            writer.flush();
            writer.close();
//            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class get extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class  send extends AsyncTask<Void, Void, Void> {

        EditText tampilSp, tampilKp, tampilKi, tampilKd;
        send(EditText tampilSP, EditText tampilKP, EditText tampilKI, EditText tampilKD){
            this.tampilSp = tampilSP;
            this.tampilKp = tampilKP;
            this.tampilKi = tampilKI;
            this.tampilKd = tampilKD;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendData(
                            tampilSp.getText().toString()+" "+
                                    tampilKp.getText().toString()+" "+
                                    tampilKi.getText().toString()+" "+
                                    tampilKd.getText().toString()+"\n");
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Bluetooth.isBtConnected){
            refresh.setEnabled(false);
            loger.setEnabled(true);
            tampilSp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3){}});
//            Log.d("stat= ","yes");
        }
        else{
            refresh.setEnabled(false);
            tampilSp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3){}});
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences parameter = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = parameter.edit();
        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
        nilaiKp = Integer.parseInt(tampilKp.getText().toString());
        nilaiKi = Integer.parseInt(tampilKi.getText().toString());
        nilaiKd = Integer.parseInt(tampilKd.getText().toString());
        editor.putInt("saveSp",nilaiSp);
        editor.putInt("saveKp",nilaiKp);
        editor.putInt("saveKi",nilaiKi);
        editor.putInt("saveKd",nilaiKd);
        editor.commit();
        flagSim = false;
        try {
            if(mulaiSim.isAlive()) {
                mulaiSim.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        animasiA();

        x=0;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled())
        {
            Disconnect();
            bluetoothAdapter.disable();
            msg("Bluetooth Disable");
        }
        finish();
    }

    public void HalamanMenu(View view){
        SharedPreferences parameter = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = parameter.edit();
        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
        nilaiKp = Integer.parseInt(tampilKp.getText().toString());
        nilaiKi = Integer.parseInt(tampilKi.getText().toString());
        nilaiKd = Integer.parseInt(tampilKd.getText().toString());
        editor.putInt("saveSp",nilaiSp);
        editor.putInt("saveKp",nilaiKp);
        editor.putInt("saveKi",nilaiKi);
        editor.putInt("saveKd",nilaiKd);
        editor.commit();
        flagSim = false;
        try {
            mulaiSim.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        animasiA();

        start.setEnabled(true);
        x=0;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled())
        {
            Disconnect();
            bluetoothAdapter.disable();
            msg("Bluetooth Disable");
        }
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }
}
