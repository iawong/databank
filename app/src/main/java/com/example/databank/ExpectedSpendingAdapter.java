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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ExpectedSpendingAdapter extends RecyclerView.Adapter<ExpectedSpendingAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Integer> spendingIds;
    private final ArrayList<String> spendingNames;
    private final ArrayList<Integer> spendingAmounts;
    private final ArrayList<Integer> spendingDays;
    private final OnSpendingDeleteListener deleteListener;

    public interface OnSpendingDeleteListener {
        void onSpendingDelete(int position, int spendingId);
    }

    public ExpectedSpendingAdapter(Context context,
                                   ArrayList<Integer> spendingIds,
                                   ArrayList<String> spendingNames,
                                   ArrayList<Integer> spendingAmounts,
                                   ArrayList<Integer> spendingDays,
                                   OnSpendingDeleteListener deleteListener) {
        this.context = context;
        this.spendingIds = spendingIds;
        this.spendingNames = spendingNames;
        this.spendingAmounts = spendingAmounts;
        this.spendingDays = spendingDays;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.expected_spending_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.spendingName.setText(spendingNames.get(position));
        String spendingDate = "Day " + spendingDays.get(position);
        holder.spendingDate.setText(spendingDate);

        int amountInCents = spendingAmounts.get(position);
        BigDecimal amount = BigDecimal.valueOf(Math.abs(amountInCents)).movePointLeft(2);
        String formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
        holder.spendingAmount.setText(formattedAmount);

        holder.deleteButton.setOnClickListener(v -> deleteListener.onSpendingDelete(holder.getAdapterPosition(), spendingIds.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return spendingNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView spendingName, spendingDate, spendingAmount;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.spendingCardView);
            spendingName = itemView.findViewById(R.id.spendingName);
            spendingDate = itemView.findViewById(R.id.spendingDate);
            spendingAmount = itemView.findViewById(R.id.spendingAmount);
            deleteButton = itemView.findViewById(R.id.removeSpending);
        }
    }
}
