package com.example.databank;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.databank.databinding.ActivityTransactionSummary2Binding;
import com.example.databank.databinding.ActivityTransactionSummaryBinding;

public class TransactionSummary2 extends AppCompatActivity {

    private ActivityTransactionSummary2Binding binding;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionSummary2Binding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);


    }
}