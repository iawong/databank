package com.example.databank;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databank.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * This is the account activity
 * 1/26/25 notes
 * Added the export functionality
 * TODO: add search functionality for transactions
 * TODO: add activity for data summary like pie charts
 * TODO: change onUpgrade method in databaseHelper to create a temp table before dropping old table
 * TODO: import json file and read to database
 * TODO: rearrange transaction cardview
 * TODO: update account balance "hidden" string to be grey
 * TODO: update settings to open up like SAO settings on bottom right
 */
public class MainActivity extends AppCompatActivity implements OnDeleteListener {
    private AccountAdapter accountAdapter; // for populating the recycler view which goes into the main activity
    private DatabaseHelper db;
    private ArrayList<Integer> accountIds;
    private ArrayList<String> accountNames;
    private ArrayList<Double> accountBalances;
    private DrawerLayout drawerLayout;
    // the name of this class "ActivityMainBinding" literally comes from the fact that
    // the class' name is MainActivity. So, in my TransactionActivity class, the binding class
    // name is ActivityTransactionBinding
    private ActivityMainBinding binding;

    // ActivityResultLauncher for adding accounts
    private ActivityResultLauncher<Intent> addAccountLauncher = registerForActivityResult(
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

    // ActivityResultLauncher for changing account details
    private ActivityResultLauncher<Intent> accountChangeResultLauncher = registerForActivityResult(
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

    // ActivityResultLauncher for adding transactions
    private ActivityResultLauncher<Intent> transactionResultLauncher = registerForActivityResult(
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

        // Set up Toolbar
        MaterialToolbar toolbar = findViewById(R.id.accountAppbar);
        setSupportActionBar(toolbar);

        // Override actionbar title because it defaults to the app title
        // despite setting the title in the activity_main.xml
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.account_appbar));
        }

        // Set up DrawerLayout and NavigationView
        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navigationView;

        // Set up the hamburger button for the toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up navigation item actions
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_export) {
                    try {
                        exportDatabaseToJson();
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error exporting to JSON", e);
                    }
                } else if (id == R.id.nav_import) {
                } else if (id == R.id.nav_delete_all) {
                    deleteAllAlertView();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

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

        // account adapter to handle account and transaction changes
        accountAdapter = new AccountAdapter(MainActivity.this,
                                            accountIds,
                                            accountNames,
                                            accountBalances,
                                            transactionResultLauncher,
                                            accountChangeResultLauncher,
                                            MainActivity.this);
        accountRecyclerView.setAdapter(accountAdapter);
        accountRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
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

    /**
     * not used
     * @param position transaction position
     * @param accountId account id
     * @param transactionId transaction id
     * @param amount transaction amount
     */
    @Override
    public void onTransactionDelete(int position, int accountId, int transactionId, double amount) {}

    /**
     * Creates the delete all alert view
     */
    private void deleteAllAlertView() {
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

    /**
     * Exports the database as a JSON file
     * @throws JSONException json exception if it fails to create a json file
     */
    private void exportDatabaseToJson() throws JSONException {
        // get all the account data and write to the JSON array
        Cursor accountsCursor = db.getAllAccounts();
        JSONArray accountsJsonArray = new JSONArray();

        if (accountsCursor.moveToFirst()) {
            do {
                JSONObject accountObject = new JSONObject();
                accountObject.put("account_id", accountsCursor.getInt(0));
                accountObject.put("account_name", accountsCursor.getString(1));
                accountObject.put("balance", accountsCursor.getDouble(2));
                accountsJsonArray.put(accountObject);
            } while (accountsCursor.moveToNext());
        }
        accountsCursor.close();

        // get all the transaction data and write to the JSON array
        Cursor transactionsCursor = db.getAllTransactions();
        JSONArray transactionsJsonArray = new JSONArray();

        if (transactionsCursor.moveToFirst()) {
            do {
                JSONObject transactionObject = new JSONObject();
                transactionObject.put("transaction_id", transactionsCursor.getInt(0));
                transactionObject.put("account_id", transactionsCursor.getInt(1));
                transactionObject.put("amount", transactionsCursor.getDouble(2));
                transactionObject.put("description", transactionsCursor.getString(3));
                transactionObject.put("date", transactionsCursor.getString(4));
                transactionObject.put("category", transactionsCursor.getString(5));
                transactionsJsonArray.put(transactionObject);
            } while (transactionsCursor.moveToNext());
        }
        transactionsCursor.close();

        // Create a JSONObject to hold both tables
        JSONObject exportJson = new JSONObject();
        exportJson.put("accounts", accountsJsonArray);
        exportJson.put("transactions", transactionsJsonArray);

        // Write the JSON to a file
        writeJsonToFile(exportJson.toString());

        Toast.makeText(this, "Data exported successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Writes the json string into a file and saves it to the app folders
     * @param jsonString json string to write into a file
     */
    private void writeJsonToFile(String jsonString) {
        try {
            // Define the file path and name
            File exportDir = new File(getExternalFilesDir(null), "exports");

            // Create directory if it doesn't exist
            if (!exportDir.exists()) {
                boolean directoryMade = exportDir.mkdirs();

                if (!directoryMade) {
                    Toast.makeText(MainActivity.this, "Directory not made", Toast.LENGTH_SHORT).show();
                }
            }

            File jsonFile = new File(exportDir, "accounts_and_transactions.json");

            // Write the JSON string to the file
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(jsonString);
            writer.flush();
            writer.close();

            Toast.makeText(this, "Exported to: " + jsonFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // the absolute path: /storage/emulated/0/Android/data/com.example.databank/files/exports
        } catch (Exception e) {
            Log.e("MainActivity", "Error writing JSON to file", e);
            Toast.makeText(this, "Failed to write file!", Toast.LENGTH_SHORT).show();
        }
    }
}