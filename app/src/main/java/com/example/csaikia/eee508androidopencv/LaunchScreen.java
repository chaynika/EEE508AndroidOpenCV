package com.example.csaikia.eee508androidopencv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Interpolator;

public class LaunchScreen extends AppCompatActivity {

    Boolean lineDet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
    }



    public void startCameraActivityLineDet(View v){

        lineDet = true;
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("LineDetFlag",lineDet);
        startActivity(i);

    }


    public void startCameraActivityEdgeDet(View v){
        lineDet = false;
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("LineDetFlag",lineDet);
        startActivity(i);

    }
}