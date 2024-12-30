package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityChangeTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        // TODO: validate transaction ID
        int transactionId = getIntent().getIntExtra("transactionId", -1);
        double transAmt = getIntent().getDoubleExtra("transactionAmount", 0);
        String transDesc = getIntent().getStringExtra("transactionDescription");
        String transDate = getIntent().getStringExtra("transactionDate");

        newTransAmt.setText(String.valueOf(transAmt));
        newTransDesc.setText(transDesc);
        newTransDate.setText(transDate);

        // TODO: validate account ID
        int accountId = getIntent().getIntExtra("accountId", -1);

        Button saveChanges = binding.changeTransaction;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedTransAmt = newTransAmt.getText().toString();
                String changedTransDesc = newTransDesc.getText().toString();
                String changedTransDate = newTransDate.getText().toString();

                // TODO: handle errors if double can't be parsed
                db = new DatabaseHelper(ChangeTransactionActivity.this);
                db.updateTransaction(accountId, transactionId, Double.parseDouble(changedTransAmt), changedTransDesc, changedTransDate);

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