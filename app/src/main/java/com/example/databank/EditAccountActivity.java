package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityEditAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityEditAccountBinding binding = ActivityEditAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputChangeAccountName = binding.textInputChangeAccountName;
        TextInputEditText newAccName = binding.changeAccountName;

        InputMethodManager imm = (InputMethodManager) getSystemService(EditAccountActivity.INPUT_METHOD_SERVICE);

        newAccName.setOnFocusChangeListener((v, hasFocus) -> Helper.hideKeyboard(v, imm));

        int accountId = getIntent().getIntExtra("accountId", -1);

        if (accountId == -1) {
            // kicking out of editing the account because we couldn't find the account id
            Toast.makeText(EditAccountActivity.this, "Error: Invalid account ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        String accountName = getIntent().getStringExtra("accountName");

        newAccName.setText(accountName);

        // make sure account name does not exceed 20 characters otherwise it overflows
        newAccName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        Button saveChanges = binding.saveEditAccountButton;
        saveChanges.setOnClickListener(v -> {
            if (newAccName.getText() == null) {
                // kick out of saving since user needs to enter something
                return;
            }

            String changedAccName = newAccName.getText().toString().trim();

            if (changedAccName.isEmpty()) {
                textInputChangeAccountName.setError("Please enter an account name");
                return;
            } else if (changedAccName.equals(accountName)) {
                textInputChangeAccountName.setError("Account name is the same");
                return;
            } else {
                textInputChangeAccountName.setError(null);
            }

            try (DatabaseHelper db = new DatabaseHelper(EditAccountActivity.this)) {
                db.updateAccount(accountId, changedAccName);
            } catch (Exception e) {
                Log.e("EditAccountActivity", "Error editing account", e);
                Toast.makeText(EditAccountActivity.this, "Failed to edit account. Please try again", Toast.LENGTH_SHORT).show();
            }

            Intent returnIntent = new Intent();

            // position of the account we're editing from the account adapter
            returnIntent.putExtra("accountPosition", getIntent().getIntExtra("position", -1));
            returnIntent.putExtra("accountName", changedAccName);
            setResult(RESULT_OK, returnIntent);
            finish();
        });

        Button cancel = binding.cancelEditAccountButton;
        cancel.setOnClickListener(v -> {
            Intent returnToAccounts = new Intent();
            setResult(RESULT_CANCELED, returnToAccounts);
            finish();
        });
    }
}