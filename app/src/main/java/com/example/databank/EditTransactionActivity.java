package com.example.databank;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityEditTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {
    ActivityEditTransactionBinding binding;
    // TODO: maybe declare and initialize the database outside to avoid repetition
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditTransactionBinding.inflate(getLayoutInflater());
        // Find the root view
        View rootView = binding.getRoot();
        setContentView(rootView);

        TextInputLayout textInputChangeTransactionAmount = binding.textInputChangeTransactionAmount;
        TextInputLayout textInputChangeTransactionDescription = binding.textInputChangeTransactionDescription;
        TextInputLayout textInputChangeTransactionDate = binding.textInputChangeTransactionDate;
        TextInputEditText newTransAmt = binding.changeTransactionAmount;
        newTransAmt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });
        TextInputEditText newTransDesc = binding.changeTransactionDescription;
        newTransDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });
        TextInputEditText newTransDate = binding.changeTransactionDate;
        newTransDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        newTransDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show the DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EditTransactionActivity.this,
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

        int accountId = getIntent().getIntExtra("accountId", -1);

        if (accountId == -1) {
            Toast.makeText(this, "Error: Invalid account ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        int transactionId = getIntent().getIntExtra("transactionId", -1);

        if (transactionId == -1) {
            Toast.makeText(this, "Error: Invalid transaction ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        int position = getIntent().getIntExtra("position", -1);

        if (position == -1) {
            Toast.makeText(EditTransactionActivity.this, "Account position not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        double transAmt = getIntent().getDoubleExtra("transactionAmount", 0);
        String transDesc = getIntent().getStringExtra("transactionDescription");
        String transDate = getIntent().getStringExtra("transactionDate");

        newTransAmt.setText(NumberFormat.getCurrencyInstance(Locale.US).format(transAmt));
        newTransDesc.setText(transDesc);
        newTransDate.setText(transDate);

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
                        double parsed = Double.parseDouble(cleanString) / 100;
                        String formatted = NumberFormat.getCurrencyInstance(Locale.US).format(parsed);
                        newTransAmt.setText(formatted);
                        newTransAmt.setSelection(formatted.length()); // Move cursor to the end
                    } else {
                        current = getString(R.string.default_currency);
                        newTransAmt.setText(current);
                        newTransAmt.setSelection(current.length());
                    }
                }

                newTransAmt.addTextChangedListener(this);
            }
        });

        // makes sure the transaction description doesn't exceed 255 characters
        newTransDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});

        Button saveChanges = binding.saveEditTransactionButton;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newTransAmt.getText() == null) {
                    // can't be null
                    return;
                }

                String changedTransAmt = newTransAmt.getText().toString().trim();

                if (newTransDesc.getText() == null) {
                    // can't be null but can be empty
                    return;
                }

                String changedTransDesc = newTransDesc.getText().toString().trim();

                if (newTransDate.getText() == null) {
                    // can't be null but can be empty
                    return;
                }

                String changedTransDate = newTransDate.getText().toString().trim();

                double parsedDouble;

                try {
                    parsedDouble = Double.parseDouble(changedTransAmt.replaceAll("[$,]", ""));
                } catch (Exception e) {
                    Toast.makeText(EditTransactionActivity.this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: handle errors if double can't be parsed
                db = new DatabaseHelper(EditTransactionActivity.this);
                db.updateTransaction(accountId, transactionId, parsedDouble, changedTransDesc, changedTransDate);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("position", position);
                returnIntent.putExtra("accountId", accountId);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Button back = binding.cancelEditTransactionButton;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToTransactions = new Intent();
                setResult(RESULT_CANCELED, returnToTransactions);
                finish();
            }
        });
    }

    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(EditTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}