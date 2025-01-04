package com.example.databank;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * custom adapter for a transaction object
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private Context context;
    private int accountId;
    private ArrayList<Integer> transactionIds;
    private ArrayList<Double> transactionAmounts;
    private ArrayList<String> transactionDescriptions;
    private ArrayList<String> transactionDates;
    private ActivityResultLauncher<Intent> transactionChangeResultLauncher;

    public TransactionAdapter (Context context,
                               int accountId,
                               ArrayList<Integer> transactionIds,
                               ArrayList<Double> transactionAmounts,
                               ArrayList<String> transactionDescriptions,
                               ArrayList<String> transactionDates,
                               ActivityResultLauncher<Intent> transactionChangeResultLauncher) {
        this.context = context;
        this.accountId = accountId;
        this.transactionIds = transactionIds;
        this.transactionAmounts = transactionAmounts;
        this.transactionDescriptions = transactionDescriptions;
        this.transactionDates = transactionDates;
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
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        String formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(transactionAmounts.get(position));

        holder.transactionAmount.setText(formattedAmount);

        String description = String.valueOf(transactionDescriptions.get(position));
        String descriptionPreview = "";

        if (description.length() >= 20) {
            descriptionPreview = description.substring(0, 20) + "...";
        } else {
            descriptionPreview = description;
        }

        holder.transactionDescription.setText(descriptionPreview);
        holder.transactionDate.setText(String.valueOf(transactionDates.get(position)));

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeTransactionDetails = new Intent(context, ChangeTransactionActivity.class);
                changeTransactionDetails.putExtra("accountId", accountId);
                changeTransactionDetails.putExtra("transactionId", transactionIds.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("transactionAmount", transactionAmounts.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("transactionDescription", transactionDescriptions.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("transactionDate", transactionDates.get(holder.getAdapterPosition()));
                transactionChangeResultLauncher.launch(changeTransactionDetails);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionAmounts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView transactionAmount, transactionDescription, transactionDate;
        ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            editButton = itemView.findViewById(R.id.editTransaction);
        }
    }
}
