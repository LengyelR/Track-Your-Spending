package com.lengyel.richard.spendingtracking;


import  com.lengyel.richard.spendingtracking.TransactionDbSchema.TransactionTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by Richard on 2016.03.20..
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION_1 = 1;
    private static final int ADDED_DAY_COLUMN = 2;
    private static final int ADDED_REGULAR_COLUMN = 3;
    private static final int LATEST = 3;
    private static final String DATABASE_NAME = "transactionTracks.db";
    private static final String DB_PATH = "//data//com.lengyel.richard.spendingtracking//databases//transactionTracks.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, LATEST);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TransactionTable.NAME + "(" +
                        " _id integer primary key autoincrement, " +
                        TransactionTable.Cols.TRANSACTION_ID + ", " +
                        TransactionTable.Cols.NAME + ", " +
                        TransactionTable.Cols.VALUE + ", " +
                        TransactionTable.Cols.FOOD + ", " +
                        TransactionTable.Cols.DAY + ", " +
                        TransactionTable.Cols.REGULAR + ", " +
                        TransactionTable.Cols.DATE  +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        backupDb();
        if (oldVersion < ADDED_DAY_COLUMN) {
            db.execSQL("ALTER TABLE " + TransactionTable.NAME +
                      " ADD COLUMN " + TransactionTable.Cols.DAY + " TEXT DEFAULT ''");
        } else if (oldVersion < ADDED_REGULAR_COLUMN){
            db.execSQL("ALTER TABLE " + TransactionTable.NAME +
                    " ADD COLUMN " + TransactionTable.Cols.REGULAR + " INT DEFAULT 0");
        }
    }

    public static void backupDb() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String backupDBPath = "backup.db";
                File currentDB = new File(data, DB_PATH);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception ex) {
        }
    }

    public static void replaceDb(String inputFilePath) {
        try{
            InputStream mInput = new FileInputStream(inputFilePath);
            OutputStream mOutput = new FileOutputStream("//data" + DB_PATH);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = mInput.read(mBuffer))>0)
            {
                mOutput.write(mBuffer, 0, mLength);
            }
            mOutput.flush();
            mOutput.close();
            mInput.close();
        }catch (Exception ex){
            Log.e("argh...",ex.toString());
        }
    }
}
