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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TransactionSummary extends AppCompatActivity {
    private ActivityTransactionSummaryBinding binding;
    private PieChart pieChart;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionSummaryBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        TextInputEditText fromDate = getTextInputEditTextDate(binding.transFromDate);
        TextInputEditText toDate = getTextInputEditTextDate(binding.transToDate);

        Button search = createSearchButton(fromDate, toDate);
        db = new DatabaseHelper(TransactionSummary.this);
        pieChart = binding.pieChart;
        setUpPieChart();
    }

    private Button createSearchButton(TextInputEditText textInputFromDate, TextInputEditText textInputToDate) {
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
            }
        });

        return search;
    }

    private void setUpPieChart() {
        ArrayList<PieEntry> entries = BuildPieEntriesAll();

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.RED);
//        colors.add(Color.parseColor("#FF6384"));
//        colors.add(Color.parseColor("#36A2EB"));
//        colors.add(Color.parseColor("#FFCE56"));
//        colors.add(Color.parseColor("#4BC0C0"));
//        colors.add(Color.parseColor("#9966FF"));
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Spendings");
        pieChart.animateY(1000);

        pieChart.invalidate();
    }

    /**
     * Queries the transactions database for all transactions and builds
     * an ArrayList of pie entries to be used for display purposes.
     * @return ArrayList of pie entries with all transactions
     */
    private ArrayList<PieEntry> BuildPieEntriesAll() {
        Cursor cursor = db.summarizeALlTransactionsByCategory();
        int queryRowCount = cursor.getCount();
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<Double> amount = new ArrayList<>();

        if (queryRowCount == 0) {
            Toast.makeText(TransactionSummary.this, "No transactions found", Toast.LENGTH_SHORT).show();
            cursor.close();
            return null;
        } else {
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(0));
                amount.add(cursor.getDouble(1));
            }
        }

        cursor.close();

        ArrayList<PieEntry> entries = new ArrayList<>();
        for(int i = 0; i < queryRowCount; i++) {
            entries.add(new PieEntry(amount.get(i).floatValue(), categories.get(i)));
        }

        return entries;
    }

    private void getTransactions(String fromDate, String toDate) {

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