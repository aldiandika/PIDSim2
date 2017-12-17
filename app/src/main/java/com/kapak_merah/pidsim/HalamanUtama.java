package com.kapak_merah.pidsim;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class HalamanUtama extends AppCompatActivity {
    Animation animFade, animTranslate;
    public static int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        animFade = AnimationUtils.loadAnimation(this, R.anim.anim_fade);
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

//        if(Bluetooth.myBluetooth.isEnabled())
//        {
//            Bluetooth.myBluetooth.disable();
//        }
    }


    public void dasarTeori(View view){
        Intent activityDT = new Intent(HalamanUtama.this,Teori.class);
        startActivity(activityDT);
    }

    public void simulate(View view){
        Intent activityDT = new Intent(HalamanUtama.this,Simulation.class);
        startActivity(activityDT);
    }

    public void latihanSoal(View view){
        Intent activityDT = new Intent(HalamanUtama.this,LatihanSoal.class);
        startActivity(activityDT);
    }

    public void about(View v){
        Intent activityDT = new Intent(HalamanUtama.this,About.class);
        startActivity(activityDT);
    }

    public void fade(View view){
        view.startAnimation(animFade);

    }

}
