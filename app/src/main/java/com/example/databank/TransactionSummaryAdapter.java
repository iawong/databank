package com.example.databank;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Custom adapter for displaying transaction summaries grouped by category.
 */
public class TransactionSummaryAdapter extends RecyclerView.Adapter<TransactionSummaryAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<String> categories;
    private final ArrayList<Integer> amounts;

    public TransactionSummaryAdapter(Context context, ArrayList<String> categories, ArrayList<Integer> amounts) {
        this.context = context;
        this.categories = categories;
        this.amounts = amounts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.summary_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.summaryCategory.setText(categories.get(position));

        int amountInCents = amounts.get(position);
        int absAmountInCents = Math.abs(amountInCents);

        int totalSpend = 0;
        for (int amt : amounts) {
            totalSpend += Math.abs(amt);
        }

        float percentage = (totalSpend == 0) ? 0f : ((float) absAmountInCents / totalSpend * 100f);

        BigDecimal amount = BigDecimal.valueOf(absAmountInCents).movePointLeft(2);
        String formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
        if (amountInCents < 0) {
            formattedAmount = "-" + formattedAmount;
        }

        String amountAndPercentage = String.format(Locale.US, "%s (%.1f%%)", formattedAmount, percentage);
        holder.summaryAmount.setText(amountAndPercentage);
        holder.summaryAmount.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView summaryCategory, summaryAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.summaryCardView);
            summaryCategory = itemView.findViewById(R.id.summaryCategory);
            summaryAmount = itemView.findViewById(R.id.summaryAmount);
        }
    }
}
