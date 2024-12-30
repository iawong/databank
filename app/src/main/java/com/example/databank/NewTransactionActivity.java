package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityNewTransactionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NewTransactionActivity extends AppCompatActivity {
    ActivityNewTransactionBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputTransactionAmount = binding.textInputTransactionAmount;
        TextInputLayout textInputTransactionDescription = binding.textInputTransactionDescription;
        TextInputLayout textInputTransactionDate = binding.textInputTransactionDate;
        TextInputEditText newTransAmt = binding.newTransactionAmount;
        TextInputEditText newTransDesc = binding.newTransactionDescription;
        TextInputEditText newTransDate = binding.newTransactionDate;

        Button saveNewTransaction = binding.saveTransaction;
        saveNewTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strAmount = newTransAmt.getText().toString();
                String strDescription = newTransDesc.getText().toString();
                String strDate = newTransDate.getText().toString();

                if (strAmount.isEmpty()) {
                    textInputTransactionAmount.setError("Please enter a transaction amount");
                } else {
                    textInputTransactionAmount.setError(null);
                }

                double amount = 0;

                // TODO: that sout ain't gonna work
                try {
                    amount = Double.parseDouble(strAmount);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format: " + strAmount);
                }

                int accountId = getIntent().getIntExtra("accountId", -1);

                DatabaseHelper db = new DatabaseHelper(NewTransactionActivity.this);
                db.addTransaction(accountId, amount, strDescription, strDate);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);

                finish();
            }
        });

        Button back = binding.backButton;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToTransactions = new Intent();
                setResult(RESULT_OK, returnToTransactions);
                finish();
            }
        });
    }

    // TODO: use this method to validate the transaction values the user enters
    private void validateTransactionValues() {

//
//            Intent returnIntent = new Intent();
//
//            returnIntent.putExtra("transAmount", amount);
//            returnIntent.putExtra("transDescription", strDescription);
//            setResult(RESULT_OK, returnIntent);
//        }
    }
}