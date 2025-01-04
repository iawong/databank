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
        TextInputEditText newAccName = binding.changeAccountName;

        int accountId = getIntent().getIntExtra("accountId", -1);

        if (accountId == -1) {
            Toast.makeText(this, "Error: Invalid account ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        String accountName = getIntent().getStringExtra("accountName");

        newAccName.setText(accountName);

        Button saveChanges = binding.changeAccount;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedAccName = newAccName.getText().toString();

                if (changedAccName.isEmpty()) {
                    textInputChangeAccountName.setError("Please enter an account name");
                    return;
                } else {
                    textInputChangeAccountName.setError(null);
                }

                db = new DatabaseHelper(ChangeAccountActivity.this);
                db.updateAccount(accountId, changedAccName);

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

        Button back = binding.backButton;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToAccounts = new Intent();
                setResult(RESULT_OK, returnToAccounts);
                finish();
            }
        });
    }
}