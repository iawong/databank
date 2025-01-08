package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityEditAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditAccountActivity extends AppCompatActivity {
    ActivityEditAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout textInputChangeAccountName = binding.textInputChangeAccountName;
        TextInputEditText newAccName = binding.changeAccountName;

        newAccName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyboard(v);
            }
        });

        int accountId = getIntent().getIntExtra("accountId", -1);

        if (accountId == -1) {
            Toast.makeText(this, "Error: Invalid account ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        String accountName = getIntent().getStringExtra("accountName");

        newAccName.setText(accountName);

        // make sure account name does not exceed 20 characters otherwise it overflows
        newAccName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        Button saveChanges = binding.saveEditAccountButton;
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                returnIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                returnIntent.putExtra("accountName", changedAccName);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Button cancel = binding.cancelEditAccountButton;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnToAccounts = new Intent();
                setResult(RESULT_CANCELED, returnToAccounts);
                finish();
            }
        });
    }

    /**
     * Used to hide the keyboard when user clicks away from the view passed in
     * @param v view passed in
     */
    private void hideKeyboard(View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(EditTransactionActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}