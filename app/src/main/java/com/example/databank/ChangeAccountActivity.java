package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityChangeAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        String accountName = getIntent().getStringExtra("accountName");
        double accountBalance = getIntent().getDoubleExtra("accountBalance", 0);

        newAccName.setText(accountName);
        newAccBalance.setText(String.valueOf(accountBalance));

        // TODO: validate accountID
        int accountId = getIntent().getIntExtra("accountId", -1);

        Button saveChanges = binding.changeAccount;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedAccName = newAccName.getText().toString();
                String changedAccBalance = newAccBalance.getText().toString();

                // TODO: handle errors if double can't be parsed
                db = new DatabaseHelper(ChangeAccountActivity.this);
                db.updateAccount(accountId, changedAccName, Double.parseDouble(changedAccBalance));

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