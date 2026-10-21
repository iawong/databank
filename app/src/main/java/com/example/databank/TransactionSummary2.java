package com.example.databank;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.databank.databinding.ActivityTransactionSummary2Binding;
import com.example.databank.databinding.ActivityTransactionSummaryBinding;
import com.google.android.material.textfield.TextInputEditText;

public class TransactionSummary2 extends AppCompatActivity {

    private ActivityTransactionSummary2Binding binding;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionSummary2Binding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        InputMethodManager imm = (InputMethodManager) getSystemService(NewTransactionActivity.INPUT_METHOD_SERVICE);

        TextInputEditText fromDate = Helper.getTextInputEditTextDate(this, binding.transFromDate, imm);
        TextInputEditText toDate = Helper.getTextInputEditTextDate(this, binding.transToDate, imm);
    }
}