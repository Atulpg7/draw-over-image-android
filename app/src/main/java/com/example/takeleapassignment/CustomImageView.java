package com.example.takeleapassignment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomImageView extends View {

    private Paint brush;
    private Path path;
    private Canvas mCanvas;
    private Bitmap image, workingBitmap;

    public float brushSize;
    public int brushColor;


    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void init() {
        brush = new Paint();//For changing color, strokes etc
        path = new Path();//For single path to store in list

        brushSize = 8; //Default size
        brushColor = Color.BLACK;//Default Color

        //Setting line properties
        brush.setAntiAlias(true);
        brush.setColor(brushColor);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(brushSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (workingBitmap == null) {
            workingBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(workingBitmap);
        }

        canvas.save();
        if (image != null) {
            mCanvas.drawBitmap(image, 0, 0, brush);
        }
        mCanvas.drawPath(path, brush);
        canvas.drawBitmap(workingBitmap, 0, 0, brush);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX, pointY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
            default:
                return false;
        }

        postInvalidate();
        return false;
    }

    @SuppressLint("InlinedApi")
    public void saveImage() {

        OutputStream outputStream;
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis()+".png");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        try {
            outputStream = getContext().getContentResolver().openOutputStream(uri);
            workingBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Toast.makeText(getContext(), "Image Saved :)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setBrushSize(float newSize) {
        brushSize = newSize;
        brush.setStrokeWidth(brushSize);
        invalidate();
    }

    public void setBrushColor(int color) {
        brushColor = color;
        brush.setColor(brushColor);
        invalidate();
    }

    public void setImage(Bitmap bitmap) {
        image = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), false);
        invalidate();
    }

    public void eraseAll() {
        path.reset();
        invalidate();
    }
}
