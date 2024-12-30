package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityNewAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NewAccountActivity extends AppCompatActivity {
    ActivityNewAccountBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputAccountName = binding.textInputAccountName;
        TextInputLayout textInputAccountBalance = binding.textInputAccountBalance;
        TextInputEditText newAccountName = binding.newAccountName;
        TextInputEditText newAccountBalance = binding.newAccountBalance;

        Button saveNewAccount = binding.saveAccount;
        saveNewAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strAccountName = newAccountName.getText().toString().trim();

                if (strAccountName.isEmpty()) {
                    textInputAccountName.setError("Please enter an account name");
                } else {
                    textInputAccountName.setError(null);
                }

                String strAccountBalance = newAccountBalance.getText().toString().trim();
                double accountBalance = 0;

                if (!strAccountBalance.isEmpty()) {
                    try {
                        accountBalance = Double.parseDouble(strAccountBalance);
                    } catch (Exception e) {
                        Toast.makeText(NewAccountActivity.this, "Exception: " + e, Toast.LENGTH_SHORT).show();
                    }
                }

                DatabaseHelper db = new DatabaseHelper(NewAccountActivity.this);
                db.addAccount(strAccountName, accountBalance);
                // TODO: close database?

                Intent returnIntent = new Intent();
                // TODO: pass something more specific back to identify account that was added?
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
