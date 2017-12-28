package com.aldi_andika.pidsimrpm;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LatihanSoal extends AppCompatActivity {

    final String TABLE_NAME = "tabelSoal";
    public String id, soal, jA, jB, jC;
    public int j, k=0, jawaban[], jawabanBenar[],skor=0,posisiCursor[],maks,jumlahSoal;
    List<Integer> listSoal;

    Cursor cursor;
    RadioGroup radio;
    ImageButton next, finish;
    TextView nomorSoal, soaltv, score;
    RadioButton a, b, c;
    ImageView soalGambar;
    RelativeLayout layskor;
    ScrollView laysoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_latihan_soal);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        jawaban = new int[10];
        jawabanBenar = new int[10];
        posisiCursor = new int[10];
        listSoal = new ArrayList<Integer>();
        maks = 5; //ganti jadi 100 kalo penuh
        jumlahSoal = 5;
        for (int i=0; i<maks; i++){
            listSoal.add(i);
        }
        Collections.shuffle(listSoal);

        layskor = (RelativeLayout) findViewById(R.id.leyoutskor);
        laysoal = (ScrollView) findViewById(R.id.leyoutsoal);
        score = (TextView) findViewById(R.id.tampilSkor);
        nomorSoal = (TextView) findViewById(R.id.nomorSoal);
        soaltv = (TextView) findViewById(R.id.soal);
        a = (RadioButton) findViewById(R.id.radioA);
        b = (RadioButton) findViewById(R.id.radioB);
        c = (RadioButton) findViewById(R.id.radioC);
        soalGambar = (ImageView) findViewById(R.id.soalGambar);
        finish = (ImageButton)findViewById(R.id.finish);
        next = (ImageButton)findViewById(R.id.next);
        finish.setVisibility(View.GONE);
        next.setEnabled(false);
//        soaltv.setTextSize(12);
        radio = (RadioGroup)findViewById(R.id.radioGrup);
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                Toast.makeText(LatihanSoal.this, ""+checkedId, Toast.LENGTH_SHORT).show();
                if(checkedId == -1)
                {
                    next.setEnabled(false);
                }
                else
                {
                    next.setEnabled(true);
                }
            }
        });

        DatabaseHelper myDbHelper = new DatabaseHelper(LatihanSoal.this);
        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            myDbHelper.openDataBase();

        }catch(SQLException sqle){

            throw sqle;

        }
//        Toast.makeText(LatihanSoal.this, "Success", Toast.LENGTH_SHORT).show();

        cursor = myDbHelper.query(TABLE_NAME, null,null,null,null,null,null);
//        cursor.moveToFirst();

        setSoal();
    }

    public void setSoal(){
        cursor.moveToPosition(listSoal.get(k));

        a.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        b.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        c.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        soalGambar.setVisibility(View.GONE);
        id = cursor.getString(0);
        soal = cursor.getString(1);
        jA = cursor.getString(2);
        jB = cursor.getString(3);
        jC = cursor.getString(4);

        if(id.equals("1")){
            soalGambar.setVisibility(View.VISIBLE);
            soalGambar.setImageResource(R.drawable.soalbergambarsatu);
        }
        if(id.equals("4"))
        {
            a.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.opsisatu100,0);
            b.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.opsidua100,0);
            c.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.opsitiga100,0);
        }
        nomorSoal.setText("Soal " + (k+1));
        soaltv.setText(soal);

        a.setText(jA);
        b.setText(jB);
        c.setText(jC);

    }

    public void next(View view){
        int selected = radio.getCheckedRadioButtonId();
        if (selected!=-1)
        {
            RadioButton selectedButton = (RadioButton)findViewById(selected);
            int index = radio.indexOfChild(selectedButton);
            jawaban[k] = index;
            j = Integer.parseInt(cursor.getString(5));
            jawabanBenar[k] = j;
            radio.clearCheck();

        }

        k++;
          if(k < jumlahSoal){
              setSoal();
        }else{
            for (int i=0; i<k; i++){
                if(jawabanBenar[i] == jawaban[i]){
                    skor = skor + 20;
                }
            }

              a.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
              b.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
              c.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
              next.setVisibility(View.GONE);
              laysoal.setVisibility(View.GONE);
              layskor.setVisibility(View.VISIBLE);
              score.setText(""+skor);

              finish.setVisibility(View.VISIBLE);
        }

    }

    public void HalamanMenu(View view){
        finish();
    }

}
