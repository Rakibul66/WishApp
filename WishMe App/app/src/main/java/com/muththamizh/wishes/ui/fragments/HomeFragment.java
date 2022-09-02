package com.muththamizh.wishes.ui.fragments;

import static com.muththamizh.wishes.App.hasNetwork;
import static com.muththamizh.wishes.utils.Constant.CHILD_DATABASE;
import static com.muththamizh.wishes.utils.Constant.LOGO;
import static com.muththamizh.wishes.utils.Constant.NAME;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.muththamizh.wishes.R;
import com.muththamizh.wishes.adapters.CategoryAdapter;
import com.muththamizh.wishes.ui.activity.SecondActivity;
import com.muththamizh.wishes.utils.CategoryItem;
import com.muththamizh.wishes.utils.Constant;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class HomeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // private DatabaseReference mDatabaseRef;
    private View view;
    private String mParam1;
    private String mParam2;
    private CategoryAdapter adapter;
    private List<CategoryItem> exampleList;
    private AlertDialog progressDialog;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        initView();
        return view;
    }

    private void initView() {
        progressDialog = new SpotsDialog.Builder()
                .setContext(getActivity())
                .setTheme(R.style.Custom)
                .setCancelable(false)
                .setMessage("Please Wait...")
                .build();
        if (hasNetwork()) {
            refreshLayout = view.findViewById(R.id.swipeHomne);
            progressDialog.show();
            exampleList = new ArrayList<>();
            recyclerView = view.findViewById(R.id.recyeler_view);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            new LoadData(HomeFragment.this).execute();
            refreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue),
                    getResources().getColor(R.color.green), getResources().getColor(R.color.purple)
                    , getResources().getColor(R.color.orange));
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    exampleList.clear();
                    adapter.notifyDataSetChanged();
                    new LoadData(HomeFragment.this).execute();
                }
            });
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle("No Internet Connection")
                    .setMessage(R.string.internet)
                    .setPositiveButton("okk", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).setIcon(R.drawable.ic_signal).show();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_fav) {
            startActivity(new Intent(getActivity(), SecondActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_view, menu);
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
    }

    private static class LoadData extends AsyncTask<Void, Void, Void> {
        private WeakReference<HomeFragment> weakReference;

        LoadData(HomeFragment homeFragment) {
            this.weakReference = new WeakReference<>(homeFragment);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final HomeFragment weak = weakReference.get();
            if (weak == null || weak.isHidden()) {
                return null;
            }
            Constant.dataBaseReference.child(CHILD_DATABASE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String name = postSnapshot.child(NAME).getValue(String.class);
                        String logo = postSnapshot.child(LOGO).getValue(String.class);
                        weak.exampleList.add(new CategoryItem(name, logo).setViewType(1));
                    }
                    weak.adapter = new CategoryAdapter(weak.exampleList, weak.getActivity());
                    weak.recyclerView.setAdapter(weak.adapter);
                    weak.adapter.notifyDataSetChanged();
                    weak.progressDialog.dismiss();
                    weak.refreshLayout.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    weak.progressDialog.dismiss();
                    weak.refreshLayout.setRefreshing(false);
                    Toast.makeText(weak.getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
    }
}
