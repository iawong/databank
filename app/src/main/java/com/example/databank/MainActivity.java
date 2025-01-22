package com.example.databank;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

/**
 * This is the account activity
 * 1/19/25 notes
 * Transaction sorting is good now. Updated scrolling so that
 * new transactions added or updated will refresh the entire list
 * and the adapter. Continuous scrolling should be possible now.
 * TODO: add toolbar side menu for settings and delete all
 * TODO: export and import data as json to excel
 * TODO: add search functionality for transactions
 * TODO: add activity for data summary like pie charts
 * TODO: rearrange transaction cardview
 */
public class MainActivity extends AppCompatActivity implements OnDeleteListener {
    AccountAdapter accountAdapter; // for populating the recycler view which goes into the main activity
    DatabaseHelper db;
    ArrayList<Integer> accountIds;
    ArrayList<String> accountNames;
    ArrayList<Double> accountBalances;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    // the name of this class "ActivityMainBinding" literally comes from the fact that
    // the class' name is MainActivity. So, in my TransactionActivity class, the binding class
    // name is ActivityTransactionBinding
    ActivityMainBinding binding;

    // ActivityResultLauncher for adding accounts
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

    // ActivityResultLauncher for changing account details
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

    // ActivityResultLauncher for adding transactions
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

        // Set up Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.accountAppbar);
        setSupportActionBar(toolbar);

        // Set up DrawerLayout and NavigationView
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;

        // Set up the hamburger button for the toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                super.onDrawerOpened(drawerView);
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_settings) {
                    System.out.println("settings");
                } else if (id == R.id.nav_help) {
                    System.out.println("help");
                } else if (id == R.id.nav_logout) {
                    System.out.println("logout");
                }

                drawerLayout.closeDrawer(GravityCompat.END);
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

    /**
     * not used
     * @param position transaction position
     * @param accountId account id
     * @param transactionId transaction id
     * @param amount transaction amount
     */
    @Override
    public void onTransactionDelete(int position, int accountId, int transactionId, double amount) {}
}