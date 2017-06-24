package com.lengyel.richard.spendingtracking;

import com.lengyel.richard.spendingtracking.TransactionDbSchema.TransactionTable;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.ParcelUuid;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Richard on 2016.06.04..
 */
public class TransactionCursorWrapper  extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public TransactionCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Transaction getTransaction(){
        String uuidString = getString(getColumnIndex(TransactionTable.Cols.TRANSACTION_ID));
        String name = getString(getColumnIndex(TransactionTable.Cols.NAME));
        String day = getString(getColumnIndex(TransactionTable.Cols.DAY));
        double value = getDouble(getColumnIndex(TransactionTable.Cols.VALUE));
        long date = getLong(getColumnIndex(TransactionTable.Cols.DATE));
        int isFood = getInt(getColumnIndex(TransactionTable.Cols.FOOD));
        int isRegular = getInt(getColumnIndex(TransactionTable.Cols.REGULAR));

        Transaction transaction = new Transaction(ParcelUuid.fromString(uuidString));
        transaction.setValue(value);
        transaction.setName(name);
        transaction.setDate(new Date(date));
        transaction.setDay(day);
        transaction.setIsFood(isFood != 0);
        transaction.setIsRegular(isRegular != 0);

        return transaction;
    }
}
