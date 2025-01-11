package com.example.databank;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * This is the account activity
 * 1/8/25 notes
 * Cleaned up some code. Namely, getting rid of warnings.
 * TODO: clean up reloadTransactions method
 * TODO: add categories to transactions
 * TODO: resolve other TODOs around and create new list of items/features
 */
public class MainActivity extends AppCompatActivity implements OnDeleteListener {
    AccountAdapter accountAdapter; // for populating the recycler view which goes into the main activity
    DatabaseHelper db;
    ArrayList<Integer> accountIds;
    ArrayList<String> accountNames;
    ArrayList<Double> accountBalances;

    // the name of this class "ActivityMainBinding" literally comes from the fact that
    // the class' name is MainActivity. So, in my TransactionActivity class, the binding class
    // name is ActivityTransactionBinding
    ActivityMainBinding binding;

    /**
     * ActivityResultLauncher for adding accounts
     */
    ActivityResultLauncher<Intent> addAccountLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(MainActivity.this, "Account could not be added", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long accountId = data.getLongExtra("accountId", -1);
                        String accountName = data.getStringExtra("accountName");

                        insertItemIntoAccounts((int) accountId, accountName);
                    }
                }
            }
    );

    /**
     * ActivityResultLauncher for changing account details
     * Will come back and update the account name/balances if changes were made
     */
    ActivityResultLauncher<Intent> accountChangeResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(MainActivity.this, "Account changed data not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int accountPosition = data.getIntExtra("accountPosition", -1);

                        if (accountPosition == -1) {
                            Toast.makeText(MainActivity.this, "Account to change not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String changedAccountName = data.getStringExtra("accountName");

                        updateAccount(accountPosition, changedAccountName);
                    }
                }
            }
    );

    /**
     * ActivityResultLauncher for adding transactions
     * Will come back and update the account balances if the transactions have changed
     */
    ActivityResultLauncher<Intent> transactionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(MainActivity.this, "Transaction not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int accountPosition = data.getIntExtra("position", -1);

                        if (accountPosition == -1) {
                            Toast.makeText(MainActivity.this, "Unable to add transaction", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int accountId = data.getIntExtra("accountId", -1);

                        if (accountId == -1) {
                            Toast.makeText(MainActivity.this, "Unable to find account", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        reloadAccount(accountPosition, accountId);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView); //getRoot(), get the outer most view

        RecyclerView accountRecyclerView = binding.accountList;

        FloatingActionButton addAccountButton = binding.addAccountButton;
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addNewAccount = new Intent(MainActivity.this, NewAccountActivity.class);
                addAccountLauncher.launch(addNewAccount);
            }
        });

        db = new DatabaseHelper(MainActivity.this);
        accountIds = new ArrayList<>();
        accountNames = new ArrayList<>();
        accountBalances = new ArrayList<>();

        storeAccountData();

        accountAdapter = new AccountAdapter(MainActivity.this,
                                            accountIds,
                                            accountNames,
                                            accountBalances,
                                            transactionResultLauncher,
                                            accountChangeResultLauncher,
                                            MainActivity.this);
        accountRecyclerView.setAdapter(accountAdapter);
        accountRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        FloatingActionButton deleteAllButton = binding.deleteAllButton;
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View alertView = getLayoutInflater().inflate(R.layout.alert_dialog, null);

                Button positiveButton = alertView.findViewById(R.id.alertPositiveButton);
                Button negativeButton = alertView.findViewById(R.id.alertNegativeButton);

                // alert dialog to confirm if the user wants to delete all
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(alertView)
                        .setCancelable(false)
                        .show();

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onClick(View v) {
                        db.deleteAll();
                        accountIds.clear();
                        accountNames.clear();
                        accountBalances.clear();
                        // using this instead of a more specific notify because we're
                        // deleting all accounts
                        accountAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });

                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                }
            }
        });
    }

    /**
     * this queries the account table and fills up the account
     * arraylists with the account data so that it can be viewed as
     * the most up to date information
     */
    private void storeAccountData() {
        Cursor cursor = db.getAllAccounts();

        // TODO: use background image for no accounts instead of toast
        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "No account data found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                accountIds.add(cursor.getInt(0));
                accountNames.add(cursor.getString(1));
                accountBalances.add(cursor.getDouble(2));
            }
        }

        cursor.close();
    }

    /**
     * reload the specific account to show the most up to date info
     * @param position position of the account to reload
     * @param accountId account id of the account to grab new balance for
     */
    private void reloadAccount(int position, int accountId) {
        Cursor cursor = db.getAccount(accountId);

        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "No account data found", Toast.LENGTH_SHORT).show();
        } else {
            cursor.moveToFirst();

            accountBalances.set(position, cursor.getDouble(2));
            accountAdapter.notifyItemChanged(position);
        }
    }

    /**
     * Inserts the new account
     * @param accountId account id of new account
     * @param accountName account name of new account
     */
    private void insertItemIntoAccounts(int accountId, String accountName) {
        accountIds.add(accountId);
        accountNames.add(accountName);
        accountBalances.add(0.0);

        // Notify the adapter of the changes
        // accountIds size - 1 should be the position/index of the new account
        accountAdapter.notifyItemInserted(accountIds.size() - 1);
    }

    /**
     * Update the account name
     * @param position position of the account to update
     * @param changedAccName the changed account name
     */
    private void updateAccount(int position, String changedAccName) {
        accountNames.set(position, changedAccName);

        accountAdapter.notifyItemChanged(position);
    }

    /**
     * close the database when closing the app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // close the database connection when the app is closed
        if (db != null) {
            db.close();
        }
    }

    /**
     * delete account
     * @param position account position
     * @param accountId account id
     */
    @Override
    public void onAccountDelete(int position, int accountId) {
        View alertView = getLayoutInflater().inflate(R.layout.alert_dialog, null);

        Button positiveButton = alertView.findViewById(R.id.alertPositiveButton);
        Button negativeButton = alertView.findViewById(R.id.alertNegativeButton);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(alertView)
                .setCancelable(false)
                .create();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteAccount(accountId);

                accountIds.remove(position);
                accountNames.remove(position);
                accountBalances.remove(position);
                accountAdapter.notifyItemRemoved(position);

                dialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }
    }

    @Override
    public void onTransactionDelete(int position, int accountId, int transactionId, double amount) {}
}