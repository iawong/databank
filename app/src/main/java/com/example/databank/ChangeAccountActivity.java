package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityChangeAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

public class ChangeAccountActivity extends AppCompatActivity {
    ActivityChangeAccountBinding binding;
    // TODO: maybe declare and initialize the database outside to avoid repetition
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangeAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputChangeAccountName = binding.textInputChangeAccountName;
        TextInputLayout textInputChangeAccountBalance = binding.textInputChangeAccountBalance;
        TextInputEditText newAccName = binding.changeAccountName;
        TextInputEditText newAccBalance = binding.changeAccountBalance;

        int accountId = getIntent().getIntExtra("accountId", -1);

        if (accountId == -1) {
            Toast.makeText(this, "Error: Invalid account ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        String accountName = getIntent().getStringExtra("accountName");
        double accountBalance = getIntent().getDoubleExtra("accountBalance", 0);

        newAccName.setText(accountName);
        newAccBalance.setText(NumberFormat.getCurrencyInstance(Locale.US).format(accountBalance));

        newAccBalance.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    newAccBalance.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString) / 100;
                        String formatted = NumberFormat.getCurrencyInstance(Locale.US).format(parsed);
                        newAccBalance.setText(formatted);
                        newAccBalance.setSelection(formatted.length()); // Move cursor to the end
                    } else {
                        current = getString(R.string.default_currency);
                        newAccBalance.setText(current);
                        newAccBalance.setSelection(current.length());
                    }
                }

                newAccBalance.addTextChangedListener(this);
            }
        });

        Button saveChanges = binding.changeAccount;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedAccName = newAccName.getText().toString();
                String changedAccBalance = newAccBalance.getText().toString();

                if (changedAccName.isEmpty()) {
                    textInputChangeAccountName.setError("Please enter an account name");
                    return;
                } else {
                    textInputChangeAccountName.setError(null);
                }

                double parsedBalance = 0;

                try {
                    parsedBalance = Double.parseDouble(changedAccBalance.replaceAll("[$,]", ""));
                } catch (NumberFormatException e) {
                    Toast.makeText(ChangeAccountActivity.this, "Invalid balance entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                db = new DatabaseHelper(ChangeAccountActivity.this);
                db.updateAccount(accountId, changedAccName, parsedBalance);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Button deleteAccount = binding.deleteAccount;
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = new DatabaseHelper(ChangeAccountActivity.this);
                db.deleteAccount(accountId);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}