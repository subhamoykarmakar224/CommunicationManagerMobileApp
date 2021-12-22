package com.subhamoy.datamule.datamule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.subhamoy.datamule.datamule.helper.GetPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEnv();
    }

    private void initEnv() {
        // Runtime permission check
        GetPermissions.getRuntimePermissions(MainActivity.this);
    }
}