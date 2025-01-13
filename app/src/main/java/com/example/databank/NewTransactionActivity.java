package com.example.databank;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityNewTransactionBinding;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewTransactionActivity extends AppCompatActivity {
    ActivityNewTransactionBinding binding;
    boolean isDebit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputTransactionAmount = binding.textInputTransactionAmount;
        TextInputEditText newTransAmt = binding.newTransactionAmount;

        newTransAmt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        TextInputEditText newTransDesc = binding.newTransactionDescription;

        newTransDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        newTransAmt.setText(getString(R.string.default_currency));

        newTransAmt.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    newTransAmt.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    // Parse as cents (integer)
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString) / 100;
                        String formatted = NumberFormat.getCurrencyInstance(Locale.US).format(parsed);
                        current = formatted;
                        newTransAmt.setText(formatted);
                        newTransAmt.setSelection(formatted.length()); // Move cursor to end
                    } else {
                        current = getString(R.string.default_currency);
                        newTransAmt.setText(current);
                        newTransAmt.setSelection(current.length());
                    }

                    newTransAmt.addTextChangedListener(this);
                }
            }
        });

        // radio group to save if the transaction is a credit or debit
        RadioGroup transactionTypeGroup = findViewById(R.id.radioGroupTransactionType);

        int selectedId = transactionTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioDebitButton) {
            isDebit = true;
        } else if (selectedId == R.id.radioCreditButton) {
            isDebit = false;
        }

        transactionTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDebitButton) {
                isDebit = true;
            } else if (checkedId == R.id.radioCreditButton) {
                isDebit = false;
            }
        });

        // makes sure transaction description doesn't exceed 255 characters
        newTransDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});

        TextInputEditText newTransDate = getTextInputEditText();

        // Transaction category drop down
        AutoCompleteTextView newTransCategory = binding.newTransactionCategory;
        String[] transactionCategories = getResources().getStringArray(R.array.transaction_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(NewTransactionActivity.this, R.layout.dropdown_item, transactionCategories);

        newTransCategory.setAdapter(adapter);

        Button saveNewTransaction = binding.saveNewTransactionButton;
        saveNewTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newTransAmt.getText() == null) {
                    // kick out of saving since user needs to enter a transaction amount
                    return;
                }

                String strAmount = newTransAmt.getText().toString().trim();

                if (newTransDesc.getText() == null) {
                    // kick out of saving because this should not be null even
                    // if user has not entered a description
                    return;
                }

                String strDescription = newTransDesc.getText().toString().trim();

                if (newTransDate.getText() == null) {
                    // kick out of saving because this should not be null even
                    // if user has not entered a date
                    return;
                }

                String strDate = newTransDate.getText().toString().trim();

                if (strAmount.isEmpty()) {
                    textInputTransactionAmount.setError("Please enter a transaction amount");
                    return;
                } else {
                    textInputTransactionAmount.setError(null);
                }

                double amount;

                try {
                    amount = Double.parseDouble(strAmount.replaceAll("[$,]", ""));

                    if (isDebit) {
                        amount *= -1;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(NewTransactionActivity.this, "Invalid transaction amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                String strCategory = newTransCategory.getText().toString().trim();

                int accountId = getIntent().getIntExtra("accountId", -1);

                if (accountId == -1) {
                    Toast.makeText(NewTransactionActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
                    finish();
                }

                long transactionId = -1;

                try (DatabaseHelper db = new DatabaseHelper(NewTransactionActivity.this)) {
                    transactionId = db.addTransaction(accountId, amount, strDescription, strDate, strCategory);

                    if (transactionId == -1) {
                        // failed to add transaction, try again
                        return;
                    }
                } catch (Exception e) {
                    Log.e("NewTransactionActivity", "Error adding transaction", e);
                    Toast.makeText(NewTransactionActivity.this, "Failed to add transaction. Please try again.", Toast.LENGTH_SHORT).show();
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("transactionId", transactionId);
                returnIntent.putExtra("transactionAmount", amount);
                returnIntent.putExtra("transactionDescription", strDescription);
                returnIntent.putExtra("transactionDate", strDate);
                returnIntent.putExtra("transactionCategory", strCategory);

                setResult(RESULT_OK, returnIntent);

                finish();
            }
        });

        Button back = binding.cancelNewTransactionButton;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToTransactions = new Intent();
                setResult(RESULT_CANCELED, returnToTransactions);
                finish();
            }
        });
    }

    /**
     * Create formatted transaction date
     * @return the TextInputEditText with the new transaction date
     */
    private @NonNull TextInputEditText getTextInputEditText() {
        TextInputEditText newTransDate = binding.newTransactionDate;

        Calendar calendar = Calendar.getInstance();
        String runDate = String.format(Locale.US, "%02d/%02d/%d",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.YEAR));

        newTransDate.setText(runDate);

        newTransDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show the DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        NewTransactionActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Update the TextInputEditText with the selected date in MM/DD/YYYY format
                            String formattedDate = String.format(Locale.US, "%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            newTransDate.setText(formattedDate);
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });

        newTransDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        return newTransDate;
    }

    /**
     * Hides the keyboard after tapping out of the edit text
     * @param v view that the keyboard should be hidden from
     */
    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(NewTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}