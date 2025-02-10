package com.example.databank;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * TODO: close db connections when done
 * This class creates the account and transactions database and
 * holds all the functions for getting/inserting/updating/deleting
 * account and transaction information
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "databank.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME_ACCOUNT = "accounts";
    private static final String COLUMN_ACCOUNT_ID = "account_id";
    private static final String COLUMN_ACCOUNT_NAME = "account_name";
    private static final String COLUMN_ACCOUNT_BALANCE = "balance";
    private static final String TABLE_NAME_TRANSACTION = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "transaction_id";
    private static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    private static final String COLUMN_TRANSACTION_DESCRIPTION = "description";
    private static final String COLUMN_TRANSACTION_DATE = "date";
    private static final String COLUMN_TRANSACTION_CATEGORY = "category";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table creation SQL
        String CREATE_ACCOUNTS_TABLE =
            "CREATE TABLE " + TABLE_NAME_ACCOUNT + " (" +
            COLUMN_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ACCOUNT_NAME + " TEXT NOT NULL, " +
            COLUMN_ACCOUNT_BALANCE + " REAL NOT NULL);";

        String CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE " + TABLE_NAME_TRANSACTION + " (" +
            COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ACCOUNT_ID + " INTEGER NOT NULL, " +
            COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL, " +
            COLUMN_TRANSACTION_DESCRIPTION + " TEXT, " +
            COLUMN_TRANSACTION_DATE + " TEXT NOT NULL, " +
            COLUMN_TRANSACTION_CATEGORY + " TEXT, " +
            "FOREIGN KEY (" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_NAME_ACCOUNT + " (" + COLUMN_ACCOUNT_ID + "));";

        String CREATE_INDEX_ON_DATE =
                "CREATE INDEX idx_transaction_date ON " + TABLE_NAME_TRANSACTION + " (" + COLUMN_ACCOUNT_ID +
                ", " + COLUMN_TRANSACTION_DATE + ");";

        db.execSQL(CREATE_ACCOUNTS_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_INDEX_ON_DATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TRANSACTION);
        onCreate(db);
    }

    long addAccount(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ACCOUNT_NAME, name);
        values.put(COLUMN_ACCOUNT_BALANCE, 0);

        long resultCode = db.insert(TABLE_NAME_ACCOUNT, null, values);

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to insert account: " + name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Added account: " + name, Toast.LENGTH_SHORT).show();
        }

        return resultCode;
    }

    long addTransaction(int accountId, double amount, String description, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ACCOUNT_ID, accountId);
        values.put(COLUMN_TRANSACTION_AMOUNT, amount);
        values.put(COLUMN_TRANSACTION_DESCRIPTION, description);
        values.put(COLUMN_TRANSACTION_DATE, date);
        values.put(COLUMN_TRANSACTION_CATEGORY, category);

        long resultCode = db.insert(TABLE_NAME_TRANSACTION, null, values);

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to insert transaction amount: " + amount, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Added transaction amount: " + amount, Toast.LENGTH_SHORT).show();
        }

        // grab the account row by accountId and add transaction amount to the account balance
        Cursor account = getAccount(accountId);
        account.moveToFirst();
        double balance = account.getDouble(2) + amount;
        updateAccountBalance(accountId, balance);

        return resultCode;
    }

    void updateAccount(int accountId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ACCOUNT_NAME, name);

        int resultCode = db.update(TABLE_NAME_ACCOUNT, values, COLUMN_ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)});

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to update account: " + name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Updated account: " + name, Toast.LENGTH_SHORT).show();
        }
    }

    void updateAccountBalance(int accountId, double balance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ACCOUNT_BALANCE, balance);

        int resultCode = db.update(TABLE_NAME_ACCOUNT, values, COLUMN_ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)});

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to update account balance", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Updated account balance amount", Toast.LENGTH_SHORT).show();
        }
    }

    void updateTransaction(int accountId, int transactionId, double newAmount, String description, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Cursor transaction = getTransactionAmount(accountId, transactionId);

        transaction.moveToFirst();
        double curAmount = transaction.getDouble(0);

        values.put(COLUMN_TRANSACTION_AMOUNT, newAmount);
        values.put(COLUMN_TRANSACTION_DESCRIPTION, description);
        values.put(COLUMN_TRANSACTION_DATE, date);
        values.put(COLUMN_TRANSACTION_CATEGORY, category);

        int resultCode = db.update(TABLE_NAME_TRANSACTION, values, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to update transaction", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Updated transaction", Toast.LENGTH_SHORT).show();
        }

        // grab the account row by accountId and add transaction newAmount to the account balance
        Cursor account = getAccount(accountId);
        account.moveToFirst();
        double balance = account.getDouble(2) + newAmount - curAmount;
        updateAccountBalance(accountId, balance);
    }

    void deleteAccount(int accountId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int resultCode = db.delete(TABLE_NAME_ACCOUNT, COLUMN_ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)});

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted account", Toast.LENGTH_SHORT).show();
        }

        deleteAccountTransactions(accountId);
    }

    void deleteAccountTransactions(int accountId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int resultCode = db.delete(TABLE_NAME_TRANSACTION, COLUMN_ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)});

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to delete all account transactions", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted all account transactions", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteTransaction(int accountId, int transactionId, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();

        int resultCode = db.delete(TABLE_NAME_TRANSACTION, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});

        if (resultCode == -1) {
            Toast.makeText(context, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted transaction", Toast.LENGTH_SHORT).show();
        }

        // grab the account row by accountId and subtract the transaction amount from the account balance
        Cursor account = getAccount(accountId);
        account.moveToFirst();
        double balance = account.getDouble(2) - amount;
        updateAccountBalance(accountId, balance);
    }

    void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        int resultCodeAccount = db.delete(TABLE_NAME_ACCOUNT, null, null);

        if (resultCodeAccount == -1) {
            Toast.makeText(context, "Failed to delete all accounts", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted all accounts", Toast.LENGTH_SHORT).show();
        }

        int resultCodeTransaction = db.delete(TABLE_NAME_TRANSACTION, null, null);

        if (resultCodeTransaction == -1) {
            Toast.makeText(context, "Failed to delete all transactions", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Deleted all transactions", Toast.LENGTH_SHORT).show();
        }

        onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION + 1);
    }

    Cursor getAllAccounts() {
        String query = "SELECT * FROM " + TABLE_NAME_ACCOUNT;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    Cursor getAccount(int accountId) {
        String query = "SELECT * FROM " + TABLE_NAME_ACCOUNT +
                " WHERE " + COLUMN_ACCOUNT_ID + " = " + accountId;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    Cursor getAccountTransactions(int accountId, int limit, int offset) {
        String query = "SELECT * FROM " + TABLE_NAME_TRANSACTION +
                " WHERE " + COLUMN_ACCOUNT_ID + " = ? " +
                " ORDER BY " + COLUMN_TRANSACTION_DATE + " DESC " +
                " LIMIT ? OFFSET ?";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, new String[] {
                    String.valueOf(accountId),
                    String.valueOf(limit),
                    String.valueOf(offset)
            });
        }

        return cursor;
    }

    Cursor getAllTransactions() {
        String query = "SELECT * FROM " + TABLE_NAME_TRANSACTION;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    Cursor getTransactionAmount(int accountId, int transactionId) {
        String query = "SELECT " + COLUMN_TRANSACTION_AMOUNT +
                " FROM " + TABLE_NAME_TRANSACTION +
                " WHERE " + COLUMN_TRANSACTION_ID + " = " + transactionId +
                " AND " + COLUMN_ACCOUNT_ID + " = " + accountId;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    Cursor getTransaction(int accountId, int transactionId) {
        String query = "SELECT * FROM " + TABLE_NAME_TRANSACTION + " WHERE " +
                COLUMN_ACCOUNT_ID + " = " + accountId + " AND " + COLUMN_TRANSACTION_ID +
                " = " + transactionId;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }
}
