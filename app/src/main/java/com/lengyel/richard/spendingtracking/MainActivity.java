package com.lengyel.richard.spendingtracking;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NewTransactionDialog.NoticeDialogListener{

    private static final String NEW_TRANSACTION = "NewTransactionDialogFragment";
    private static final int CHOOSE_DB_FILE = 0;
    public static final int REQUEST_WRITE_PERMISSION = 1;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TransactionAdapter mAdapter;
    private TextView mValueTextView;
    private TextView mAverageTextView;
    private TextView mPredictionTextView;
    private TextView mDayCountTextView;
    private TextView mFoodSumTextView;
    private TextView mNonFoodSumTextView;
    private TextView mRegularSumTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mValueTextView = (TextView) findViewById(R.id.value);
        mPredictionTextView = (TextView) findViewById(R.id.prediction);
        mAverageTextView = (TextView) findViewById(R.id.average);
        mDayCountTextView = (TextView) findViewById(R.id.day_count);
        mFoodSumTextView = (TextView) findViewById(R.id.food_sum);
        mNonFoodSumTextView = (TextView) findViewById(R.id.non_food_sum);
        mRegularSumTextView = (TextView) findViewById(R.id.regular_sum);

        mRecyclerView = (RecyclerView) findViewById(R.id.transaction_recycler_view);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        updateUI();

        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = NewTransactionDialog.newInstance(null);
                dialogFragment.show(getSupportFragmentManager(), NEW_TRANSACTION);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: won't be called after leaving settings screen
        updateUI();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //TODO: material design, is it justified to put these here?
        switch(id){
            //TODO: create separate class for AlertDialog
            case R.id.action_delete_all:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Alert!!");
                alert.setMessage("Are you sure to delete the database?");

                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TransactionManager.get(getApplicationContext()).deleteAll();
                        updateUI();
                        dialog.dismiss();
                    }
                });

                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                break;

            case R.id.action_backup_db:
                checkPermission();

                DatabaseHelper.backupDb();

                String backupDBPath = "backup.db";
                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File backupDB = new File(sd, backupDBPath);

                //TODO: marshmallow
                if (backupDB.isFile())
                    startActivity(new Intent()
                            .setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupDB))
                            .setType("text/plain"));
                else
                    Toast.makeText(this,"File not exists!",Toast.LENGTH_SHORT).show();

                break;
            case R.id.action_replace_db:
                checkPermission();

                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, CHOOSE_DB_FILE);
                break;
            case R.id.action_settings:
                Intent i = new Intent(this, Preferences.class);
                startActivity(i);
                break;
//            case R.id.action_food_filter:
//                filterUI();
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void filterUI() {
        TransactionManager transactionManager = TransactionManager.get(getApplicationContext());
        List<Transaction> transactions = transactionManager.getTransactions(true);

        if (mAdapter == null) {
            mAdapter = new TransactionAdapter(transactions,this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTransactions(transactions);
            mAdapter.notifyDataSetChanged();
        }
    }

    //TODO: not user friendly, should show an error
    private String safeBalanceRead(SharedPreferences preferences){
        String def = "1000";
        String val = preferences.getString(getString(R.string.balance_key), def);
        return val == "" ? def : val;
    }

    private void updateUI() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        double regularOutgoings = Double.parseDouble(
                preferences.getString(getString(R.string.outgoings_key), "750"));
        double currentBalance = Double.parseDouble(safeBalanceRead(preferences));

        TransactionManager transactionManager = TransactionManager.get(getApplicationContext());
        List<Transaction> transactions = transactionManager.getTransactions(false);


        if (!transactions.isEmpty()){
            DecimalFormat formatter = new DecimalFormat("#.0#");
            double transactionSum = 0;
            for (Transaction tr : transactions) {
                transactionSum += tr.getValue();
            }

            double averageFoodSpent = transactionManager.getAverageFoodSpent();
            double foodSum = transactionManager.getSum(true);
            double nonFoodSum = transactionManager.getSum(false);
            double regularSum = transactionManager.getRegular();
            int dayCount = transactionManager.countDays();
            int daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

            double foodPrediction = (daysInMonth-dayCount)*averageFoodSpent;
            double finalPrediction = transactionSum + regularOutgoings - regularSum + foodPrediction;

            String predictionText = "(" + formatter.format(finalPrediction) + ")";
            String foodText = formatter.format(foodSum) + " (" + formatter.format(foodPrediction+foodSum) + ")";
            String regularText = formatter.format(regularSum) + " (" + formatter.format(regularOutgoings) + ")";
            String daysText = String.valueOf(dayCount) + " (" + String.valueOf(daysInMonth) + ")";
            String balanceText = formatter.format(currentBalance-transactionSum);
            String finalPredictionText =  String.format("(prediction: %s, goal: %s)",
                    formatter.format(currentBalance-finalPrediction),
                    formatter.format(currentBalance-1500)); //TODO: hardcoded...should be a config

            mValueTextView.setText(formatter.format(transactionSum));
            mPredictionTextView.setText(predictionText);
            mAverageTextView.setText(formatter.format(averageFoodSpent));
            mDayCountTextView.setText(daysText);
            mFoodSumTextView.setText(foodText);
            mNonFoodSumTextView.setText(formatter.format(nonFoodSum));
            mRegularSumTextView.setText(regularText);
            mToolbar.setTitle(balanceText);
            mToolbar.setSubtitle(finalPredictionText);

        } else {
            mValueTextView.setText(R.string.sum);
            mPredictionTextView.setText("");
            mAverageTextView.setText("");
            mDayCountTextView.setText("");
            mNonFoodSumTextView.setText("");
            mRegularSumTextView.setText("");
            mFoodSumTextView.setText("");
            mToolbar.setTitle(safeBalanceRead(preferences));
        }

        if (mAdapter == null) {
            mAdapter = new TransactionAdapter(transactions,this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTransactions(transactions);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Transaction tr = dialog.getArguments().getParcelable(NewTransactionDialog.EXTRA_TRANSACTION);
        boolean update = tr != null;

        SimpleDateFormat dateFormat = new SimpleDateFormat(TransactionManager.DATE_FORMAT);

        EditText nameEditText = (EditText) dialog.getDialog().findViewById(R.id.name_edit);
        EditText valueEditText = (EditText) dialog.getDialog().findViewById(R.id.value_edit);
        TextView datePickerText = (TextView) dialog.getDialog().findViewById(R.id.date_picker);
        TextView timePickerText = (TextView) dialog.getDialog().findViewById(R.id.time_picker);
        CheckBox mIsFoodCheckBox = (CheckBox) dialog.getDialog().findViewById(R.id.is_food);
        CheckBox mIsRegularCheckBox = (CheckBox) dialog.getDialog().findViewById(R.id.is_regular);

        String name = nameEditText.getText().toString();
        double value = Double.valueOf(valueEditText.getText().toString());
        boolean isFood = mIsFoodCheckBox.isChecked();
        boolean isRegular = mIsRegularCheckBox.isChecked();
        Date date = null;
        Date time = null;
        try {
            String date_text = datePickerText.getText().toString();
            String time_text = timePickerText.getText().toString();
            date = dateFormat.parse(date_text + " " + time_text);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (tr == null)
            tr = new Transaction();

        tr.setDate(date);
        tr.setDay(date);
        tr.setName(name);
        tr.setValue(value);
        tr.setIsFood(isFood);
        tr.setIsRegular(isRegular);

        if (update)
            TransactionManager.get(getApplicationContext()).updateTransaction(tr);
        else
            TransactionManager.get(getApplicationContext()).addTransaction(tr);

        updateUI();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
        switch (requestCode){
            case CHOOSE_DB_FILE:
                Uri uri = data.getData();
                String src = Chooser.getPath(this,uri);
                if (src == null){
                    Toast.makeText(this,"File not found!",Toast.LENGTH_SHORT).show();
                    break;
                }
                DatabaseHelper.replaceDb(src);
                updateUI();
                break;
            default:
                Toast.makeText(this,"DEFAULT", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_PERMISSION);
        }
    }
}
