package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityEditAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditAccountActivity extends AppCompatActivity {
    ActivityEditAccountBinding binding;
    // TODO: maybe declare and initialize the database outside to avoid repetition
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditAccountBinding.inflate(getLayoutInflater());
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

        // make sure account name does not exceed 20 characters otherwise it overflows
        newAccName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        Button saveChanges = binding.saveEditAccountButton;
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

                db = new DatabaseHelper(EditAccountActivity.this);
                db.updateAccount(accountId, changedAccName);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Button back = binding.cancelEditAccountButton;
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