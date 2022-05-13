package com.oiqinc.wheretogo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.oiqinc.wheretogo.AutorizateSystem.PreferenceHelper;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void leave (View view){
        PreferenceHelper helper = new PreferenceHelper(Settings.this);
        helper.putIsLogin(false);
        Toast.makeText(this, "При перезаходе вас деавторизует!", Toast.LENGTH_SHORT).show();

    }
}