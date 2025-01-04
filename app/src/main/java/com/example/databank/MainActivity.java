package com.example.databank;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
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
 * 12/31/24 notes
 * TODO: resolve other TODOs around and create new list of items/features
 */
public class MainActivity extends AppCompatActivity implements OnDeleteListener {
    RecyclerView accountRecyclerView;
    AccountAdapter accountAdapter; // for populating the recycler view which goes into the main activity
    DatabaseHelper db;
    ArrayList<Integer> accountIds;
    ArrayList<String> accounts;
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
                        // Reload account data when returning from NewAccountActivity
                        Intent data = activityResult.getData();

                        assert data != null;

                        reloadAccounts();
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

                        assert data != null;

                        reloadAccounts();
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

                        assert data != null;

                        reloadAccounts();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); //getRoot(), get the outer most view

        accountRecyclerView = binding.accountList;

        FloatingActionButton addAccountButton = binding.addAccountButton;
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addNewAccount = new Intent(MainActivity.this, NewAccountActivity.class);
                addAccountLauncher.launch(addNewAccount);
            }
        });

        db = new DatabaseHelper(MainActivity.this);
        accountIds = new ArrayList<>();
        accounts = new ArrayList<>();
        accountBalances = new ArrayList<>();

        storeAccountData();

        accountAdapter = new AccountAdapter(MainActivity.this,
                                            accountIds,
                                            accounts,
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
                // alert dialog to confirm if the user wants to delete all
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete all accounts and transactions? This cannot be undone.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteAll();
                                accountIds.clear();
                                accounts.clear();
                                accountBalances.clear();
                                accountAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }

    /**
     * this sets the account data so that it can be viewed as the most up to date
     */
    private void storeAccountData() {
        Cursor cursor = db.getAllAccounts();

        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "No account data found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                accountIds.add(cursor.getInt(0));
                accounts.add(cursor.getString(1));
                accountBalances.add(cursor.getDouble(2));
            }
        }

        cursor.close();
    }

    /**
     * When we get results, reload the accounts to have the most up to date data
     */
    private void reloadAccounts() {
        accountIds.clear();
        accounts.clear();
        accountBalances.clear();

        // Reload data from the database
        storeAccountData();

        // Notify the adapter of the changes
        // TODO: find better way to notify item change instead
        accountAdapter.notifyDataSetChanged();
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
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this account? This cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db = new DatabaseHelper(MainActivity.this);

                        db.deleteAccount(accountId);

                        accountIds.remove(position);
                        accounts.remove(position);
                        accountBalances.remove(position);
                        accountAdapter.notifyItemRemoved(position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onTransactionDelete(int position, int accountId, int transactionId, double amount) {

    }
}