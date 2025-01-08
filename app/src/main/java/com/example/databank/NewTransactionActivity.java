package com.example.databank;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityNewTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTransactionActivity extends AppCompatActivity {
    ActivityNewTransactionBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputTransactionAmount = binding.textInputTransactionAmount;
        TextInputLayout textInputTransactionDescription = binding.textInputTransactionDescription;
        TextInputLayout textInputTransactionDate = binding.textInputTransactionDate;
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

        TextInputEditText newTransDate = getTextInputEditText();

        // makes sure transaction description doesn't exceed 255 characters
        newTransDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});

        Button saveNewTransaction = binding.saveNewTransactionButton;
        saveNewTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strAmount = newTransAmt.getText().toString().trim();
                String strDescription = newTransDesc.getText().toString().trim();
                String strDate = newTransDate.getText().toString().trim();

                if (strAmount.isEmpty()) {
                    textInputTransactionAmount.setError("Please enter a transaction amount");
                    return;
                } else {
                    textInputTransactionAmount.setError(null);
                }

                double amount = 0;

                try {
                    amount = Double.parseDouble(strAmount.replaceAll("[$,]", ""));
                } catch (NumberFormatException e) {
                    Toast.makeText(NewTransactionActivity.this, "Exception: " + e, Toast.LENGTH_SHORT).show();
                    return;
                }

                int accountId = getIntent().getIntExtra("accountId", -1);

                DatabaseHelper db = new DatabaseHelper(NewTransactionActivity.this);
                db.addTransaction(accountId, amount, strDescription, strDate);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);

                finish();
            }
        });

        Button back = binding.cancelNewTransactionButton;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToTransactions = new Intent();
                setResult(RESULT_OK, returnToTransactions);
                finish();
            }
        });
    }

    private @NonNull TextInputEditText getTextInputEditText() {
        TextInputEditText newTransDate = binding.newTransactionDate;

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
                            String formattedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
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

    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(NewTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}