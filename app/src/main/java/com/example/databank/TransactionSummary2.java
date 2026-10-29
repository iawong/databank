package com.example.databank;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityTransactionSummary2Binding;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TransactionSummary2 extends AppCompatActivity {

    private ActivityTransactionSummary2Binding binding;
    private DatabaseHelper db;
    private ArrayList<String> categories;
    private ArrayList<Integer> amounts;
    private TransactionSummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionSummary2Binding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        InputMethodManager imm = (InputMethodManager) getSystemService(NewTransactionActivity.INPUT_METHOD_SERVICE);

        TextInputEditText fromDate = Helper.getTextInputEditTextDate(this, binding.transFromDate, imm);
        TextInputEditText toDate = Helper.getTextInputEditTextDate(this, binding.transToDate, imm);

        db = new DatabaseHelper(this);
        categories = new ArrayList<>();
        amounts = new ArrayList<>();

        setupRecyclerView();
        createSearchButton(fromDate, toDate);

        String fromDateText = fromDate.getText() == null ? "" : fromDate.getText().toString().trim();
        String toDateText = toDate.getText() == null ? "" : toDate.getText().toString().trim();

        loadSummary(fromDateText, toDateText);
    }

    private final ActivityResultLauncher<Intent> categoryTransactionsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            activityResult -> {
                if (activityResult.getResultCode() == RESULT_OK) {
                    String fromDateText = binding.transFromDate.getText() == null ? "" : binding.transFromDate.getText().toString().trim();
                    String toDateText = binding.transToDate.getText() == null ? "" : binding.transToDate.getText().toString().trim();
                    loadSummary(fromDateText, toDateText);
                }
            }
    );

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.transSumList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionSummaryAdapter(this, categories, amounts, category -> {
            String fromDateText = binding.transFromDate.getText() == null ? "" : binding.transFromDate.getText().toString().trim();
            String toDateText = binding.transToDate.getText() == null ? "" : binding.transToDate.getText().toString().trim();
            Intent intent = new Intent(TransactionSummary2.this, TransactionsByCategoryActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("fromDate", fromDateText);
            intent.putExtra("toDate", toDateText);
            categoryTransactionsLauncher.launch(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void createSearchButton(TextInputEditText textInputFromDate, TextInputEditText textInputToDate) {
        Button search = binding.saveDateRange;
        search.setOnClickListener(v -> {
            String fromDate = "";
            String toDate = "";

            if (textInputFromDate.getText() != null) {
                fromDate = textInputFromDate.getText().toString().trim();
            }

            if (textInputToDate.getText() != null) {
                toDate = textInputToDate.getText().toString().trim();
            }

            loadSummary(fromDate, toDate);
        });
    }

    private void loadSummary(String fromDate, String toDate) {
        Cursor cursor;
        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            cursor = db.summarizeTransactionsByCategoryAndDate(fromDate, toDate);
        } else {
            cursor = db.summarizeAllTransactionsByCategory();
        }

        categories.clear();
        amounts.clear();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(0));
                amounts.add(cursor.getInt(1));
            }

            cursor.close();
        }

        removeExcludedCategories();
        sortCategoriesByAmount();
        updateTotalSum();

        if (categories.isEmpty()) {
            Toast.makeText(this, "No transactions found for the selected date range", Toast.LENGTH_SHORT).show();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private boolean isExcludedCategory(String category) {
        String[] excludedCategories = {"Account Transfer", "Cash Deposit", "Cash Withdraw", "Credit Card Payment", "Paycheck", ""};
        return Arrays.asList(excludedCategories).contains(category);
    }

    private void removeExcludedCategories() {
        for (int i = categories.size() - 1; i >= 0; i--) {
            if (isExcludedCategory(categories.get(i))) {
                categories.remove(i);
                amounts.remove(i);
            }
        }
    }

    private void sortCategoriesByAmount() {
        List<Pair<String, Integer>> list = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            list.add(new Pair<>(categories.get(i), amounts.get(i)));
        }

        list.sort((a, b) -> Integer.compare(Math.abs(b.second), Math.abs(a.second)));

        categories.clear();
        amounts.clear();

        for (Pair<String, Integer> p : list) {
            categories.add(p.first);
            amounts.add(p.second);
        }
    }

    private void updateTotalSum() {
        int total = 0;
        for (int amount : amounts) {
            total += Math.abs(amount);
        }
        BigDecimal totalDecimal = BigDecimal.valueOf(total).movePointLeft(2);
        String formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(totalDecimal);
        String displayText = getString(R.string.total_summary) + " " + formattedTotal;
        binding.totalSummaryText.setText(displayText);
    }
}