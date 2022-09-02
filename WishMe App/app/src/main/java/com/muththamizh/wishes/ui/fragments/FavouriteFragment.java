package com.muththamizh.wishes.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.muththamizh.wishes.R;
import com.muththamizh.wishes.adapters.WallAdapter;
import com.muththamizh.wishes.manager.FavoritesStorage;
import com.muththamizh.wishes.utils.WallItem;

import java.util.ArrayList;
import java.util.List;

import static com.muththamizh.wishes.App.hasNetwork;

public class FavouriteFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View view;
    private RecyclerView recyclerView;
    private WallAdapter adapter;
    private List<WallItem> exampleList = new ArrayList<>();
    private ImageView imageView;
    private TextView errorText;
    private Integer item = 0;
    private Integer lines_beetween_ads = 8;
    private Boolean native_ads_enabled = false;
    private SwipeRefreshLayout refreshLayout;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FavouriteFragment() {
        // Required empty public constructor
    }

    public static FavouriteFragment newInstance(String param1, String param2) {
        FavouriteFragment fragment = new FavouriteFragment();
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
        view = inflater.inflate(R.layout.fragment_favourite, container, false);
        initView();
        return view;
    }

    private void initView() {
        refreshLayout = view.findViewById(R.id.swipeFav);
        recyclerView = view.findViewById(R.id.recyeler_fav);
        imageView = view.findViewById(R.id.error);
        if (hasNetwork()) {
            if (getResources().getString(R.string.ADMOB_ADS_ENABLED_NATIVE).equals("true")) {
                native_ads_enabled = true;
                lines_beetween_ads = Integer.parseInt(getResources().getString(R.string.NATIVE_ADS_ITEM_BETWWEN_ADS));
            }
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter = new WallAdapter(exampleList, getActivity());
            recyclerView.setAdapter(adapter);
            errorText = view.findViewById(R.id.errortext);
            refreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue),
                    getResources().getColor(R.color.green), getResources().getColor(R.color.purple)
                    , getResources().getColor(R.color.orange));
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            });
            loadFavorites();
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

    private void loadFavorites() {
        final FavoritesStorage storageFavorites = new FavoritesStorage(getActivity().getApplicationContext());
        List<WallItem> wallpapers = storageFavorites.loadFavorites();
        if (wallpapers == null) {
            wallpapers = new ArrayList<>();
        }
        if (wallpapers.size() != 0) {
            exampleList.clear();
            for (int i = 0; i < wallpapers.size(); i++) {
                WallItem a = new WallItem();
                a = wallpapers.get(i);
                Log.w("TAG", "loadFavorites: " + a);
                exampleList.add(a.setViewType(1));
                if (native_ads_enabled) {
                    item++;
                    if (item.equals(lines_beetween_ads)) {
                        item = 0;
                        if (getResources().getString(R.string.NATIVE_ADS_TYPE).equals("admob")) {
                            exampleList.add(new WallItem().setViewType(2));
                        }
                    }
                }
            }
            imageView.setVisibility(View.GONE);
            errorText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            Log.w("TAG", "loadFavorites: No Wallpapers");
            imageView.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
