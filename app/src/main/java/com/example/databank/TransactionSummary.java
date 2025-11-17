package com.example.databank;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.databank.databinding.ActivityTransactionSummaryBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class TransactionSummary extends AppCompatActivity {
    private ActivityTransactionSummaryBinding binding;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionSummaryBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        pieChart = findViewById(R.id.pieChart);
        setUpPieChart();
    }

    private void setUpPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30f, "Food"));
        entries.add(new PieEntry(25f, "Transport"));
        entries.add(new PieEntry(20f, "Entertainment"));
        entries.add(new PieEntry(15f, "Shopping"));
        entries.add(new PieEntry(10f, "Bills"));

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF6384"));
        colors.add(Color.parseColor("#36A2EB"));
        colors.add(Color.parseColor("#FFCE56"));
        colors.add(Color.parseColor("#4BC0C0"));
        colors.add(Color.parseColor("#9966FF"));
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Spendings");
        pieChart.animate();

//        pieChart.invalidate();
    }
}