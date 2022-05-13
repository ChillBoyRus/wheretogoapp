package com.oiqinc.wheretogo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.oiqinc.wheretogo.AutorizateSystem.RegisterActivity;

public class Loading extends AppCompatActivity {
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        text = findViewById(R.id.textloading);

        AlphaAnimation animation1 = new AlphaAnimation(0f, 1.0f);
        animation1.setDuration(4000);
        animation1.setStartOffset(0);
        animation1.setFillAfter(true);
        text.startAnimation(animation1);

        Intent nextactivityintent = new Intent(Loading.this, RegisterActivity.class);

        CountDownTimer nextactivity = new CountDownTimer(4000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startActivity(nextactivityintent);
            }
        }.start();
    }
}