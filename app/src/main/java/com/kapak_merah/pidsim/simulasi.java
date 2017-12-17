package com.kapak_merah.pidsim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Tamu on 30/10/2016.
 */

public class simulasi extends ImageView{

    Bitmap background,objek;
    int screenWidth, y=0, k=0;
    public static double x=50;
    Paint red;
    public simulasi(Context context) {
        super(context);

//        if (!isInEditMode()) {
        screenWidth = HalamanUtama.screenWidth;
//        background = decodeBitmap(getResources(), R.drawable.sim,this.getMeasuredWidth(),this.getMeasuredWidth()/2);
//        background = decodeBitmap(getResources(), R.drawable.simul,400,100);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.bgsim);
        background = Bitmap.createScaledBitmap(background,screenWidth,screenWidth/2,false);
// bb = getResizedBitmap(background,200,400);

            objek = BitmapFactory.decodeResource(getResources(), R.drawable.sim_3);

            red = new Paint();
            red.setColor(Color.RED);
            red.setStyle(Paint.Style.FILL);
//        }

    }

    public simulasi(Context context, AttributeSet attrs){
        super(context,attrs);

//        if (!isInEditMode()) {
//        background = decodeBitmap(getResources(), R.drawable.sim,this.getMeasuredWidth()/2,this.getMeasuredWidth()/2);
//        background = decodeBitmap(getResources(), R.drawable.simul,100,100);
        screenWidth = HalamanUtama.screenWidth;
        Log.d("sW = ", ""+screenWidth);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.bgsim);
        Log.d("sW = ", ""+background.getWidth()+"nn"+background.getHeight());

        background = Bitmap.createScaledBitmap(background,screenWidth,screenWidth/2,false);
            objek = BitmapFactory.decodeResource(getResources(), R.drawable.sim_3);

            red = new Paint();
            red.setColor(Color.RED);
            red.setStyle(Paint.Style.FILL);
//        }
    }

    public simulasi(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);

//        if (!isInEditMode()) {
//        background = decodeBitmap(getResources(), R.drawable.sim,this.getMeasuredWidth(),this.getMeasuredWidth()/2);
//        background = decodeBitmap(getResources(), R.drawable.simul,100,100);
        screenWidth = HalamanUtama.screenWidth;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.bgsim);
        background = Bitmap.createScaledBitmap(background,screenWidth,screenWidth/2,false);
//        bb = getResizedBitmap(background,300,400);
            objek = BitmapFactory.decodeResource(getResources(), R.drawable.sim_3);


            red = new Paint();
            red.setColor(Color.RED);
            red.setStyle(Paint.Style.FILL);
//        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(this.getMeasuredWidth(), this.getMeasuredWidth()/2);
        this.setLayoutParams(lp);
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, background.getWidth(), background.getHeight()), new RectF(0, 0, this.getMeasuredWidth(), this.getMeasuredWidth()/2), Matrix.ScaleToFit.CENTER);
        Bitmap output = Bitmap.createBitmap(background, 0, 0, background.getWidth(), background.getHeight(), m, true);

//        canvas.drawBitmap(background,null,new Rect(0,0,this.getMeasuredWidth(), this.getMeasuredWidth()/2), null);
        canvas.drawBitmap(output,0,0,null);

        Matrix m1 = new Matrix();
        m1.setRectToRect(new RectF(0, 0, objek.getWidth(), objek.getHeight()), new RectF(0, 0, this.getMeasuredWidth()/4, this.getMeasuredWidth()/4), Matrix.ScaleToFit.CENTER);
        Bitmap output1 = Bitmap.createBitmap(objek, 0, 0, objek.getWidth(), objek.getHeight(), m1, true);
        if (x == 50) {
            canvas.drawBitmap(output1,((this.getMeasuredWidth()/2) - (this.getMeasuredWidth()/8)), ((this.getMeasuredWidth()/4) - (this.getMeasuredWidth()/21)),null);
        } else {
            int kordinatX = (int)(x*this.getMeasuredWidth()/100) - (this.getMeasuredWidth()/8);
            if(kordinatX < (this.getMeasuredWidth()/32)){
                kordinatX = this.getMeasuredWidth()/32;
            }else if (kordinatX > (this.getMeasuredWidth() - (this.getMeasuredWidth()/3) + (this.getMeasuredWidth()/16))){
                kordinatX = (this.getMeasuredWidth() - (this.getMeasuredWidth()/3) + (this.getMeasuredWidth()/16));
            }
            canvas.drawBitmap(output1, kordinatX,((this.getMeasuredWidth()/4) - (this.getMeasuredWidth()/21)), null);
//        Log.d("hasil = ",""+((float)x/100)*this.getMeasuredWidth());
        }
//        (this.getMeasuredWidth()/2) - (objek.getWidth()/2), (this.getMeasuredWidth()/4 - (objek.getWidth()/2))

//        canvas.drawBitmap(objek, ((this.getMeasuredWidth()/2) - (objek.getWidth()/2)), this.getMeasuredWidth()/4, null);
//        canvas.save();
//        Matrix matrix = new Matrix();
//        canvas.setMatrix(matrix);
//        canvas.drawCircle(0, 0, 48, red);
    }



    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth,scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, screenWidth, screenWidth/2, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth){
                inSampleSize *=2;
            }
        }
        return inSampleSize;
    }
    public Bitmap decodeBitmap(Resources res, int resId, int reqWidth, int reqHeight){

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public void refresh(double pos){
        x = pos;
        if (x>=0 && x< (this.getMeasuredWidth()-(this.getMeasuredWidth()/8))) {
            k++;
            double itung = pos -(Simulation.nilaiPegas*(Math.sin(k)));
            Double n = new Double(itung);
            x = n.intValue();
//            x = pos;
        }
        else if(x<0){
            x=1;
        }
        else if(x>(this.getMeasuredWidth()-(this.getMeasuredWidth()/8))){
            x=(this.getMeasuredWidth()-(this.getMeasuredWidth()/8))-1;
        }
        invalidate();
    }





}
