package com.example.databank;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityTransactionBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity implements OnDeleteListener{
    TransactionAdapter transactionAdapter;
    ActivityTransactionBinding binding;
    RecyclerView transactionRecyclerView;
    DatabaseHelper db;
    ArrayList<Integer> transactionIds;
    ArrayList<Double> transactionAmounts;
    ArrayList<String> transactionDescriptions;
    ArrayList<String> transactionDates;
    int accountId;

    // get results from new transaction activity
    ActivityResultLauncher<Intent> addTransactionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(TransactionActivity.this, "Transaction data not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        reloadTransactions();
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> transactionChangeResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(TransactionActivity.this, "Transaction data not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        reloadTransactions();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        transactionRecyclerView = binding.transactionList;

        accountId = getIntent().getIntExtra("accountId", -1);

        if (accountId == -1) {
            Toast.makeText(TransactionActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        int position = getIntent().getIntExtra("position", -1);

        if (position == -1) {
            Toast.makeText(TransactionActivity.this, "Account position not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        FloatingActionButton addTransaction = binding.addTransactionButton;
        addTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TransactionActivity.this, NewTransactionActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("accountId", accountId);
                addTransactionLauncher.launch(intent);
            }
        });

        // back button to go back to the main activity aka
        // where all the accounts are
        Button back = binding.backToAccountsButton;
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnToAccounts = new Intent();
                returnToAccounts.putExtra("position", position);
                returnToAccounts.putExtra("accountId", accountId);
                setResult(RESULT_OK, returnToAccounts);
                finish();
            }
        });

        db = new DatabaseHelper(TransactionActivity.this);
        transactionIds = new ArrayList<>();
        transactionAmounts = new ArrayList<>();
        transactionDescriptions = new ArrayList<>();
        transactionDates = new ArrayList<>();

        storeTransactionData();

        transactionAdapter = new TransactionAdapter(TransactionActivity.this,
                                                    accountId,
                                                    position,
                                                    transactionIds,
                                                    transactionAmounts,
                                                    transactionDescriptions,
                                                    transactionDates,
                                                    transactionChangeResultLauncher,
                                                    TransactionActivity.this);
        transactionRecyclerView.setAdapter(transactionAdapter);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(TransactionActivity.this));
    }


    private void storeTransactionData() {
        Cursor cursor = db.getAccountTransactions(accountId);

        if (cursor.getCount() == 0) {
            Toast.makeText(TransactionActivity.this, "No transaction data found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                transactionIds.add(cursor.getInt(0));
                transactionAmounts.add(cursor.getDouble(2));
                transactionDescriptions.add(cursor.getString(3));
                transactionDates.add(cursor.getString(4));
            }
        }

        cursor.close();
    }

    private void reloadTransactions() {
        transactionIds.clear();
        transactionAmounts.clear();
        transactionDescriptions.clear();
        transactionDates.clear();

        storeTransactionData();

        transactionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // close the database connection when the app is closed
        if (db != null) {
            db.close();
        }
    }

    /**
     * Not using this
     * @param position position of account
     * @param accountId account id
     */
    @Override
    public void onAccountDelete(int position, int accountId) {}

    /**
     * delete the transaction
     * @param position transaction position
     * @param accId account id
     * @param transactionId transaction id
     */
    @Override
    public void onTransactionDelete(int position, int accId, int transactionId, double amount) {
        View alertView = getLayoutInflater().inflate(R.layout.alert_dialog, null);

        Button positiveButton = alertView.findViewById(R.id.alertPositiveButton);
        Button negativeButton = alertView.findViewById(R.id.alertNegativeButton);

        AlertDialog dialog = new AlertDialog.Builder(TransactionActivity.this)
                .setView(alertView)
                .setCancelable(false)
                .create();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = new DatabaseHelper(TransactionActivity.this);

                db.deleteTransaction(accId, transactionId, amount);

                transactionIds.remove(position);
                transactionAmounts.remove(position);
                transactionDescriptions.remove(position);
                transactionDates.remove(position);

                transactionAdapter.notifyItemRemoved(position);

                dialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }
    }
}