package com.example.databank;

/**
 * This interface is made to delegate the account deletion to the activity
 * that wants to do this.
 * We separate this operation from the adapter because the adapter is only
 * responsible for displaying data.
 * Having database operations in the account adapter would make it more difficult
 * to test and maintain.
 */
public interface OnDeleteListener {
    void onAccountDelete(int position, int accountId);
    void onTransactionDelete(int position, int accountId, int transactionId, double amount);
}
