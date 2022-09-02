package com.muththamizh.wishes.ui.activity;

import static com.muththamizh.wishes.App.hasNetwork;
import static com.muththamizh.wishes.utils.Constant.PRIVACY_POLICY_LINK;
import static com.muththamizh.wishes.utils.Constant.SHARE_TEXT;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.muththamizh.wishes.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
, com.muththamizh.wishes.ui.fragments.HomeFragment.OnFragmentInteractionListener, com.muththamizh.wishes.ui.fragments.FavouriteFragment.OnFragmentInteractionListener {

    DrawerLayout drawer;
    NavigationView mNav;
    AlertDialog.Builder builder;
    private long backpressedTime;
    private Toast backToast;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareLayout();
    }

    private void prepareLayout() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        drawer = findViewById(R.id.drawer);
        mNav = findViewById(R.id.nav_View);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mNav.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new com.muththamizh.wishes.ui.fragments.HomeFragment()).commit();
        if (hasNetwork()) {
            mAdView = findViewById(R.id.adView);
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
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                try {
                    getSupportActionBar().setTitle(R.string.app_name);
                } catch (NullPointerException e) {
                    Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_contect:
                onContect();
                break;
            case R.id.nav_favourite:
                Intent intent = new Intent(this, SecondActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                onShare();
                break;
            case R.id.nav_policy:
                onPolicy();
                break;
            case R.id.nav_exit:
                onExit();
                break;
            case R.id.nav_send:
                onRateus();
                break;
            case R.id.nav_text:
                startActivity(new Intent(this, com.muththamizh.wishes.ui.activity.TextCategory.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onContect() {
        String[] Email = {getResources().getString(R.string.gmail_id)};
        String Subject = getResources().getString(R.string.app_name);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, Email);
        intent.putExtra(Intent.EXTRA_SUBJECT, Subject);
        startActivity(Intent.createChooser(intent, "Send Via"));
    }

    private void onRateus() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent gotoMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(gotoMarket);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName());
            Intent gotoMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(gotoMarket);
        }
    }

    private void onPolicy() {
        try {
            Intent policyintent = new Intent(Intent.ACTION_VIEW);
            policyintent.setData(Uri.parse(PRIVACY_POLICY_LINK));
            startActivity(policyintent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Enter Privacy Policy Link in Constant File", Toast.LENGTH_LONG).show();
        }
    }

    private void onExit() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are You Want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    private void onShare() {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, SHARE_TEXT + "https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Enter Share Text in Constant File", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (backpressedTime + 2000 > System.currentTimeMillis()) {
            if (mAdView != null && mAdView.isShown()) {
                mAdView.destroy();
            }
            backToast.cancel();
            super.onBackPressed();
            finishAffinity();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press Back Again To Exit", Toast.LENGTH_LONG);
            backToast.show();
        }
        backpressedTime = System.currentTimeMillis();
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
