package com.example.databank;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databank.databinding.ActivityNewAccountBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity for creating a new account
 */
public class NewAccountActivity extends AppCompatActivity {
    private ActivityNewAccountBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InputMethodManager imm = (InputMethodManager) getSystemService(NewAccountActivity.INPUT_METHOD_SERVICE);

        TextInputLayout textInputAccountName = binding.textInputAccountName;
        TextInputEditText newAccountName = getTextInputEditText(imm);

        Button saveNewAccount = binding.saveNewAccountButton;
        saveNewAccount.setOnClickListener(v -> {
            if (newAccountName.getText() == null) {
                // kick out of saving since user needs to enter something
                return;
            }

            String strAccountName = newAccountName.getText().toString().trim();

            if (strAccountName.isEmpty()) {
                textInputAccountName.setError("Please enter an account name");
                return;
            } else {
                textInputAccountName.setError(null);
            }

            long accountId = -1;

            try (DatabaseHelper db = new DatabaseHelper(NewAccountActivity.this)) {
                accountId = db.addAccount(strAccountName);

                if (accountId == -1) {
                    // failed to add account, try again
                    return;
                }
            } catch (Exception e) {
                Log.e("NewAccountActivity", "Error adding account", e);
                Toast.makeText(NewAccountActivity.this, "Failed to add account. Please try again.", Toast.LENGTH_SHORT).show();
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("accountId", accountId);
            returnIntent.putExtra("accountName", strAccountName);
            setResult(RESULT_OK, returnIntent);

            finish();
        });

        Button back = binding.cancelNewAccountButton;
        back.setOnClickListener(v -> {
            Intent returnToAccounts = new Intent();
            setResult(RESULT_CANCELED, returnToAccounts);
            finish();
        });
    }

    /**
     * Get account name from user
     * @return TextInputEditText with new account name
     */
    private @NonNull TextInputEditText getTextInputEditText(InputMethodManager imm) {
        TextInputEditText newAccountName = binding.newAccountName;

        // makes sure account name does not exceed 20 characters otherwise it overflows
        newAccountName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        // hides the keyboard if clicking outside the edit text
        newAccountName.setOnFocusChangeListener((v, hasFocus) -> Helper.hideKeyboard(v, imm));

        return newAccountName;
    }
}
