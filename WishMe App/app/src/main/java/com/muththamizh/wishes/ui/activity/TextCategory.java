package com.muththamizh.wishes.ui.activity;

import static com.muththamizh.wishes.App.hasNetwork;
import static com.muththamizh.wishes.utils.Constant.LOGO;
import static com.muththamizh.wishes.utils.Constant.NAME;
import static com.muththamizh.wishes.utils.Constant.TEXT_WISHES;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.muththamizh.wishes.R;
import com.muththamizh.wishes.adapters.CategoryAdapter;
import com.muththamizh.wishes.utils.CategoryItem;
import com.muththamizh.wishes.utils.Constant;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class TextCategory extends AppCompatActivity {
    private CategoryAdapter adapter;
    private List<CategoryItem> exampleList;
    private AlertDialog progressDialog;
    private RecyclerView recyclerView;
    private AdView mAdView;
    private SwipeRefreshLayout refreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_category);
        Toolbar toolbar = findViewById(R.id.toolbar_text_cat);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.app_name);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Something is Wrong" + e, Toast.LENGTH_SHORT).show();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.Custom)
                .setCancelable(false)
                .setMessage("Please Wait...")
                .build();
        if (hasNetwork()) {
            refreshLayout = findViewById(R.id.swipeTextCategory);
            mAdView = findViewById(R.id.adView_text_cat);
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
            progressDialog.show();
            exampleList = new ArrayList<>();
            recyclerView = findViewById(R.id.recyler_cat);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(layoutManager);
            new LoadData(this).execute();
            refreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue),
                    getResources().getColor(R.color.green), getResources().getColor(R.color.purple)
                    , getResources().getColor(R.color.orange));
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    exampleList.clear();
                    adapter.notifyDataSetChanged();
                    new LoadData(TextCategory.this).execute();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_view, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        EditText et = (EditText) searchView.findViewById(R.id.search_src_text);
        et.setTextColor(getResources().getColor(R.color.black));
        et.setHint(getResources().getString(R.string.search));
        et.setBackground(getResources().getDrawable(R.drawable.search_view_background));
        et.setHintTextColor(getResources().getColor(R.color.black));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_fav) {
            startActivity(new Intent(TextCategory.this, SecondActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private static class LoadData extends AsyncTask<Void, Void, Void> {
        private WeakReference<TextCategory> weakReference;

        LoadData(TextCategory homeFragment) {
            this.weakReference = new WeakReference<>(homeFragment);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final TextCategory category=weakReference.get();
            if (category==null||category.isFinishing()){
                return null;
            }
            Constant.dataBaseReference.child(TEXT_WISHES).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String name = postSnapshot.child(NAME).getValue(String.class);
                        String logo = postSnapshot.child(LOGO).getValue(String.class);
                        category.exampleList.add(new CategoryItem(name, logo).setViewType(2));
                    }
                    category.adapter = new CategoryAdapter(category.exampleList, category);
                    category.recyclerView.setAdapter(category.adapter);
                    category.adapter.notifyDataSetChanged();
                    category.progressDialog.dismiss();
                    category.refreshLayout.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    category.progressDialog.dismiss();
                    category.refreshLayout.setRefreshing(false);
                    Toast.makeText(category, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
