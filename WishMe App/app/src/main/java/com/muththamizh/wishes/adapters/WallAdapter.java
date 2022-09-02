package com.muththamizh.wishes.adapters;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.muththamizh.wishes.R;
import com.muththamizh.wishes.manager.FavoritesStorage;
import com.muththamizh.wishes.manager.PrefManager;
import com.muththamizh.wishes.utils.WallItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<WallItem> lovelist;
    private Context context;
    private boolean favorites = false;
    private static InterstitialAd mAd;
    DownloadTask downloadTask;
    ProgressDialog mProgressDialog;

    // A menu item view type.

    public WallAdapter(List<WallItem> lovelist, Context context) {
        this.lovelist = lovelist;
        this.context = context;
    }

    public WallAdapter(List<WallItem> lovelist, Context context, boolean favorites) {
        this.lovelist = lovelist;
        this.context = context;
        this.favorites = favorites;
    }

    @Override
    public int getItemCount() {
        return lovelist.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 1: {
                View v1 = inflater.inflate(R.layout.custom_image4, parent, false);
                viewHolder = new ImageViewHolder(v1);
                break;
            }
            case 2: {
                Log.w("Native Ads", "LAYOUT CALLS");
                View v2 = inflater.inflate(R.layout.item_admob_native_ads, parent, false);
                viewHolder = new AdmobNativeHolder(v2);
                break;
            }
            case 3: {
                View v3 = inflater.inflate(R.layout.custom_text_layout, parent, false);
                viewHolder = new TextHolder(v3);
                break;
            }
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (lovelist.get(position) == null) {
            return 1;
        } else {
            return lovelist.get(position).getViewType();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        switch (lovelist.get(position).getViewType()) {
            case 1: {
                final ImageViewHolder menuItemHolder = (ImageViewHolder) holder;
                Picasso.get()
                        .load(lovelist.get(position).getImage())
                        .placeholder(R.drawable.placeholderimage)
                        .into(menuItemHolder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                menuItemHolder.imageView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                menuItemHolder.imageView.setImageResource(R.drawable.ic_broken);
                            }
                        });
                final FavoritesStorage storageFavorites = new FavoritesStorage(context.getApplicationContext());
                List<WallItem> wallpapers = storageFavorites.loadFavorites();
                boolean exist = false;
                if (wallpapers == null) {
                    wallpapers = new ArrayList<>();
                }
                for (int i = 0; i < wallpapers.size(); i++) {
                    if (wallpapers.get(i).getImage().equals(lovelist.get(position).getImage())) {
                        exist = true;
                    }
                }
                if (!exist) {
                    menuItemHolder.favourite.setImageResource(R.drawable.ic_favorite);
                } else {
                    menuItemHolder.favourite.setImageResource(R.drawable.ic_favorite_done);
                }

                menuItemHolder.face.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.facebook.katana");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    intent.setPackage("com.facebook.lite");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                            Uri uri = Uri.parse(path);
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("image/jpeg");
                                            intent.setPackage("com.facebook.katana");
                                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                intent.setPackage("com.facebook.lite");
                                                try {
                                                    context.startActivity(intent);
                                                } catch (ActivityNotFoundException e) {
                                                    Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } catch (ClassCastException e) {
                                            Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                    Uri uri = Uri.parse(path);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("image/jpeg");
                                    intent.setPackage("com.facebook.katana");
                                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        intent.setPackage("com.facebook.lite");
                                        try {
                                            context.startActivity(intent);
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (ClassCastException e) {
                                    Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.facebook.katana");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    intent.setPackage("com.facebook.lite");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.facebook.katana");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    intent.setPackage("com.facebook.lite");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                menuItemHolder.twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.twitter.android");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                            Uri uri = Uri.parse(path);
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("image/jpeg");
                                            intent.setPackage("com.twitter.android");
                                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (ClassCastException e) {
                                            Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                    Uri uri = Uri.parse(path);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("image/jpeg");
                                    intent.setPackage("com.twitter.android");
                                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ClassCastException e) {
                                    Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.twitter.android");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.twitter.android");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                menuItemHolder.insta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                            Uri uri = Uri.parse(path);
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("image/jpeg");
                                            intent.setPackage("com.instagram.android");
                                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (ClassCastException e) {
                                            Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                    Uri uri = Uri.parse(path);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("image/jpeg");
                                    intent.setPackage("com.instagram.android");
                                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ClassCastException e) {
                                    Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

                menuItemHolder.whats.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.whatsapp");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                            Uri uri = Uri.parse(path);
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("image/jpeg");
                                            intent.setPackage("com.whatsapp");
                                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (ClassCastException e) {
                                            Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                    Uri uri = Uri.parse(path);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("image/jpeg");
                                    intent.setPackage("com.whatsapp");
                                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ClassCastException e) {
                                    Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.whatsapp");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.setPackage("com.whatsapp");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                menuItemHolder.favourite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final FavoritesStorage storageFavorites = new FavoritesStorage(context.getApplicationContext());
                        List<WallItem> wallpapers = storageFavorites.loadFavorites();
                        try {
                            List<WallItem> favorites_list = storageFavorites.loadFavorites();
                            Boolean exist = false;
                            if (favorites_list == null) {
                                favorites_list = new ArrayList<>();
                            }

                            for (int i = 0; i < favorites_list.size(); i++) {
                                if (favorites_list.get(i).getImage().equals(lovelist.get(position).getImage())) {
                                    exist = true;
                                }
                            }

                            if (!exist) {
                                ArrayList<WallItem> audios = new ArrayList<WallItem>();

                                for (int i = 0; i < favorites_list.size(); i++) {
                                    audios.add(favorites_list.get(i));
                                }
                                audios.add(lovelist.get(position));
                                storageFavorites.storeAudio(audios);
                                menuItemHolder.favourite.setImageResource(R.drawable.ic_favorite_done);

                            } else {
                                ArrayList<WallItem> new_favorites = new ArrayList<WallItem>();
                                for (int i = 0; i < favorites_list.size(); i++) {
                                    if (!favorites_list.get(i).getImage().equals(lovelist.get(position).getImage())) {
                                        new_favorites.add(favorites_list.get(i));
                                    }
                                }
                                if (favorites) {
                                    lovelist.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();
                                }
                                storageFavorites.storeAudio(new_favorites);
                                menuItemHolder.favourite.setImageResource(R.drawable.ic_favorite);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            try {
                                lovelist.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            } catch (IndexOutOfBoundsException ex) {
                                Log.d("Exepection", "" + ex);
                            }
                        }
                    }
                });
                menuItemHolder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                context.startActivity(Intent.createChooser(intent, "Share Image"));
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                            Uri uri = Uri.parse(path);
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("image/jpeg");
                                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                                            context.startActivity(Intent.createChooser(intent, "Share Image"));
                                        } catch (ClassCastException e) {
                                            Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                    Uri uri = Uri.parse(path);
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("image/jpeg");
                                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                                    context.startActivity(Intent.createChooser(intent, "Share Image"));
                                } catch (ClassCastException e) {
                                    Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                context.startActivity(Intent.createChooser(intent, "Share Image"));
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                Bitmap bitmap = ((BitmapDrawable) menuItemHolder.imageView.getDrawable()).getBitmap();
                                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
                                Uri uri = Uri.parse(path);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                context.startActivity(Intent.createChooser(intent, "Share Image"));
                            } catch (ClassCastException e) {
                                Toast.makeText(context, "Something Went Wrong: Image is Still Loading or Image not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                menuItemHolder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProgressDialog = new ProgressDialog(context);
                        mProgressDialog.setMessage("Downloading");
                        mProgressDialog.setIndeterminate(true);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadTask.cancel(true);
                            }
                        });
                        if (mAd == null) {
                            loadInstital();
                            downloadTask = new DownloadTask(WallAdapter.this);
                            downloadTask.execute(lovelist.get(position).getImage());
                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        downloadTask = new DownloadTask(WallAdapter.this);
                                        downloadTask.execute(lovelist.get(position).getImage());
                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                downloadTask = new DownloadTask(WallAdapter.this);
                                downloadTask.execute(lovelist.get(position).getImage());
                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            downloadTask = new DownloadTask(WallAdapter.this);
                            downloadTask.execute(lovelist.get(position).getImage());
                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            downloadTask = new DownloadTask(WallAdapter.this);
                            downloadTask.execute(lovelist.get(position).getImage());
                        }
                    }
                });
                break;
            }
            case 2: {
                Log.w("Native Ads", "BIND CALLS");
                final AdmobNativeHolder holderView = (AdmobNativeHolder) holder;
                holderView.adLoader.loadAd(new AdRequest.Builder().build());
                break;
            }
            case 3: {
                final TextHolder ItemHolder = (TextHolder) holder;
                ItemHolder.wishes.setText(lovelist.get(position).getImage());

                ItemHolder.copy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Copy", lovelist.get(position).getImage());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Copy SuccessFully...", Toast.LENGTH_SHORT).show();
                            } catch (NullPointerException e) {
                                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("Copy", lovelist.get(position).getImage());
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(context, "Copy SuccessFully...", Toast.LENGTH_SHORT).show();
                                        } catch (NullPointerException e) {
                                            Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Copy", lovelist.get(position).getImage());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(context, "Copy SuccessFully...", Toast.LENGTH_SHORT).show();
                                } catch (NullPointerException e) {
                                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Copy", lovelist.get(position).getImage());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Copy SuccessFully...", Toast.LENGTH_SHORT).show();
                            } catch (NullPointerException e) {
                                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Copy", lovelist.get(position).getImage());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Copy SuccessFully...", Toast.LENGTH_SHORT).show();
                            } catch (NullPointerException e) {
                                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                            }

                        }

                    }
                });
                ItemHolder.share1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                context.startActivity(Intent.createChooser(intent, "Share Via"));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            String uri = lovelist.get(position).getImage();
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("text/plain");
                                            intent.putExtra(Intent.EXTRA_TEXT, uri);
                                            context.startActivity(Intent.createChooser(intent, "Share Via"));
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    String uri = lovelist.get(position).getImage();
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_TEXT, uri);
                                    context.startActivity(Intent.createChooser(intent, "Share Via"));
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                context.startActivity(Intent.createChooser(intent, "Share Via"));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                context.startActivity(Intent.createChooser(intent, "Share Via"));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

                ItemHolder.twitter1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.twitter.android");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            String uri = lovelist.get(position).getImage();
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("text/plain");
                                            intent.setPackage("com.twitter.android");
                                            intent.putExtra(Intent.EXTRA_TEXT, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    String uri = lovelist.get(position).getImage();
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.setPackage("com.twitter.android");
                                    intent.putExtra(Intent.EXTRA_TEXT, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.twitter.android");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.twitter.android");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Twitter have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

                ItemHolder.insta1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            String uri = lovelist.get(position).getImage();
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("text/plain");
                                            intent.setPackage("com.instagram.android");
                                            intent.putExtra(Intent.EXTRA_TEXT, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    String uri = lovelist.get(position).getImage();
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.setPackage("com.instagram.android");
                                    intent.putExtra(Intent.EXTRA_TEXT, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Instagram have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

                ItemHolder.whats1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.whatsapp");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            String uri = lovelist.get(position).getImage();
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("text/plain");
                                            intent.setPackage("com.whatsapp");
                                            intent.putExtra(Intent.EXTRA_TEXT, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    String uri = lovelist.get(position).getImage();
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.setPackage("com.whatsapp");
                                    intent.putExtra(Intent.EXTRA_TEXT, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.whatsapp");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.whatsapp");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(context, "Whatsapp have not been installed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                ItemHolder.face1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAd == null) {
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.facebook.katana");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    intent.setPackage("com.facebook.lite");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else if (mAd.isLoaded()) {
                            if (check()) {
                                Log.w("Ads", "TIMER METHOD");
                                final ProgressDialog mProgressDialog;
                                mProgressDialog = new ProgressDialog(context);
                                mProgressDialog.setMessage("Showing Ads");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        mAd.show();
                                    }
                                }, 1500);
                                mAd.setAdListener(new AdListener() {
                                    @Override
                                    public void onAdClosed() {
                                        super.onAdClosed();
                                        Log.w("Ads", "closedMethod");
                                        loadInstital();
                                        try {
                                            String uri = lovelist.get(position).getImage();
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            intent.setType("text/plain");
                                            intent.setPackage("com.facebook.katana");
                                            intent.putExtra(Intent.EXTRA_TEXT, uri);
                                            try {
                                                context.startActivity(intent);
                                            } catch (ActivityNotFoundException ex) {
                                                intent.setPackage("com.facebook.lite");
                                                try {
                                                    context.startActivity(intent);
                                                } catch (ActivityNotFoundException e) {
                                                    Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Log.w("Ads", "NOT TIMER");
                                try {
                                    String uri = lovelist.get(position).getImage();
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.setPackage("com.facebook.katana");
                                    intent.putExtra(Intent.EXTRA_TEXT, uri);
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ex) {
                                        intent.setPackage("com.facebook.lite");
                                        try {
                                            context.startActivity(intent);
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else if (mAd.isLoading()) {
                            Log.w("Ads", "isLoadingMethod");
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.facebook.katana");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    intent.setPackage("com.facebook.lite");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w("Ads", "notLoadingMethod");
                            loadInstital();
                            try {
                                String uri = lovelist.get(position).getImage();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.facebook.katana");
                                intent.putExtra(Intent.EXTRA_TEXT, uri);
                                try {
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    intent.setPackage("com.facebook.lite");
                                    try {
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(context, "Facebook not Installed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Something Went Wrong: Text is Still Loading or Text not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                break;
            }
        }
    }

    public class AdmobNativeHolder extends RecyclerView.ViewHolder {
        private final AdLoader adLoader;
        private UnifiedNativeAd nativeAd;
        private FrameLayout frameLayout;

        public AdmobNativeHolder(@NonNull View itemView) {
            super(itemView);
            Log.w("Native Ads", "VIEWHOLDER CALLS");
            frameLayout = (FrameLayout) itemView.findViewById(R.id.fl_adplaceholder);
            AdLoader.Builder builder = new AdLoader.Builder(context, context.getResources().getString(R.string.ad_unit_id_native));

            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                // OnUnifiedNativeAdLoadedListener implementation.
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    // You must call destroy on old ads when you are done with them,
                    // otherwise you will have a memory leak.
                    if (nativeAd != null) {
                        nativeAd.destroy();
                    }
                    nativeAd = unifiedNativeAd;
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    UnifiedNativeAdView adView = (UnifiedNativeAdView) inflater
                            .inflate(R.layout.ad_unified, null);
                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });

            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();

            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();

            builder.withNativeAdOptions(adOptions);

            this.adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.w("Native Ads", "AD FAILED" + errorCode);

                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }
            }).build();

        }
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        com.google.android.gms.ads.formats.MediaView mediaView = adView.findViewById(R.id.ad_media);

        mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (child instanceof ImageView) {
                    ImageView imageView = (ImageView) child;
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd();
                }
            });
        } else {

        }
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, whats, face, insta, twitter, share, download, favourite;

        ImageViewHolder(@NonNull final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.Imageviewcustom);
            whats = itemView.findViewById(R.id.whatsapp);
            face = itemView.findViewById(R.id.facebook);
            insta = itemView.findViewById(R.id.instagram);
            twitter = itemView.findViewById(R.id.twitter);
            share = itemView.findViewById(R.id.share);
            download = itemView.findViewById(R.id.download);
            favourite = itemView.findViewById(R.id.favourite);
        }
    }

    private void loadInstital() {
        Log.w("Ads", "loadmethodCalled");
        mAd = new InterstitialAd(context);
        mAd.setAdUnitId(context.getResources().getString(R.string.admob_interstital_ad_unit));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAd.loadAd(adRequest);
    }

    private static class DownloadTask extends AsyncTask<String, Integer, String> {
        File file;
        private PowerManager.WakeLock mWakeLock;
        private WeakReference<WallAdapter> adapterWeakReference;

        DownloadTask(WallAdapter adapter) {
            this.adapterWeakReference = new WeakReference<>(adapter);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = adapterWeakReference.get().context.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis());
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + adapterWeakReference.get().context.getResources().getString(R.string.app_name));
                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    assert imageUri != null;
                    output = resolver.openOutputStream(imageUri);
                } else {
                    String filename = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM).toString() + File.separator + adapterWeakReference.get().context.getResources().getString(R.string.app_name);
                    File dir = new File(filename);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    file = new File(dir, System.currentTimeMillis() + ".jpg");
                    output = new FileOutputStream(file);
                }

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) adapterWeakReference.get().context.getSystemService(Context.POWER_SERVICE);
            assert pm != null;
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
            adapterWeakReference.get().mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            adapterWeakReference.get().mProgressDialog.setIndeterminate(false);
            adapterWeakReference.get().mProgressDialog.setMax(100);
            adapterWeakReference.get().mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            adapterWeakReference.get().mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(adapterWeakReference.get().context, "Download error: Image Not Found", Toast.LENGTH_LONG).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Toast.makeText(adapterWeakReference.get().context, "File downloaded", Toast.LENGTH_SHORT).show();
                } else {
                    if (file != null) {
                        try {
                            Toast.makeText(adapterWeakReference.get().context, "File downloaded", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(file));
                            adapterWeakReference.get().context.sendBroadcast(intent);
                        } catch (Exception e) {
                            Log.e("TAG", "onPostExecute: ", e);
                        }
                    }
                }
            }
        }
    }

    private static class TextHolder extends RecyclerView.ViewHolder {
        TextView wishes;
        ImageView whats1, face1, insta1, twitter1, share1, copy;

        TextHolder(View itemView) {
            super(itemView);
            wishes = itemView.findViewById(R.id.TextWish);
            whats1 = itemView.findViewById(R.id.whatsapp1);
            face1 = itemView.findViewById(R.id.facebook1);
            insta1 = itemView.findViewById(R.id.instagram1);
            twitter1 = itemView.findViewById(R.id.twitter1);
            share1 = itemView.findViewById(R.id.share1);
            copy = itemView.findViewById(R.id.download1);
        }
    }

    public boolean check() {
        PrefManager prf = new PrefManager(context.getApplicationContext());
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());

        if (prf.getString("LAST_DATE_ADS").equals("")) {
            prf.setString("LAST_DATE_ADS", strDate);
        } else {
            String toyBornTime = prf.getString("LAST_DATE_ADS");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                Date oldDate = dateFormat.parse(toyBornTime);
                Log.w("TIME", "check: " + oldDate);
                System.out.println(oldDate);
                Date currentDate = new Date();

                long diff = currentDate.getTime() - oldDate.getTime();
                long seconds = diff / 1000;

                if (seconds > Integer.parseInt(context.getResources().getString(R.string.AD_MOB_TIME))) {
                    prf.setString("LAST_DATE_ADS", strDate);
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
