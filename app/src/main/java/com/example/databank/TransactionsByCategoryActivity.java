package com.example.databank;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toolbar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityTransactionsByCategoryBinding;

import java.util.ArrayList;

public class TransactionsByCategoryActivity extends AppCompatActivity {
    private ActivityTransactionsByCategoryBinding binding;
    private DatabaseHelper db;
    private ArrayList<Integer> transactionIds;
    private ArrayList<Integer> accountIds;
    private ArrayList<Integer> transactionAmounts;
    private ArrayList<String> transactionDescriptions;
    private ArrayList<String> transactionDates;
    private ArrayList<String> transactionCategories;
    private TransactionAdapter transactionAdapter;
    private String category;
    private String fromDate;
    private String toDate;

    private final ActivityResultLauncher<Intent> transactionChangeResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            activityResult -> {
                if (activityResult.getResultCode() == RESULT_OK) {
                    loadTransactions();
                    setResult(RESULT_OK);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionsByCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        category = getIntent().getStringExtra("category");
        fromDate = getIntent().getStringExtra("fromDate");
        toDate = getIntent().getStringExtra("toDate");

        Toolbar toolbar = binding.transactionsByCategoryAppbar;
        toolbar.setTitle(category != null ? category : "Transactions");

        db = new DatabaseHelper(this);
        transactionIds = new ArrayList<>();
        accountIds = new ArrayList<>();
        transactionAmounts = new ArrayList<>();
        transactionDescriptions = new ArrayList<>();
        transactionDates = new ArrayList<>();
        transactionCategories = new ArrayList<>();

        transactionAdapter = new TransactionAdapter(this,
                accountIds,
                transactionIds,
                transactionAmounts,
                transactionDescriptions,
                transactionDates,
                transactionCategories,
                transactionChangeResultLauncher);

        RecyclerView recyclerView = binding.transactionsByCategoryList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(transactionAdapter);

        loadTransactions();

        Button backButton = binding.backButton;
        backButton.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_OK);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void loadTransactions() {
        transactionIds.clear();
        accountIds.clear();
        transactionAmounts.clear();
        transactionDescriptions.clear();
        transactionDates.clear();
        transactionCategories.clear();

        Cursor cursor = db.getTransactionsByCategoryAndDate(category, fromDate, toDate);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                transactionIds.add(cursor.getInt(0));
                accountIds.add(cursor.getInt(1));
                transactionAmounts.add(cursor.getInt(2));
                transactionDescriptions.add(cursor.getString(3));
                transactionDates.add(cursor.getString(4));
                transactionCategories.add(cursor.getString(5));
            }
            cursor.close();
        }

        transactionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
