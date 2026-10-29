package com.example.databank;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * custom adapter for a transaction object
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final Context context;
    private final int accountId;
    private final int accountPosition;
    private final ArrayList<Integer> accountIds;
    private final ArrayList<Integer> transactionIds;
    private final ArrayList<Integer> transactionAmounts;
    private final ArrayList<String> transactionDescriptions;
    private final ArrayList<String> transactionDates;
    private final ArrayList<String> transactionCategories;
    private final ActivityResultLauncher<Intent> transactionChangeResultLauncher;

    public TransactionAdapter (Context context,
                               int accountId,
                               int accountPosition,
                               ArrayList<Integer> transactionIds,
                               ArrayList<Integer> transactionAmounts,
                               ArrayList<String> transactionDescriptions,
                               ArrayList<String> transactionDates,
                               ArrayList<String> transactionCategories,
                               ActivityResultLauncher<Intent> transactionChangeResultLauncher) {
        this.context = context;
        this.accountId = accountId;
        this.accountPosition = accountPosition;
        this.accountIds = null;
        this.transactionIds = transactionIds;
        this.transactionAmounts = transactionAmounts;
        this.transactionDescriptions = transactionDescriptions;
        this.transactionDates = transactionDates;
        this.transactionCategories = transactionCategories;
        this.transactionChangeResultLauncher = transactionChangeResultLauncher;
    }

    public TransactionAdapter (Context context,
                               ArrayList<Integer> accountIds,
                               ArrayList<Integer> transactionIds,
                               ArrayList<Integer> transactionAmounts,
                               ArrayList<String> transactionDescriptions,
                               ArrayList<String> transactionDates,
                               ArrayList<String> transactionCategories,
                               ActivityResultLauncher<Intent> transactionChangeResultLauncher) {
        this.context = context;
        this.accountId = -1;
        this.accountPosition = -1;
        this.accountIds = accountIds;
        this.transactionIds = transactionIds;
        this.transactionAmounts = transactionAmounts;
        this.transactionDescriptions = transactionDescriptions;
        this.transactionDates = transactionDates;
        this.transactionCategories = transactionCategories;
        this.transactionChangeResultLauncher = transactionChangeResultLauncher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.transaction_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int amountInCents = transactionAmounts.get(position);
        BigDecimal amount = BigDecimal.valueOf(Math.abs(amountInCents)).movePointLeft(2);
        String formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);

        // Add a negative sign if the amount is negative and set colors
        if (amountInCents < 0) {
            formattedAmount = "-" + formattedAmount;
            holder.transactionAmount.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.transactionAmount.setTextColor(Color.parseColor("#4CAF50"));
        }

        holder.transactionAmount.setText(formattedAmount);

        String description = String.valueOf(transactionDescriptions.get(position));
        String descriptionPreview;

        if (description.length() >= 20) {
            descriptionPreview = description.substring(0, 20) + "...";
        } else {
            descriptionPreview = description;
        }

        holder.transactionDescription.setText(descriptionPreview);
        holder.transactionDate.setText(String.valueOf(transactionDates.get(position)));
        holder.transactionCategory.setText(String.valueOf(transactionCategories.get(position)));

        holder.cardView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos >= transactionIds.size()) {
                return;
            }

            int currentAccountId = (accountIds != null && pos < accountIds.size()) ? accountIds.get(pos) : accountId;
            Intent changeTransactionDetails = new Intent(context, EditTransactionActivity.class);
            changeTransactionDetails.putExtra("accountId", currentAccountId);
            changeTransactionDetails.putExtra("transactionId", transactionIds.get(pos));
            changeTransactionDetails.putExtra("transactionAmount", transactionAmounts.get(pos));
            changeTransactionDetails.putExtra("transactionDescription", transactionDescriptions.get(pos));
            changeTransactionDetails.putExtra("transactionDate", transactionDates.get(pos));
            changeTransactionDetails.putExtra("transactionCategory", transactionCategories.get(pos));
            changeTransactionDetails.putExtra("accountPosition", accountPosition);
            changeTransactionDetails.putExtra("transactionPosition", pos);
            transactionChangeResultLauncher.launch(changeTransactionDetails);
        });
    }

    @Override
    public int getItemCount() {
        return transactionAmounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView transactionAmount, transactionDescription, transactionDate, transactionCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.transactionCardView);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
        }
    }
}
