package com.muththamizh.wishes.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muththamizh.wishes.utils.WallItem;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class FavoritesStorage {
    private final String STORAGE = "MY_FAVORITE";
    private SharedPreferences preferences;
    private Context context;

    public FavoritesStorage(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<WallItem> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("favoritesList", json);
        editor.apply();
    }

    public ArrayList<WallItem> loadFavorites() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("favoritesList", null);
        Type type = new TypeToken<ArrayList<WallItem>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}

