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
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityEditTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {
    ActivityEditTransactionBinding binding;
    private boolean isDebit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditTransactionBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        TextInputEditText newTransAmt = binding.editTransactionAmount;

        newTransAmt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        double transAmt = getIntent().getDoubleExtra("transactionAmount", 0);

        String formattedTransAmt = NumberFormat.getCurrencyInstance(Locale.US).format(transAmt);

        // remove the negative when viewing the amount to edit
        if (formattedTransAmt.startsWith("-")) {
            formattedTransAmt = formattedTransAmt.substring(1);
        }

        newTransAmt.setText(formattedTransAmt);

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

        // radio group to save if the transaction is a credit or debit
        RadioGroup transactionTypeGroup = findViewById(R.id.editRadioGroupTransactionType);

        if (newTransAmt.getText() == null) {
            Log.e("EditTransactionActivity", "newTransAmt.getText() is null");
            return;
        }

        if (transAmt < 0) {
            transactionTypeGroup.check(R.id.editRadioDebitButton);
        } else {
            transactionTypeGroup.check(R.id.editRadioCreditButton);
        }

        int selectedId = transactionTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.editRadioDebitButton) {
            isDebit = true;
        } else if (selectedId == R.id.editRadioCreditButton) {
            isDebit = false;
        }

        transactionTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.editRadioDebitButton) {
                isDebit = true;
            } else if (checkedId == R.id.editRadioCreditButton) {
                isDebit = false;
            }
        });

        TextInputEditText newTransDesc = binding.editTransactionDescription;

        newTransDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        // makes sure the transaction description doesn't exceed 255 characters
        newTransDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
        newTransDesc.setText(getIntent().getStringExtra("transactionDescription"));

        TextInputEditText newTransDate = binding.editTransactionDate;

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
                            // Update the TextInputEditText with the selected date in yyyy-MM-dd format
                            String formattedDate = String.format(Locale.US, "%d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay);
                            newTransDate.setText(formattedDate);
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });

        newTransDate.setText(getIntent().getStringExtra("transactionDate"));

        AutoCompleteTextView newTransCategory = binding.editTransactionCategory;
        String[] transactionCategories = getResources().getStringArray(R.array.transaction_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditTransactionActivity.this, R.layout.dropdown_item, transactionCategories);

        newTransCategory.setText(getIntent().getStringExtra("transactionCategory"));
        newTransCategory.setAdapter(adapter);

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

        int accountPosition = getIntent().getIntExtra("accountPosition", -1);

        if (accountPosition == -1) {
            Toast.makeText(EditTransactionActivity.this, "Account position not found", Toast.LENGTH_SHORT).show();
            finish();
        }

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

                double changedAmount;

                try {
                    changedAmount = Double.parseDouble(changedTransAmt.replaceAll("[$,]", ""));

                    if (isDebit) {
                        changedAmount *= -1;
                    }
                } catch (Exception e) {
                    Toast.makeText(EditTransactionActivity.this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                String changedTransCategory = newTransCategory.getText().toString().trim();

                try (DatabaseHelper db = new DatabaseHelper(EditTransactionActivity.this)) {
                    db.updateTransaction(accountId, transactionId, changedAmount, changedTransDesc, changedTransDate, changedTransCategory);
                } catch (Exception e) {
                    Log.e("EditTransactionActivity", "Error editing transaction", e);
                    Toast.makeText(EditTransactionActivity.this, "Failed to edit transaction. Please try again", Toast.LENGTH_SHORT).show();
                }

                Intent returnIntent = new Intent();

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

    /**
     * Hides the keyboard after tapping out of the edit text
     * @param v view that the keyboard should be hidden from
     */
    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(EditTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}