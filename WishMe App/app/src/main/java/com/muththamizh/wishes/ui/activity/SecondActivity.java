package com.muththamizh.wishes.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.muththamizh.wishes.R;
import com.muththamizh.wishes.ui.fragments.FavouriteFragment;

import static com.muththamizh.wishes.App.hasNetwork;

public class SecondActivity extends AppCompatActivity implements FavouriteFragment.OnFragmentInteractionListener {
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        prepareLayout();
    }

    private void prepareLayout() {
        Toolbar toolbar = findViewById(R.id.toolbar_second);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Something is Wrong" + e, Toast.LENGTH_SHORT).show();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Favourite");
        getSupportFragmentManager().beginTransaction().replace(R.id.second_frame, new FavouriteFragment()).commit();
        if (hasNetwork()) {
            mAdView = findViewById(R.id.adView_second);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    mAdView.loadAd(new AdRequest.Builder().build());
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
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
    public void onBackPressed() {
        if (mAdView != null && mAdView.isShown()) {
            mAdView.destroy();
            super.onBackPressed();
            finish();
        } else {
            super.onBackPressed();
            finish();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null && mAdView.isShown()) {
            mAdView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null && mAdView.isShown()) {
            mAdView.resume();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {


    }
}
