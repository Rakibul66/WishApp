package com.muththamizh.wishes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.muththamizh.wishes.R;
import com.muththamizh.wishes.utils.CategoryItem;

import java.util.ArrayList;
import java.util.List;

import static com.muththamizh.wishes.utils.Constant.CALLED_NAME;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private List<CategoryItem> exampleList;
    private List<CategoryItem> exampleListFull;
    private Context context;

    public CategoryAdapter(List<CategoryItem> exampleList, Context context) {
        this.exampleList = exampleList;
        exampleListFull = new ArrayList<>(exampleList);
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 1: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_category,
                        parent, false);
                viewHolder = new ImgViewHolder(v);
                break;
            }
            case 2: {
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_category,
                        parent, false);
                viewHolder = new TextHolder(v1);
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (exampleList.get(position).getViewType()) {
            case 1: {
                final ImgViewHolder imgViewHolder = (ImgViewHolder) holder;
                Picasso.get()
                        .load(exampleList.get(position).getImage())
                        .placeholder(R.drawable.placeholderimage)
                        .into(imgViewHolder.image, new Callback() {
                            @Override
                            public void onSuccess() {
                                imgViewHolder.image.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                imgViewHolder.image.setImageResource(R.drawable.ic_broken);
                            }
                        });
                imgViewHolder.title.setText(exampleList.get(position).getTitle());


                imgViewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, com.muththamizh.wishes.ui.activity.WallActivity.class);
                        intent.putExtra(CALLED_NAME, exampleList.get(position).getTitle());
                        context.startActivity(intent);
                    }
                });
                break;
            }
            case 2: {
                final TextHolder imgViewHolder = (TextHolder) holder;
                Picasso.get()
                        .load(exampleList.get(position).getImage())
                        .placeholder(R.drawable.placeholderimage)
                        .into(imgViewHolder.image, new Callback() {
                            @Override
                            public void onSuccess() {
                                imgViewHolder.image.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                imgViewHolder.image.setImageResource(R.drawable.ic_broken);
                            }
                        });
                imgViewHolder.title.setText(exampleList.get(position).getTitle());
                imgViewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, com.muththamizh.wishes.ui.activity.TextActivity.class);
                        intent.putExtra(CALLED_NAME, exampleList.get(position).getTitle());
                        context.startActivity(intent);
                    }
                });
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return exampleList.size();
    }

    private static class TextHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        TextHolder(View viewType) {
            super(viewType);
            image = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.text_view1);

        }
    }

    static class ImgViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        ImgViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.text_view1);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (exampleList.get(position) == null) {
            return 1;
        } else {
            return exampleList.get(position).getViewType();
        }
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CategoryItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CategoryItem item : exampleListFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            exampleList.clear();
            exampleList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
