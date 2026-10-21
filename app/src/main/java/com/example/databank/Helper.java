package com.example.databank;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.DatePickerDialog;
import android.content.Context;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class Helper {
    static void hideKeyboard(View view, InputMethodManager imm) {
        if (view == null || imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Create formatted date
     * @return the TextInputEditText with the new date
     */
    static TextInputEditText getTextInputEditTextDate(Context context, TextInputEditText text, InputMethodManager imm) {
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
                        context,
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

        text.setOnFocusChangeListener((v, hasFocus) -> hideKeyboard(v, imm));

        return text;
    }
}
