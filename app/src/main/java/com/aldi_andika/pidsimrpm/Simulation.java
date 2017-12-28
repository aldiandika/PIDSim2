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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Simulation extends AppCompatActivity {

    simulasi sim;
    public static BluetoothAdapter mBluetooth = null;
    public static double nilaiRedaman;
    public int nilaiSp,nilaiKp,nilaiKi,nilaiKd,nilaiKpegas,nilaiKredaman,
            posSp,posKp,posKi,posKd,posKpegas,posKredaman,konversi;
    public static double lastError,error,setPos,lastPos,nilaiPos,ambilData;
    public static double P,D,I,nilaiPID = 0,lastPID=50,posisi;
    public int infinity = Integer.MAX_VALUE;
    public double x=0,itung,clearToSend,tampong;
    public boolean flagSim=false, flagProgress=false;
    public String PREFS_NAME = "simpanan",strDate;
    public LineGraphSeries<DataPoint> series,series2,series3;

    SeekBar sp,Kp,Ki,Kd,Kpegas,Kredaman,objSim;
    RadioGroup toggle;
    RadioButton errorBtn,posisiBtn;
    EditText tampilSp,tampilKp,tampilKi,tampilKd,tampilKpegas,tampilKredaman;
    TextView tampilError,tampilPosisi;
    GraphView graph,graph2;
    ImageButton start,stop,refresh,kirim;
    Thread mulaiSim;
    InputStream mmInputStream;
    ToggleButton loger;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    public Thread workerThread;
    File gpxfile;
    int jancuk=0;
    private Toast mToastToShow;
    public static double nilaiPegas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_simulation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sim = (simulasi) findViewById(R.id.simulasi);

        tampilError = (TextView)findViewById(R.id.error);
        tampilPosisi = (TextView)findViewById(R.id.posisi);

        start = (ImageButton)findViewById(R.id.start);
        stop = (ImageButton)findViewById(R.id.stop);
        refresh = (ImageButton)findViewById(R.id.reset);
        kirim = (ImageButton)findViewById(R.id.kirim) ;
        loger = (ToggleButton)findViewById(R.id.log);

//        objSim = (SeekBar)findViewById(R.id.objekSimu);
        sp = (SeekBar)findViewById(R.id.seekSp);
        Kp = (SeekBar)findViewById(R.id.seekKp);
        Ki = (SeekBar)findViewById(R.id.seekKi);
        Kd = (SeekBar)findViewById(R.id.seekKd);
        Kpegas = (SeekBar)findViewById(R.id.seekKpegas);
        Kredaman = (SeekBar)findViewById(R.id.seekKredaman);
        tampilSp = (EditText)findViewById(R.id.editSp);
        tampilKp = (EditText)findViewById(R.id.editKp);
        tampilKi = (EditText)findViewById(R.id.editKi);
        tampilKd = (EditText)findViewById(R.id.editKd);
        tampilKpegas = (EditText)findViewById(R.id.editKpegas);
        tampilKredaman = (EditText)findViewById(R.id.editKredaman);
        toggle = (RadioGroup)findViewById(R.id.toggle);
        errorBtn = (RadioButton)findViewById(R.id.errorBtn);
        posisiBtn = (RadioButton)findViewById(R.id.posisiBtn);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        posisiBtn.setChecked(true);
        stop.setEnabled(false);

        SharedPreferences parameter = getSharedPreferences(PREFS_NAME,0);
        posSp = parameter.getInt("saveSp",nilaiSp); //nilai yang disimpen
        posKp = parameter.getInt("saveKp",nilaiKp);
        posKi = parameter.getInt("saveKi",nilaiKd);
        posKd = parameter.getInt("saveKd",nilaiKi);
        posKpegas = parameter.getInt("saveKpegas",nilaiKpegas);
        posKredaman = parameter.getInt("saveKredaman",nilaiKredaman);
//        objSim.setProgress(50);
        sp.setProgress(posSp);
        Kp.setProgress(posKp);
        Ki.setProgress(posKi);
        Kd.setProgress(posKd);
        Kpegas.setProgress(posKpegas);
        Kredaman.setProgress(posKredaman);

        tampilSp.setText(String.valueOf(posSp));
        tampilSp.setSelection(tampilSp.getText().length());

        tampilKp.setText(String.valueOf(posKp));
        tampilKp.setSelection(tampilKp.getText().length());

        tampilKi.setText(String.valueOf(posKi));
        tampilKi.setSelection(tampilKi.getText().length());

        tampilKd.setText(String.valueOf(posKd));
        tampilKd.setSelection(tampilKd.getText().length());

        tampilKpegas.setText(String.valueOf(posKpegas));
        tampilKpegas.setSelection(tampilKpegas.getText().length());

        tampilKredaman.setText(String.valueOf(posKredaman));
        tampilKredaman.setSelection(tampilKredaman.getText().length());

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
                if (fromUser) {
                    flagProgress = false;

                }
                if(Bluetooth.isBtConnected){

                    tampong = 4+ (progress *1);
                    Log.d("seek = ","p="+progress);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tampilSp.setText(String.valueOf((int) tampong));
                        }
                    });

                }
                else {
                    tampilSp.setText(String.valueOf(progress));
                }
//                Toast.makeText(Simulation.this, ""+posSp, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                flagProgress=true;
                jancuk=0;
                Log.d("pelag","stoptouch beibeh");
                if (Bluetooth.isBtConnected && (!start.isEnabled())) {
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
        Kpegas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                tampilKpegas.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
        Kredaman.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                tampilKredaman.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
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
                if (!Bluetooth.isBtConnected) {
                    if((tampilSp.getText().toString()).matches("")){
                        sp.setProgress(0);
                    }else {
                        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
                        sp.setProgress(nilaiSp);
                    }
                    tampilSp.setSelection(tampilSp.getText().length());
                } else {
                    if((tampilSp.getText().toString()).matches("")){
                        sp.setProgress(0);
                    }else {
                        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
//                        if (nilaiSp < 4){
//                            nilaiSp =4;
//                        }else if(nilaiSp>31){
//                            nilaiSp=31;
//                        }
                        Log.d("pelag =","f="+flagProgress+" jancuk="+jancuk);
                        if (!flagProgress) {
                            sp.setProgress((int)tampong-4);
                        } else {
                            if (jancuk==0) {
                                jancuk++;
                                sp.setProgress(nilaiSp);

                            } else{
                                sp.setProgress(nilaiSp-4);
                            }
                        }
                    }
                    tampilSp.setSelection(tampilSp.getText().length());
                }
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
        tampilKpegas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((tampilKpegas.getText().toString()).matches("")){
                    Kpegas.setProgress(0);
                }else {
                    nilaiKpegas = Integer.parseInt(tampilKpegas.getText().toString());
                    Kpegas.setProgress(nilaiKpegas);
                }
                tampilKpegas.setSelection(tampilKpegas.getText().length());
            }
        });
        tampilKredaman.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((tampilKredaman.getText().toString()).matches("")){
                    Kredaman.setProgress(0);
                }else {
                    nilaiKredaman = Integer.parseInt(tampilKredaman.getText().toString());
                    Kredaman.setProgress(nilaiKredaman);
                }
                tampilKredaman.setSelection(tampilKredaman.getText().length());
            }
        });

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
        graph.getGridLabelRenderer().setVerticalAxisTitle("Posisi(cm)");
        graph.getGridLabelRenderer().setLabelVerticalWidth(60);
        graph.getGridLabelRenderer().setLabelsSpace(5);

        graph2.getGridLabelRenderer().setNumHorizontalLabels(11);
        graph2.getGridLabelRenderer().setNumVerticalLabels(11);
        graph2.getGridLabelRenderer().setHorizontalAxisTitle("Waktu(s)");
        graph2.getGridLabelRenderer().setVerticalAxisTitle("Posisi(cm)");
        graph2.getGridLabelRenderer().setLabelVerticalWidth(75);
        graph2.getGridLabelRenderer().setLabelsSpace(5);

        posisiBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    graph.getGridLabelRenderer().setVerticalAxisTitle("Posisi(cm)");
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
                    graph2.getGridLabelRenderer().setVerticalAxisTitle("Posisi(cm)");
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
            Kpegas.setEnabled(false);
            Kredaman.setEnabled(false);
            tampilKpegas.setEnabled(false);
            tampilKredaman.setEnabled(false);
            refresh.setEnabled(false);
            loger.setEnabled(true);
            sp.setMax(27);
            Log.d("stat= ","yes");
        }
        else{
            Kpegas.setEnabled(true);
            Kredaman.setEnabled(true);
            tampilKpegas.setEnabled(true);
            tampilKredaman.setEnabled(true);
            refresh.setEnabled(false);
            loger.setEnabled(false);
            sp.setMax(100);
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        strDate = sdf.format(c.getTime());

//        oncreate udah

    }

    private void addEntry(){
        if(Bluetooth.isBtConnected) {
            Kpegas.setEnabled(false);
            Kredaman.setEnabled(false);
            tampilKpegas.setEnabled(false);
            tampilKredaman.setEnabled(false);
            try {
                nilaiSp = Integer.parseInt(tampilSp.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            setPos = ambilData;
            if(setPos <= 4){setPos = 4;}
            else if(setPos >= 31){setPos = 31;}
            konversi = (int)((100*setPos - 400)/27);
            sim.refresh((int) konversi);
            error = nilaiSp - konversi;
            if (loger.isChecked()){
                note(this, "Log "+strDate, "posisi = " + ambilData + " error = " + error);
            }

        }
        else {
            Kpegas.setEnabled(true);
            Kredaman.setEnabled(true);
            tampilKpegas.setEnabled(true);
            tampilKredaman.setEnabled(true);
            nilaiPegas = Integer.parseInt(tampilKpegas.getText().toString())/10;
//            nilaiRedaman = Integer.parseInt(tampilKredaman.getText().toString())/10;
            setPos = simulasi.x;
            pid();
            lastPos = setPos;

        }
//        setPos = objSim.getProgress();

        if(setPos >= 100){setPos = 100;}
        if(setPos <= 0){setPos = 0;}
        series.appendData(new DataPoint(x,setPos),true,infinity);
        series2.appendData(new DataPoint(x,nilaiSp),true,infinity);
        series3.appendData(new DataPoint(x,error),true,infinity);

        tampilError.setText(Double.toString(error));
        tampilPosisi.setText(""+setPos);
        x++;
        if(x ==  2147483647){
            x = 0;
        }

    }

    public void pid(){

        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
        nilaiKp = Integer.parseInt(tampilKp.getText().toString());
        nilaiKi = Integer.parseInt(tampilKi.getText().toString());
        nilaiKd = Integer.parseInt(tampilKd.getText().toString());
        nilaiKpegas = Integer.parseInt(tampilKpegas.getText().toString());
        nilaiKredaman = Integer.parseInt(tampilKredaman.getText().toString());

//        if(Bluetooth.isBtConnected){
//            nilaiPos = ambilData;
//        }
//        else{nilaiPos = simulasi.x;}
//        error = nilaiSp - ((sim.x*100)/sim.getMeasuredWidth());
        error = nilaiSp - simulasi.x;

        P = (nilaiKp* error)/200;

        D = ((nilaiKd*(error - lastError)/200) );

        I = ((nilaiKi*(error + lastError))/200);
        lastError = error;

        Log.d("P =",""+P);
        if(nilaiPID >= 100){nilaiPID = 100;}
        if(nilaiPID <= 0){nilaiPID = 0;}

        if(setPos > lastPos){nilaiRedaman = (double) (-1*nilaiKredaman)/50;}
        else if(setPos < lastPos){nilaiRedaman = (double) nilaiKredaman/50;}
        else{nilaiRedaman = 0;}
        nilaiPID = (P + I + D) + nilaiRedaman;

        lastPID = lastPID + nilaiPID;

        Log.d("PID = ",""+nilaiPID+" redaman = "+nilaiKredaman);
//        Log.d("x =",""+simulasi.x);
        sim.refresh((int)(lastPID));
//        lastPID = nilaiPID;
//        System.out.println(lastPID+" "+P);

    }

    //udah
    public void kirimKirim(View view){
        Intent activityDT = new Intent(Simulation.this,Bluetooth.class);
        startActivity(activityDT);
    }

    public void mulaiSimulasi(View view){
        if(posisiBtn.isChecked()){
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
                            addEntry();

                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }


                }
            }
        });
        mulaiSim.start();

        view.setEnabled(false);
        stop.setEnabled(true);
        kirim.setEnabled(false);
        refresh.setEnabled(false);
    }

    //udah
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

        view.setEnabled(false);
        start.setEnabled(true);
        stop.setEnabled(false);
        refresh.setEnabled(true);
    }

    //udah
    public void refreshing(View view){
        start.setEnabled(true);
        x=0;
        flagSim = false;
        tampilError.setText(""+0);
        tampilPosisi.setText(""+50);
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
        sim.refresh(50);
        lastPID=50;
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
        kirim.setEnabled(true);
        stop.setEnabled(false);
        refresh.setEnabled(false);
    }

    //udah
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
                                                        Log.d("data= ", "string= "+data+" double= "+ambilData);
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

    //udah
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

    //udah
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

    //udah
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    //udah
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

    //udah
    private class  send extends AsyncTask<Void, Void, Void>{

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

    //udah
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

    //udah
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

    public void HalamanMenu(View view){
        SharedPreferences parameter = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = parameter.edit();
        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
        nilaiKp = Integer.parseInt(tampilKp.getText().toString());
        nilaiKi = Integer.parseInt(tampilKi.getText().toString());
        nilaiKd = Integer.parseInt(tampilKd.getText().toString());
        nilaiKpegas = Integer.parseInt(tampilKpegas.getText().toString());
        nilaiKredaman = Integer.parseInt(tampilKredaman.getText().toString());
        editor.putInt("saveSp",nilaiSp);
        editor.putInt("saveKp",nilaiKp);
        editor.putInt("saveKi",nilaiKi);
        editor.putInt("saveKd",nilaiKd);
        editor.putInt("saveKpegas",nilaiKpegas);
        editor.putInt("saveKredaman",nilaiKredaman);
        editor.commit();
        flagSim = false;
        try {
            mulaiSim.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        view.setEnabled(false);
        start.setEnabled(true);
        x=0;
        finish();
    }

    //udah
    @Override
    protected void onResume() {
        super.onResume();
        if(Bluetooth.isBtConnected){
            Kpegas.setEnabled(false);
            Kredaman.setEnabled(false);
            tampilKpegas.setEnabled(false);
            tampilKredaman.setEnabled(false);
            refresh.setEnabled(false);
            loger.setEnabled(true);
            sp.setProgress(0);
            sp.setMax(27);
            tampilSp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2){}});
            Log.d("stat= ","yes");
        }
        else{
            Kpegas.setEnabled(true);
            Kredaman.setEnabled(true);
            tampilKpegas.setEnabled(true);
            tampilKredaman.setEnabled(true);
            refresh.setEnabled(false);
            sp.setProgress(0);
            sp.setMax(100);
            tampilSp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3){}});
        }
    }

    //udah
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences parameter = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = parameter.edit();
        nilaiSp = Integer.parseInt(tampilSp.getText().toString());
        nilaiKp = Integer.parseInt(tampilKp.getText().toString());
        nilaiKi = Integer.parseInt(tampilKi.getText().toString());
        nilaiKd = Integer.parseInt(tampilKd.getText().toString());
        nilaiKpegas = Integer.parseInt(tampilKpegas.getText().toString());
        nilaiKredaman = Integer.parseInt(tampilKredaman.getText().toString());
        editor.putInt("saveSp",nilaiSp);
        editor.putInt("saveKp",nilaiKp);
        editor.putInt("saveKi",nilaiKi);
        editor.putInt("saveKd",nilaiKd);
        editor.putInt("saveKpegas",nilaiKpegas);
        editor.putInt("saveKredaman",nilaiKredaman);
        editor.commit();
        flagSim = false;
        try {
            mulaiSim.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        x=0;

//        Bluetooth.myBluetooth.disable();
//        msg("Bluetooth Disable");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled())
        {
            Disconnect();
            bluetoothAdapter.disable();
            msg("Bluetooth Disable");
        }
        finish();
    }
}

