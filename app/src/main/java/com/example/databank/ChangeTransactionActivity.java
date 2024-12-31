package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityChangeTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

public class ChangeTransactionActivity extends AppCompatActivity {
    ActivityChangeTransactionBinding binding;
    // TODO: maybe declare and initialize the database outside to avoid repetition
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangeTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputChangeTransactionAmount = binding.textInputChangeTransactionAmount;
        TextInputLayout textInputChangeTransactionDescription = binding.textInputChangeTransactionDescription;
        TextInputLayout textInputChangeTransactionDate = binding.textInputChangeTransactionDate;
        TextInputEditText newTransAmt = binding.changeTransactionAmount;
        TextInputEditText newTransDesc = binding.changeTransactionDescription;
        TextInputEditText newTransDate = binding.changeTransactionDate;

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

        Button saveChanges = binding.changeTransaction;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedTransAmt = newTransAmt.getText().toString().trim();
                String changedTransDesc = newTransDesc.getText().toString().trim();
                String changedTransDate = newTransDate.getText().toString().trim();

                double parsedDouble = 0;

                try {
                    parsedDouble = Double.parseDouble(changedTransAmt.replaceAll("[$,]", ""));
                } catch (Exception e) {
                    Toast.makeText(ChangeTransactionActivity.this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: handle errors if double can't be parsed
                db = new DatabaseHelper(ChangeTransactionActivity.this);
                db.updateTransaction(accountId, transactionId, parsedDouble, changedTransDesc, changedTransDate);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Button deleteTransaction = binding.deleteTransaction;
        deleteTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = new DatabaseHelper(ChangeTransactionActivity.this);
                db.deleteTransaction(accountId, transactionId, transAmt);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}