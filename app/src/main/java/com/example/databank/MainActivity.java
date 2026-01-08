package com.example.databank;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * This is the main activity for the app and shows accounts.
 * 1/7/26
 * tabled the show all values functionality, it's more important for me to
 * start on the pie chart since that's the whole point of this app.
 * TODO: figure out why pie chart isn't showing colors
 * TODO: add transaction transfer/transfer (should not be available when editing)
 * TODO: consolidate hide and show for amounts
 * TODO: add search functionality for transactions
 * TODO: change onUpgrade method in databaseHelper to create a temp table before dropping old table
 * TODO: sqlite migration features for database structure changes
 * TODO: update account balance "hidden" string to be grey
 * TODO: update settings to open up like SAO settings on bottom right
 * TODO: re-eval how I'm creating the date picker fields
 * TODO: override back button to finish activities. This should replace the back buttons I've added.
 */
public class MainActivity extends AppCompatActivity implements OnDeleteListener {
    private AccountAdapter accountAdapter; // for populating the recycler view which goes into the main activity
    private DatabaseHelper db;
    private ArrayList<Integer> accountIds;
    private ArrayList<String> accountNames;
    private ArrayList<Double> accountBalances;
    private DrawerLayout drawerLayout;

    // ActivityResultLauncher for adding accounts
    private final ActivityResultLauncher<Intent> addAccountLauncher = registerForActivityResult(
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
    private final ActivityResultLauncher<Intent> accountChangeResultLauncher = registerForActivityResult(
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
    private final ActivityResultLauncher<Intent> transactionResultLauncher = registerForActivityResult(
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

    // ActivityResultLauncher for getting the file path to save exported json files
    private final ActivityResultLauncher<Intent> exportDatabaseResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(MainActivity.this, "Unable to export file", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Uri fileUri = data.getData();

                        if (fileUri != null) {
                            saveDatabaseToUri(fileUri);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> importDatabaseResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int resultCode = activityResult.getResultCode();

                    if (resultCode == RESULT_OK) {
                        Intent data = activityResult.getData();

                        if (data == null) {
                            Toast.makeText(MainActivity.this, "Unable to export file", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Uri fileUri = data.getData();

                        if (fileUri != null) {
                            replaceDatabase(fileUri);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // the name of this class "ActivityMainBinding" literally comes from the fact that
        // the class' name is MainActivity. So, in my TransactionActivity class, the binding class
        // name is ActivityTransactionBinding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
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

                if(id == R.id.nav_show_values) {
                    showValues();
                } else if (id == R.id.nav_summary) {
                    showSummary();
                } else if (id == R.id.nav_export) {
                    exportDatabase();
                } else if (id == R.id.nav_import) {
                    importDatabase();
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
     * Exports the database as a file and asks user to name the file and save into a directory
     */
    private void exportDatabase() {
        try {
            File dbFile = getDatabasePath(db.getDatabaseName());

            if (!dbFile.exists()) {
                Toast.makeText(MainActivity.this, "Database file not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent saveDatabaseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            saveDatabaseFile.addCategory(Intent.CATEGORY_OPENABLE);
            saveDatabaseFile.setType("application/vnd.sqlite3");
            saveDatabaseFile.putExtra(Intent.EXTRA_TITLE, db.getDatabaseName());

            exportDatabaseResultLauncher.launch(saveDatabaseFile);
        } catch (Exception e) {
            Log.e("MainActivity.exportDatabase", "Error exporting database file", null);
            Toast.makeText(MainActivity.this, "Export failed!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Writes the file out into the directory
     * @param uri file path to save the file
     */
    private void saveDatabaseToUri(Uri uri) {
        try {
            File dbFile = getDatabasePath(db.getDatabaseName());
            InputStream inputStream = Files.newInputStream(dbFile.toPath());
            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            if (outputStream == null) {
                Toast.makeText(MainActivity.this, "Could not export database, URI not found", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(this, "Database exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity.saveDatabaseToUri", "Error exporting database file", null);
            Toast.makeText(MainActivity.this, "Failed to export database!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Import a database file to populate data
     */
    private void importDatabase() {
        Intent importDatabaseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        importDatabaseFile.addCategory(Intent.CATEGORY_OPENABLE);
        importDatabaseFile.setType("*/*");

        importDatabaseResultLauncher.launch(importDatabaseFile);
    }

    /**
     * Replace current database file with the selected file
     */
    private void replaceDatabase(Uri fileUri) {
        try {
            File dbFile = getDatabasePath(db.getDatabaseName());
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            OutputStream outputStream = Files.newOutputStream(dbFile.toPath());

            if (inputStream == null) {
                Toast.makeText(MainActivity.this, "Database file not found, bad URI", Toast.LENGTH_SHORT).show();
                return;
            }

            // close current db connection, will reopen after new db file is written in
            db.close();

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(this, "Database imported successfully!", Toast.LENGTH_SHORT).show();

            recreate(); // Restart the activity to reload data
            db = new DatabaseHelper(MainActivity.this);
        } catch (Exception e) {
            Log.e("MainActivity.replaceDatabase", "Error replacing database file", null);
            Toast.makeText(this, "Failed to import database!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows all account values. This button shows up on the drawer on the top left.
     */
    private void showValues() {
        // TODO: implement
    }

    /**
     * Shows a summary of all my transactions.
     */
    private void showSummary() {
        Intent intent = new Intent(this, TransactionSummary.class);
        this.startActivity(intent);
    }
}