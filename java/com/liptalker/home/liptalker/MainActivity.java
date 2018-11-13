package com.liptalker.home.liptalker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.liptalker.home.liptalker.fragment.PeopleFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,
                new PeopleFragment()).commit();

    }
}
