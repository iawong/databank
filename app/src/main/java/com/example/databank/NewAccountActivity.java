package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
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
        TextInputEditText newAccountName = getTextInputEditText();

        Button saveNewAccount = binding.saveNewAccountButton;
        saveNewAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strAccountName = "";

                if (newAccountName.getText() != null) {
                    strAccountName = newAccountName.getText().toString().trim();
                }

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

    private @NonNull TextInputEditText getTextInputEditText() {
        TextInputEditText newAccountName = binding.newAccountName;

        // makes sure account name does not exceed 20 characters otherwise it overflows
        newAccountName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        // hides the keyboard if clicking outside of the edit text
        newAccountName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        return newAccountName;
    }

    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(EditTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
