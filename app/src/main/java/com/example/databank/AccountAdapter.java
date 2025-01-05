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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * custom adapter for an account object
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Integer> accountIds;
    private ArrayList<String> accountNames;
    private ArrayList<Double> accountBalances;
    private ActivityResultLauncher<Intent> transactionResultLauncher;
    private ActivityResultLauncher<Intent> accountChangeResultLauncher;
    private OnDeleteListener deleteListener;

    public AccountAdapter (Context context,
                           ArrayList<Integer> accountIds,
                           ArrayList<String> accountNames,
                           ArrayList<Double> accountBalances,
                           ActivityResultLauncher<Intent> transactionResultLauncher,
                           ActivityResultLauncher<Intent> accountChangeResultLauncher,
                           OnDeleteListener deleteListener) {
        this.context = context;
        this.accountIds = accountIds;
        this.accountNames = accountNames;
        this.accountBalances = accountBalances;
        this.transactionResultLauncher = transactionResultLauncher;
        this.accountChangeResultLauncher = accountChangeResultLauncher;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.account_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountAdapter.ViewHolder holder, int position) {
        holder.accountName.setText(String.valueOf(accountNames.get(position)));

        String formattedBalance = NumberFormat.getCurrencyInstance(Locale.US).format(accountBalances.get(position));
        holder.accountBalance.setText(formattedBalance);

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeAccountDetails = new Intent(context, ChangeAccountActivity.class);
                changeAccountDetails.putExtra("accountId", accountIds.get(holder.getAdapterPosition()));
                changeAccountDetails.putExtra("accountName", accountNames.get(holder.getAdapterPosition()));
                accountChangeResultLauncher.launch(changeAccountDetails);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteListener.onAccountDelete(holder.getAdapterPosition(), accountIds.get(holder.getAdapterPosition()));
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewTransactions = new Intent(context, TransactionActivity.class);
                viewTransactions.putExtra("accountId", accountIds.get(holder.getAdapterPosition()));
                transactionResultLauncher.launch(viewTransactions);
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView accountName, accountBalance;
        ImageButton editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.accountCardView);
            accountName = itemView.findViewById(R.id.accountName);
            accountBalance = itemView.findViewById(R.id.accountBalance);
            editButton = itemView.findViewById(R.id.editAccount);
            deleteButton = itemView.findViewById(R.id.removeAccount);
        }
    }
}