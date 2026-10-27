package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityNewTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NewTransactionActivity extends AppCompatActivity {
    private InputMethodManager imm;
    private boolean isDebit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityNewTransactionBinding binding = ActivityNewTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imm = (InputMethodManager) getSystemService(NewTransactionActivity.INPUT_METHOD_SERVICE);

        TextInputLayout textInputTransactionAmount = binding.textInputTransactionAmount;
        TextInputEditText newTransAmt = binding.newTransactionAmount;

        newTransAmt.setOnFocusChangeListener((v, hasFocus) -> Helper.hideKeyboard(v, imm));

        TextInputEditText newTransDesc = binding.newTransactionDescription;

        newTransDesc.setOnFocusChangeListener((v, hasFocus) -> Helper.hideKeyboard(v, imm));

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

                    if (!cleanString.isEmpty()) {
                        int amountInCents;

                        try {
                            amountInCents = Integer.parseInt(cleanString);
                        } catch (NumberFormatException e) {
                            // user tried to enter a value greater than an int so
                            // we keep the text as is and return
                            newTransAmt.setText(current);
                            newTransAmt.setSelection(current.length());
                            newTransAmt.addTextChangedListener(this);
                            return;
                        }

                        BigDecimal amount = new BigDecimal(BigInteger.valueOf(amountInCents)).movePointLeft(2);
                        String formatted = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
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

        TextInputEditText newTransDate = Helper.getTextInputEditTextDate(this, binding.newTransactionDate, imm);

        // Transaction category drop down
        AutoCompleteTextView newTransCategory = binding.newTransactionCategory;
        ArrayList<String> transactionCategories;
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            transactionCategories = db.getAllCategoryNames();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(NewTransactionActivity.this, R.layout.dropdown_item, transactionCategories);

        newTransCategory.setAdapter(adapter);

        Button saveNewTransaction = binding.saveNewTransactionButton;
        saveNewTransaction.setOnClickListener(v -> {
            if (newTransAmt.getText() == null) {
                // kick out of saving since user needs to enter a transaction amount
                return;
            }

            String strAmount = newTransAmt.getText().toString().trim();

            int amount;

            try {
                amount = Integer.parseInt(strAmount.replaceAll("[$,.]", ""));

                if (amount == 0) {
                    textInputTransactionAmount.setError("Please enter a transaction amount");
                    return;
                } else {
                    textInputTransactionAmount.setError(null);
                }

                if (isDebit) {
                    amount *= -1;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(NewTransactionActivity.this, "Invalid transaction amount", Toast.LENGTH_SHORT).show();
                return;
            }

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
            String strCategory = newTransCategory.getText().toString().trim();

            int accountId = getIntent().getIntExtra("accountId", -1);

            if (accountId == -1) {
                Toast.makeText(NewTransactionActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
                finish();
            }

            long transactionId;

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

            setResult(RESULT_OK, returnIntent);

            finish();
        });

        Button back = binding.cancelNewTransactionButton;
        back.setOnClickListener(v -> {
            Intent returnToTransactions = new Intent();
            setResult(RESULT_CANCELED, returnToTransactions);
            finish();
        });
    }
}