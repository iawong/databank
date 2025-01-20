package com.example.databank;

import android.annotation.SuppressLint;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityTransactionBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity implements OnDeleteListener{
    private TransactionAdapter transactionAdapter;
    ActivityTransactionBinding binding;
    private RecyclerView transactionRecyclerView;
    private DatabaseHelper db;
    private ArrayList<Integer> transactionIds;
    private ArrayList<Double> transactionAmounts;
    private ArrayList<String> transactionDescriptions;
    private ArrayList<String> transactionDates;
    private ArrayList<String> transactionCategories;
    int accountId;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private int currentOffset = 0;

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

//                        long transactionId = data.getLongExtra("transactionId", -1);
//                        double transactionAmount = data.getDoubleExtra("transactionAmount", -1);
//                        String transactionDescription = data.getStringExtra("transactionDescription");
//                        String transactionDate = data.getStringExtra("transactionDate");
//                        String transactionCategory = data.getStringExtra("transactionCategory");

//                        insertItemIntoTransactions((int) transactionId, transactionAmount, transactionDescription, transactionDate, transactionCategory);
                        reloadTransactionData();
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

                        int transactionPosition = data.getIntExtra("transactionPosition", -1);

                        if (transactionPosition == -1)  {
                            Toast.makeText(TransactionActivity.this, "Transaction to change not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double changedTransAmt = data.getDoubleExtra("transactionAmount", -1);
                        String changedTransDesc = data.getStringExtra("transactionDescription");
                        String changedTransDate = data.getStringExtra("transactionDate");
                        String changedTransCategory = data.getStringExtra("transactionCategory");

//                        updateTransaction(transactionPosition, changedTransAmt, changedTransDesc, changedTransDate, changedTransCategory);
                        reloadTransactionData();
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
            Toast.makeText(TransactionActivity.this, "Account id not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        int accountPosition = getIntent().getIntExtra("accountPosition", -1);

        if (accountPosition == -1) {
            Toast.makeText(TransactionActivity.this, "Account position not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        FloatingActionButton addTransaction = binding.addTransactionButton;
        addTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TransactionActivity.this, NewTransactionActivity.class);
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
                returnToAccounts.putExtra("position", accountPosition);
                returnToAccounts.putExtra("accountId", accountId);
                // TODO: add a boolean to track if transactions have changed
                // this can help us avoid reloading the account in the main activity
                setResult(RESULT_OK, returnToAccounts);
                finish();
            }
        });

        db = new DatabaseHelper(TransactionActivity.this);
        transactionIds = new ArrayList<>();
        transactionAmounts = new ArrayList<>();
        transactionDescriptions = new ArrayList<>();
        transactionDates = new ArrayList<>();
        transactionCategories = new ArrayList<>();

        // load the first 10 transactions
        loadTransactions();

        transactionAdapter = new TransactionAdapter(TransactionActivity.this,
                                                    accountId,
                                                    accountPosition,
                                                    transactionIds,
                                                    transactionAmounts,
                                                    transactionDescriptions,
                                                    transactionDates,
                                                    transactionCategories,
                                                    transactionChangeResultLauncher,
                                                    TransactionActivity.this);
        transactionRecyclerView.setAdapter(transactionAdapter);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(TransactionActivity.this));
        transactionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == transactionIds.size() - 1) {
                    // Load the next page of transactions when reaching the bottom
                    loadTransactions();
                }
            }
        });
    }

    /**
     * this will handle pagination for transactions
     * we show the first 10 transactions and then when we scroll
     * to the bottom, we show the next 10 and so on
     */
    private void loadTransactions() {
        isLoading = true;

        new Thread(() -> {
            Cursor cursor = db.getAccountTransactions(accountId, PAGE_SIZE, currentOffset);

            ArrayList<Integer> newTransactionIds = new ArrayList<>();
            ArrayList<Double> newTransactionAmounts = new ArrayList<>();
            ArrayList<String> newTransactionDescriptions = new ArrayList<>();
            ArrayList<String> newTransactionDates = new ArrayList<>();
            ArrayList<String> newTransactionCategories = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    newTransactionIds.add(cursor.getInt(0));
                    newTransactionAmounts.add(cursor.getDouble(2));
                    newTransactionDescriptions.add(cursor.getString(3));
                    newTransactionDates.add(cursor.getString(4));
                    newTransactionCategories.add(cursor.getString(5));
                } while (cursor.moveToNext());
                cursor.close();
            }

            runOnUiThread(() -> {
                transactionIds.addAll(newTransactionIds);
                transactionAmounts.addAll(newTransactionAmounts);
                transactionDescriptions.addAll(newTransactionDescriptions);
                transactionDates.addAll(newTransactionDates);
                transactionCategories.addAll(newTransactionCategories);

                transactionAdapter.notifyItemRangeInserted(transactionIds.size() - newTransactionIds.size(), newTransactionIds.size());
                currentOffset += newTransactionIds.size();

                isLoading = false;

                // If no more data, stop further loading
                if (newTransactionIds.isEmpty()) {
                    transactionRecyclerView.clearOnScrollListeners();
                }
            });
        }).start();
    }


    /**
     * This will reload the transactions that have already been loaded
     * after adding a new transaction or updating a transaction
     * Updating a transaction may not necessarily change the date,
     * but nevertheless, it's easier to just reload all the transactions
     * just in case a date change was made.
     * I am suppressing the NotifyDataSetChanged because a transaction can be pushed
     * outside the 10 most recent transactions to anywhere else.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void reloadTransactionData() {
        // Reset offset and clear existing data
        currentOffset = 0;
        transactionIds.clear();
        transactionAmounts.clear();
        transactionDescriptions.clear();
        transactionDates.clear();
        transactionCategories.clear();

        // Notify the adapter that the dataset has been cleared
        transactionAdapter.notifyDataSetChanged();

        // Load the first batch of transactions
        loadTransactions();
    }

    /**
     * Inserts the new transaction
     * @param transactionId id of the transaction
     * @param transactionAmount amount of the transaction
     * @param transactionDescription description for the transaction
     * @param transactionDate date of the transaction
     * @param transactionCategory category of the transaction
     */
    private void insertItemIntoTransactions(int transactionId, double transactionAmount, String transactionDescription, String transactionDate, String transactionCategory) {
        transactionIds.add(transactionId);
        transactionAmounts.add(transactionAmount);
        transactionDescriptions.add(transactionDescription);
        transactionDates.add(transactionDate);
        transactionCategories.add(transactionCategory);

        // notify the adapter of the new transaction added
        // transactionIds.size() - 1 should be the position/index of the new transaction
        transactionAdapter.notifyItemInserted(transactionIds.size() - 1);
    }

    /**
     * Update the transaction amount, description, and date
     * @param transactionPosition position of the transaction to update
     * @param transactionAmount the changed transaction amount
     * @param transactionDescription the changed transaction description
     * @param transactionDate the changed transaction date
     */
    private void updateTransaction(int transactionPosition, double transactionAmount, String transactionDescription, String transactionDate, String transactionCategory) {
        transactionAmounts.set(transactionPosition, transactionAmount);
        transactionDescriptions.set(transactionPosition, transactionDescription);
        transactionDates.set(transactionPosition, transactionDate);
        transactionCategories.set(transactionPosition, transactionCategory);

        transactionAdapter.notifyItemChanged(transactionPosition);
    }

    /**
     * When the activity is destroyed, close the database
     */
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
                transactionCategories.remove(position);

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