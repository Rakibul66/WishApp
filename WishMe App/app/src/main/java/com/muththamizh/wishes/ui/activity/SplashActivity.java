package com.muththamizh.wishes.ui.activity;

import static com.muththamizh.wishes.App.hasNetwork;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.muththamizh.wishes.R;
import com.muththamizh.wishes.ui.fragments.PermissionFragment;
import com.muththamizh.wishes.ui.fragments.SplashFragment;
import com.muththamizh.wishes.ui.fragments.WelcomeFragment;

public class SplashActivity extends AppCompatActivity implements SplashFragment.OnFragmentInteractionListener
,WelcomeFragment.OnFragmentInteractionListener, PermissionFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new SplashFragment()).commit();
        onView();

    }

    private void onView() {
        if (hasNetwork()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int pkcheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (pkcheck != PackageManager.PERMISSION_GRANTED) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new WelcomeFragment()).commit();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 2000);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection")
                    .setMessage(R.string.internet)
                    .setPositiveButton("okk", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).setIcon(R.drawable.ic_signal).show();
        }
    }



    @Override
        public void onFragmentInteraction (Uri uri){

    }

        private class Builder {
            public Builder(SplashActivity splashActivity) {
            }
        }
    }