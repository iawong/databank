package com.example.databank;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityExpectedSpendingsBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ExpectedSpendingActivity extends AppCompatActivity implements ExpectedSpendingAdapter.OnSpendingDeleteListener {
    private DatabaseHelper db;
    private ArrayList<Integer> spendingIds;
    private ArrayList<String> spendingNames;
    private ArrayList<Integer> spendingAmounts;
    private ArrayList<Integer> spendingDays;
    private ExpectedSpendingAdapter adapter;
    private TextView totalSpendingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityExpectedSpendingsBinding binding = ActivityExpectedSpendingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);
        spendingIds = new ArrayList<>();
        spendingNames = new ArrayList<>();
        spendingAmounts = new ArrayList<>();
        spendingDays = new ArrayList<>();

        totalSpendingText = binding.totalSpendingsText;

        loadSpending();

        RecyclerView recyclerView = binding.spendingsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpectedSpendingAdapter(this, spendingIds, spendingNames, spendingAmounts, spendingDays, this);
        recyclerView.setAdapter(adapter);

        updateTotalSum();

        TextInputLayout nameLayout = binding.textInputSpendingName;
        TextInputLayout amountLayout = binding.textInputSpendingAmount;
        TextInputLayout dateLayout = binding.textInputSpendingDate;

        TextInputEditText nameInput = binding.spendingNameInput;
        TextInputEditText amountInput = binding.spendingAmountInput;
        TextInputEditText dateInput = binding.spendingDateInput;
        Button addButton = binding.addSpendingButton;

        amountInput.setText(getString(R.string.default_currency));

        amountInput.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    amountInput.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    if (!cleanString.isEmpty()) {
                        int amountInCents;

                        try {
                            amountInCents = Integer.parseInt(cleanString);
                        } catch (NumberFormatException e) {
                            amountInput.setText(current);
                            amountInput.setSelection(current.length());
                            amountInput.addTextChangedListener(this);
                            return;
                        }

                        BigDecimal amount = new BigDecimal(BigInteger.valueOf(amountInCents)).movePointLeft(2);
                        String formatted = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
                        current = formatted;
                        amountInput.setText(formatted);
                        amountInput.setSelection(formatted.length());
                    } else {
                        current = getString(R.string.default_currency);
                        amountInput.setText(current);
                        amountInput.setSelection(current.length());
                    }

                    amountInput.addTextChangedListener(this);
                }
            }
        });

        addButton.setOnClickListener(v -> {
            if (nameInput.getText() == null || amountInput.getText() == null || dateInput.getText() == null) return;
            String name = nameInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim().replaceAll("[$,.]", "");
            String dayStr = dateInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameLayout.setError("Please enter a name");
                return;
            }
            nameLayout.setError(null);

            if (amountStr.isEmpty()) {
                amountLayout.setError("Please enter an amount");
                return;
            }
            amountLayout.setError(null);

            if (dayStr.isEmpty()) {
                dateLayout.setError("Please enter a day (1-31)");
                return;
            }
            dateLayout.setError(null);

            int amount;
            int day;
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                day = Integer.parseInt(dayStr);
                if (day < 1 || day > 31) {
                    dateLayout.setError("Day must be between 1 and 31");
                    return;
                }
            } catch (NumberFormatException e) {
                dateLayout.setError("Invalid day number");
                return;
            }
            dateLayout.setError(null);

            long id = db.addExpectedSpending(name, amount, day);
            if (id != -1) {
                loadSpending();
                adapter.notifyDataSetChanged();
                updateTotalSum();

                nameInput.setText("");
                amountInput.setText(getString(R.string.default_currency));
                dateInput.setText("");
                Toast.makeText(this, "Added expected spending", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSpending() {
        Cursor cursor = db.getAllExpectedSpending();
        spendingIds.clear();
        spendingNames.clear();
        spendingAmounts.clear();
        spendingDays.clear();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                spendingIds.add(cursor.getInt(0));
                spendingNames.add(cursor.getString(1));
                spendingAmounts.add(cursor.getInt(2));
                spendingDays.add(cursor.getInt(3));
            }
            cursor.close();
        }
    }

    private void updateTotalSum() {
        int total = 0;

        for (int amt : spendingAmounts) {
            total += amt;
        }

        BigDecimal totalDecimal = BigDecimal.valueOf(total).movePointLeft(2);
        String formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(totalDecimal);
        String displayText = getString(R.string.total_expected_spending) + formattedTotal;
        totalSpendingText.setText(displayText);
    }

    @Override
    public void onSpendingDelete(int position, int spendingId) {
        db.deleteExpectedSpending(spendingId);
        spendingIds.remove(position);
        spendingNames.remove(position);
        spendingAmounts.remove(position);
        spendingDays.remove(position);
        adapter.notifyItemRemoved(position);
        updateTotalSum();
        Toast.makeText(this, "Deleted expected spending", Toast.LENGTH_SHORT).show();
    }
}
