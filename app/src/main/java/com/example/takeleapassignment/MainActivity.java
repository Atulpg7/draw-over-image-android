package com.example.takeleapassignment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //Add image related variables
    private ImageView addImageIV;
    private Group addImageG;

    private CustomImageView customImageView;

    //Brush size related variables
    private SeekBar seekBar;
    private TextView brushSizeTV;
    private Group brushSizeG;

    //Bottom options related variables
    private ImageView brushSizeIV, blackColorIV, redColorIV, yellowColorIV, greenColorIV, blueColorIV,
            orangeColorIV, eraseAllIV,saveIV;
    private ImageView previousSelectedIV;

    //Permission related variables
    private final ActivityResultLauncher<String> galleryActivityLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            setImageInCustomView(result);
        }
    });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            galleryActivityLauncher.launch("image/*");
            Toast.makeText(getApplicationContext(), "Thanks for permissions", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please allow permissions !", Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariablesAndReferences();
        initializeClickActions();
    }

    private void initializeVariablesAndReferences() {

        addImageIV = findViewById(R.id.idIVAddImage);
        customImageView = findViewById(R.id.idCVMainView);
        seekBar = findViewById(R.id.idSBBrushSizeChanger);
        brushSizeTV = findViewById(R.id.idTVBrushSize);
        brushSizeIV = findViewById(R.id.idIVBrush);
        blackColorIV = findViewById(R.id.idIVBlackColor);
        redColorIV = findViewById(R.id.idIVRedColor);
        yellowColorIV = findViewById(R.id.idIVYellowColor);
        greenColorIV = findViewById(R.id.idIVGreenColor);
        blueColorIV = findViewById(R.id.idIVBlueColor);
        orangeColorIV = findViewById(R.id.idIVOrangeColor);
        eraseAllIV = findViewById(R.id.idIVErase);
        saveIV = findViewById(R.id.idIVSave);
        brushSizeG = findViewById(R.id.idGBrushOptions);
        addImageG = findViewById(R.id.idGAddImage);

        seekBar.setMax(100);
        blackColorIV.setBackgroundResource(R.drawable.color_selected);
        previousSelectedIV = blackColorIV;
    }

    private void initializeClickActions() {

        //Add Image
        addImageIV.setOnClickListener(view -> {

            //Checking for permissions to pick image from gallery if not allowed then requesting user
            // to allow
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                galleryActivityLauncher.launch("image/*");
            } else {
                // Request permission from the user
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        //Brush size clicks
        brushSizeIV.setOnClickListener(view -> {
            int visibility = brushSizeG.getVisibility();
            if (visibility == View.VISIBLE) {
                brushSizeG.setVisibility(View.GONE);
            } else {
                brushSizeG.setVisibility(View.VISIBLE);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newSize = seekBar.getProgress();
                customImageView.setBrushSize(newSize);
                brushSizeTV.setText(String.valueOf(newSize));
            }
        });

        //Colors clicks
        blackColorIV.setOnClickListener(view -> highlightSelectedColor(blackColorIV, ContextCompat.getColor(getApplicationContext(), R.color.black)));

        redColorIV.setOnClickListener(view -> highlightSelectedColor(redColorIV, ContextCompat.getColor(getApplicationContext(), R.color.red)));

        yellowColorIV.setOnClickListener(view -> highlightSelectedColor(yellowColorIV, ContextCompat.getColor(getApplicationContext(), R.color.yellow)));

        greenColorIV.setOnClickListener(view -> highlightSelectedColor(greenColorIV, ContextCompat.getColor(getApplicationContext(), R.color.green)));

        blueColorIV.setOnClickListener(view -> highlightSelectedColor(blueColorIV, ContextCompat.getColor(getApplicationContext(), R.color.blue)));

        orangeColorIV.setOnClickListener(view -> highlightSelectedColor(orangeColorIV, ContextCompat.getColor(getApplicationContext(), R.color.orange)));

        //Other clicks
        eraseAllIV.setOnClickListener(view -> customImageView.eraseAll());

        saveIV.setOnClickListener(view-> customImageView.saveImage());
    }

    private void highlightSelectedColor(ImageView clickedColorIV, int color) {

        //Setting circle behind selected color and clearing the old one
        clickedColorIV.setBackgroundResource(R.drawable.color_selected);
        previousSelectedIV.setBackgroundResource(R.drawable.color_selector);
        previousSelectedIV = clickedColorIV;

        //Setting and updating color of lines
        customImageView.setBrushColor(color);
    }

    private void setImageInCustomView(Uri selectedImageURI) {
        //Setting image to the custom view so that we can draw on it
        Bitmap selectedImageBitmap = null;
        try {
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream;
        byteArrayOutputStream = new ByteArrayOutputStream();
        if (selectedImageBitmap != null) {
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        }

        customImageView.setVisibility(View.VISIBLE);
        addImageG.setVisibility(View.GONE);
        saveIV.setVisibility(View.VISIBLE);

        //Loading image after some millis delay to prevent crash because when we set visibility of
        // custom view to visible then it takes time to take width and height according to screen
        Bitmap finalSelectedImageBitmap = selectedImageBitmap;
        new Handler(Looper.myLooper()).postDelayed(() -> {
            customImageView.setImage(finalSelectedImageBitmap);
        }, 100);
    }
}