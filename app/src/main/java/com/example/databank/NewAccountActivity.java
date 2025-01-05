package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        TextInputEditText newAccountName = binding.newAccountName;

        Button saveNewAccount = binding.saveNewAccountButton;
        saveNewAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strAccountName = newAccountName.getText().toString().trim();

                if (strAccountName.isEmpty()) {
                    textInputAccountName.setError("Please enter an account name");
                    return;
                } else {
                    textInputAccountName.setError(null);
                }

                DatabaseHelper db = new DatabaseHelper(NewAccountActivity.this);
                db.addAccount(strAccountName);
                // TODO: close database?

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);

                finish();
            }
        });

        Button back = binding.cancelNewAccountButton;
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
