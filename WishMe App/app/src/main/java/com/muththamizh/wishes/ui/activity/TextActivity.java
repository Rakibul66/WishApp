package com.muththamizh.wishes.ui.activity;

import static com.muththamizh.wishes.App.hasNetwork;
import static com.muththamizh.wishes.App.isValid;
import static com.muththamizh.wishes.utils.Constant.CALLED_NAME;
import static com.muththamizh.wishes.utils.Constant.IMAGE;
import static com.muththamizh.wishes.utils.Constant.NAME;
import static com.muththamizh.wishes.utils.Constant.TEXT_WISHES;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.muththamizh.wishes.R;
import com.muththamizh.wishes.adapters.WallAdapter;
import com.muththamizh.wishes.utils.Constant;
import com.muththamizh.wishes.utils.WallItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class TextActivity extends AppCompatActivity {
    private AdView mAdView;
    String Namee;
    private WallAdapter adapter;
    private RecyclerView recyclerView;
    private List<WallItem> list = new ArrayList<>();
    private AlertDialog progressDialog;
    private Integer item = 0;
    private Integer lines_beetween_ads = 8;
    private Boolean native_ads_enabled = false;
    LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        Toolbar toolbar = findViewById(R.id.toolbar_text);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Something is Wrong" + e, Toast.LENGTH_SHORT).show();
        }
        if (hasNetwork()) {
            refreshLayout = findViewById(R.id.swipeText);
            mAdView = findViewById(R.id.adViewText);
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
            if (getResources().getString(R.string.ADMOB_ADS_ENABLED_NATIVE).equals("true")) {
                native_ads_enabled = true;
                lines_beetween_ads = Integer.parseInt(getResources().getString(R.string.NATIVE_ADS_ITEM_BETWWEN_ADS));
            }
            Namee = getIntent().getStringExtra(CALLED_NAME);
            progressDialog = new SpotsDialog.Builder()
                    .setContext(this)
                    .setTheme(R.style.Custom)
                    .setCancelable(false)
                    .setMessage("Please Wait...")
                    .build();
            progressDialog.show();
            recyclerView = findViewById(R.id.TextRecyler);
            recyclerView.setHasFixedSize(true);
            linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter = new WallAdapter(list, this);
            recyclerView.setAdapter(adapter);
            new loadDataa(TextActivity.this).execute();
            refreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue),
                    getResources().getColor(R.color.green), getResources().getColor(R.color.purple)
                    , getResources().getColor(R.color.orange));
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    list.clear();
                    adapter.notifyDataSetChanged();
                    new loadDataa(TextActivity.this).execute();
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

        getSupportActionBar().setTitle(Namee);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    public static class loadDataa extends AsyncTask<Void, Void, Void> {
        private WeakReference<TextActivity> weakReference;

        loadDataa(TextActivity wallpaperViewActivity) {
            this.weakReference = new WeakReference<>(wallpaperViewActivity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final TextActivity activity=weakReference.get();
            if (activity==null||activity.isFinishing()){
                return null;
            }
            Constant.dataBaseReference.child(TEXT_WISHES).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (postSnapshot.child(NAME).getValue().equals(activity.Namee)) {
                            for (DataSnapshot img : postSnapshot.child(IMAGE).getChildren()) {
                                WallItem w = new WallItem();
                                w.setImage("" + img.getValue());
                                Log.w("IITEMVALUE", "" + img.getValue());
                                if (isValid("" + img.getValue())) {
                                    activity.list.add(w.setViewType(1));
                                } else {
                                    activity.list.add(w.setViewType(3));
                                }
                                if (activity.native_ads_enabled) {
                                    activity.item++;
                                    if (activity.item.equals(activity.lines_beetween_ads)) {
                                        activity.item = 0;
                                        if (activity.getResources().getString(R.string.NATIVE_ADS_TYPE).equals("admob")) {
                                            activity.list.add(new WallItem().setViewType(2));
                                        }
                                    }
                                }
                            }
                            activity.refreshLayout.setRefreshing(false);
                            activity.adapter.notifyDataSetChanged();
                            activity.progressDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    activity.refreshLayout.setRefreshing(false);
                    activity.progressDialog.dismiss();
                }
            });
            return null;
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
}
