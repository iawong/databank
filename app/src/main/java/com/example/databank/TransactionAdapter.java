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
    private int accountPosition;
    private ArrayList<Integer> transactionIds;
    private ArrayList<Double> transactionAmounts;
    private ArrayList<String> transactionDescriptions;
    private ArrayList<String> transactionDates;
    private ArrayList<String> transactionCategories;
    private ActivityResultLauncher<Intent> transactionChangeResultLauncher;
    private OnDeleteListener deleteListener;

    public TransactionAdapter (Context context,
                               int accountId,
                               int accountPosition,
                               ArrayList<Integer> transactionIds,
                               ArrayList<Double> transactionAmounts,
                               ArrayList<String> transactionDescriptions,
                               ArrayList<String> transactionDates,
                               ArrayList<String> transactionCategories,
                               ActivityResultLauncher<Intent> transactionChangeResultLauncher,
                               OnDeleteListener deleteListener) {
        this.context = context;
        this.accountId = accountId;
        this.accountPosition = accountPosition;
        this.transactionIds = transactionIds;
        this.transactionAmounts = transactionAmounts;
        this.transactionDescriptions = transactionDescriptions;
        this.transactionDates = transactionDates;
        this.transactionCategories = transactionCategories;
        this.transactionChangeResultLauncher = transactionChangeResultLauncher;
        this.deleteListener = deleteListener;
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
        holder.transactionCategory.setText(String.valueOf(transactionCategories.get(position)));

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeTransactionDetails = new Intent(context, EditTransactionActivity.class);
                changeTransactionDetails.putExtra("accountId", accountId);
                changeTransactionDetails.putExtra("transactionId", transactionIds.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("transactionAmount", transactionAmounts.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("transactionDescription", transactionDescriptions.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("transactionDate", transactionDates.get(holder.getAdapterPosition()));
                changeTransactionDetails.putExtra("accountPosition", accountPosition);
                changeTransactionDetails.putExtra("transactionPosition", holder.getAdapterPosition());
                transactionChangeResultLauncher.launch(changeTransactionDetails);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteListener.onTransactionDelete(holder.getAdapterPosition(),
                                                   accountId,
                                                   transactionIds.get(holder.getAdapterPosition()),
                                                   transactionAmounts.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionAmounts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView transactionAmount, transactionDescription, transactionDate, transactionCategory;
        ImageButton editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
            editButton = itemView.findViewById(R.id.editTransaction);
            deleteButton = itemView.findViewById(R.id.removeTransaction);
        }
    }
}
