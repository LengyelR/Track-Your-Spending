package com.lengyel.richard.spendingtracking;

import com.lengyel.richard.spendingtracking.TransactionDbSchema.TransactionTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Richard on 2016.06.04..
 */
public class TransactionManager {

    public static final String DATE_FORMAT = "yyyy.MM.dd. HH:mm";
    public static final String DAY_FORMAT = "yyyy.MM.dd.";
    public static final String DISPLAY_DATE_FORMAT = "yyyy.MM.dd."; //LLLL : January
    public static final String DISPLAY_TIME_FORMAT = "HH:mm";

    private static TransactionManager sTransactionManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static TransactionManager get(Context context) {
        if (sTransactionManager == null) {
            sTransactionManager = new TransactionManager(context);
        }
        return sTransactionManager;
    }

    private TransactionManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new DatabaseHelper(mContext).getWritableDatabase();
    }



    public void addTransaction(Transaction tr) {
        ContentValues values = getContentValues(tr);
        mDatabase.insert(TransactionTable.NAME, null, values);
    }

    public void deleteAll(){
        mDatabase.delete(TransactionTable.NAME, null, null);
    }

    public List<Transaction> getTransactions(TransactionCursorWrapper  cursor) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                transactions.add(cursor.getTransaction());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return transactions;
    }

    public List<Transaction> getTransactions(boolean onlyNonFood){
        TransactionCursorWrapper cursor;

        if (onlyNonFood)
            cursor = queryTransactions(TransactionTable.Cols.FOOD + " = ?", new String[]{"0"});
        else
            cursor = queryTransactions(null, null);

        return getTransactions(cursor);
    }

    public double getAverageFoodSpent() {
        String queryString = "SELECT AVG(summedPerDay) FROM " +
                "(SELECT SUM(" + TransactionTable.Cols.VALUE + ") AS summedPerDay " +
                "FROM " + TransactionTable.NAME + " " +
                "WHERE " + TransactionTable.Cols.FOOD + " = 1 " +
                "GROUP BY " + TransactionTable.Cols.DAY + " )";
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        cursor.moveToFirst();
        return cursor.getDouble(0);
    }

    public int countDays(){
        String subSelect = "SELECT COUNT(*) " +
                "FROM " + TransactionTable.NAME + " " +
                "WHERE " + TransactionTable.Cols.FOOD + " = 1 " +
                "GROUP BY " + TransactionTable.Cols.DAY;

        String queryString = "SELECT COUNT(*) FROM (" +
                subSelect + ")";
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public double getSum(boolean food){
        String where = String.valueOf(food ? 1 : 0);
        String queryString = "SELECT SUM(" + TransactionTable.Cols.VALUE + ") " +
                "FROM " + TransactionTable.NAME + " " +
                "WHERE " + TransactionTable.Cols.FOOD + " = " + where + " " +
                "AND " + TransactionTable.Cols.REGULAR + " != 1"; //never calculate regulars
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        cursor.moveToFirst();
        return cursor.getDouble(0);
    }

    public double getRegular(){
        String queryString = "SELECT SUM(" + TransactionTable.Cols.VALUE + ") " +
                "FROM " + TransactionTable.NAME + " " +
                "WHERE " + TransactionTable.Cols.REGULAR + " = 1";
        Cursor cursor = mDatabase.rawQuery(queryString, null);
        cursor.moveToFirst();
        return cursor.getDouble(0);
    }

    public Transaction getTransaction(UUID id) {
        TransactionCursorWrapper cursor = queryTransactions(
                TransactionTable.Cols.TRANSACTION_ID + " = ?",
                new String[]{id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getTransaction();
        } finally {
            cursor.close();
        }
    }

    public void updateTransaction(Transaction transaction) {
        String uuidString = transaction.getId().toString();
        ContentValues values = getContentValues(transaction);
        mDatabase.update(TransactionTable.NAME, values,
                TransactionTable.Cols.TRANSACTION_ID + " = ?",
                new String[]{uuidString});
    }

    public void deleteTransaction(Transaction transaction) {
        String uuidString = transaction.getId().toString();
        mDatabase.delete(TransactionTable.NAME,
                TransactionTable.Cols.TRANSACTION_ID + " = ?",
                new String[]{uuidString});
    }

    private static ContentValues getContentValues(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(TransactionTable.Cols.TRANSACTION_ID, transaction.getId().toString());
        values.put(TransactionTable.Cols.DATE, transaction.getDate().getTime());
        values.put(TransactionTable.Cols.NAME, transaction.getName());
        values.put(TransactionTable.Cols.VALUE, transaction.getValue());
        values.put(TransactionTable.Cols.FOOD, transaction.isFood());
        values.put(TransactionTable.Cols.REGULAR, transaction.isRegular());
        values.put(TransactionTable.Cols.DAY, transaction.getDay());
        return values;
    }

    private TransactionCursorWrapper queryTransactions(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                TransactionTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                TransactionTable.Cols.DATE + " DESC"// orderBy
        );
        return new TransactionCursorWrapper(cursor);
    }


}
