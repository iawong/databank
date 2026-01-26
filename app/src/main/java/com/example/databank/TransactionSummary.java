package com.example.databank;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.databank.databinding.ActivityTransactionSummaryBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * This activity is for showing transaction summaries. The first visual is a pie chart
 * and the second is a plain text list of totals across different categories.
 */
public class TransactionSummary extends AppCompatActivity {
    private ActivityTransactionSummaryBinding binding;
    private PieChart pieChart;
    private DatabaseHelper db;
    private ArrayList<String> categories;
    private ArrayList<Double> amounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionSummaryBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        TextInputEditText fromDate = getTextInputEditTextDate(binding.transFromDate);
        TextInputEditText toDate = getTextInputEditTextDate(binding.transToDate);

        db = new DatabaseHelper(TransactionSummary.this);
        createSearchButton(fromDate, toDate);
        pieChart = binding.pieChart;
        setUpInitialPieChart();
    }

    private void createSearchButton(TextInputEditText textInputFromDate, TextInputEditText textInputToDate) {
        Button search = binding.saveDateRange;
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromDate = "";
                String toDate = "";

                if (textInputFromDate.getText() != null) {
                    fromDate = textInputFromDate.getText().toString().trim();
                }

                if (textInputToDate.getText() != null) {
                    toDate = textInputToDate.getText().toString().trim();
                }

                getTransactions(fromDate, toDate);
                updatePieChart();
            }
        });
    }

    private void setUpInitialPieChart() {
        ArrayList<PieEntry> entries = buildPieEntriesAll();
        if (entries == null) entries = new ArrayList<>();

        setPieLegend();

        PieData pieData = getPieData(entries);

        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.setData(pieData);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                if (e == null) return;

                PieEntry pe = (PieEntry) e;
                pieChart.setCenterText(pe.getLabel());
            }

            @Override
            public void onNothingSelected() {
                // Reset the center text when clicking away
                pieChart.setCenterText("Expenses");
            }
        });
    }

    private void setPieLegend() {
        Legend l = pieChart.getLegend();
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.WHITE);
        l.setTextSize(20);
    }

    @NonNull
    private static PieData getPieData(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#2ECC71"));
        colors.add(Color.parseColor("#3498DB"));
        colors.add(Color.parseColor("#9B59B6"));
        colors.add(Color.parseColor("#E74C3C"));
        colors.add(Color.parseColor("#F1C40F"));
        colors.add(Color.parseColor("#1ABC9C"));
        colors.add(Color.parseColor("#FA8072"));
        colors.add(Color.parseColor("#5D6D7E"));
        colors.add(Color.parseColor("#E67E22"));
        colors.add(Color.parseColor("#E91E63"));
        colors.add(Color.parseColor("#A6E22E"));
        colors.add(Color.parseColor("#00BCD4"));
        colors.add(Color.parseColor("#FFC107"));
        colors.add(Color.parseColor("#6610F2"));
        colors.add(Color.parseColor("#BDC3C7"));
        dataSet.setColors(colors);
        // hide the slice values
        dataSet.setDrawValues(false);
        return new PieData(dataSet);
    }

    // Helper method to build entries with custom labels
    private ArrayList<PieEntry> getFormattedEntries() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        double total = 0;
        for (Double amount : amounts) {
            total += Math.abs(amount);
        }

        for (int i = 0; i < categories.size(); i++) {
            double val = Math.abs(amounts.get(i));
            float percentage = (total == 0) ? 0f : (float) (val / total * 100f);

            // Create the string: Automotive: $1,000.00 - 56.0%
            String customLabel = String.format(Locale.US, "%s: %s - %.1f%%",
                    categories.get(i),
                    formatDoubleAsCurrency(val),
                    percentage);

            entries.add(new PieEntry((float) val, customLabel));
        }
        return entries;
    }

    /**
     * Queries the transactions database for all transactions and builds
     * an ArrayList of pie entries to be used for display purposes.
     * @return ArrayList of pie entries with all transactions
     */
    private ArrayList<PieEntry> buildPieEntriesAll() {
        Cursor cursor = db.summarizeAllTransactionsByCategory();
        int queryRowCount = cursor.getCount();

        categories = new ArrayList<>();
        amounts = new ArrayList<>();

        if (queryRowCount == 0) {
            Toast.makeText(TransactionSummary.this, "No transactions found", Toast.LENGTH_SHORT).show();
            cursor.close();
            return null;
        } else {
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(0));
                amounts.add(cursor.getDouble(1));
            }
        }

        cursor.close();

        removeExcludedCategories();

        return getFormattedEntries();
    }

    private boolean isExcludedCategory(String category) {
        String[] excludedCategories = {"Account Transfer", "Cash Deposit", "Cash Withdraw", "Credit Card Payment", "Paycheck", ""};
        return Arrays.asList(excludedCategories).contains(category);
    }

    private String formatDoubleAsCurrency(double amount) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return numberFormat.format(amount);
    }

    private void getTransactions(String fromDate, String toDate) {
        Cursor cursor = db.summarizeTransactionsByCategoryAndDate(fromDate, toDate);
        int queryRowCount = cursor.getCount();

        if (queryRowCount == 0) {
            Toast.makeText(TransactionSummary.this, "No transactions found", Toast.LENGTH_SHORT).show();
            cursor.close();
            categories = new ArrayList<>();
            amounts = new ArrayList<>();
            return;
        }

        categories = new ArrayList<>();
        amounts = new ArrayList<>();

        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0));
            amounts.add(cursor.getDouble(1));
        }

        cursor.close();
        removeExcludedCategories();
    }

    /**
     * Remove any categories and the corresponding amount that should be excluded.
     */
    private void removeExcludedCategories() {
        for (int i = categories.size() - 1; i >= 0; i--) {
            if (isExcludedCategory(categories.get(i))) {
                categories.remove(i);
                amounts.remove(i);
            }
        }
    }

    private void updatePieChart() {
        if (categories.isEmpty()) {
            pieChart.clear();
            return;
        }

        ArrayList<PieEntry> entries = getFormattedEntries();

        PieData pieData = getPieData(entries);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    /**
     * Create formatted date
     * @return the TextInputEditText with the new date
     */
    private @NonNull TextInputEditText getTextInputEditTextDate(TextInputEditText text) {
        Calendar calendar = Calendar.getInstance();
        String runDate = String.format(Locale.US, "%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        text.setText(runDate);

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show the DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        TransactionSummary.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Update the TextInputEditText with the selected date in MM/DD/YYYY format
                            String formattedDate = String.format(Locale.US, "%d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay);
                            text.setText(formattedDate);
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });

        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        return text;
    }

    /**
     * Hides the keyboard after tapping out of the edit text
     * @param v view that the keyboard should be hidden from
     */
    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(NewTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
