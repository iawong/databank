package com.example.databank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Integer> categoryIds;
    private final ArrayList<String> categoryNames;
    private final OnCategoryDeleteListener deleteListener;

    public interface OnCategoryDeleteListener {
        void onCategoryDelete(int position, int categoryId);
    }

    public CategoryAdapter(Context context, ArrayList<Integer> categoryIds, ArrayList<String> categoryNames, OnCategoryDeleteListener deleteListener) {
        this.context = context;
        this.categoryIds = categoryIds;
        this.categoryNames = categoryNames;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryName.setText(categoryNames.get(position));

        holder.deleteButton.setOnClickListener(v -> {
            deleteListener.onCategoryDelete(holder.getAdapterPosition(), categoryIds.get(holder.getAdapterPosition()));
        });
    }

    @Override
    public int getItemCount() {
        return categoryNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView categoryName;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.categoryCardView);
            categoryName = itemView.findViewById(R.id.categoryName);
            deleteButton = itemView.findViewById(R.id.removeCategory);
        }
    }
}
