package com.example.databank;

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

public class TransactionActivity extends AppCompatActivity {
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

                        assert data != null;

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

                        assert data != null;

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

        // TODO: wth is this?? double check if try / catch is appropriate
        if (accountId == -1) {
            try {
                throw new Exception("Account not found: returned -1");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
        Button back = binding.backButton;
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnToAccounts = new Intent();
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

        transactionAdapter = new TransactionAdapter(TransactionActivity.this, accountId, transactionIds, transactionAmounts, transactionDescriptions, transactionDates, transactionChangeResultLauncher);
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
}