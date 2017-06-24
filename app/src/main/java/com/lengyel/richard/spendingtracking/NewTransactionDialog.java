package com.lengyel.richard.spendingtracking;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Richard on 2016.06.05..
 */
public class NewTransactionDialog  extends DialogFragment {
    public static final String EXTRA_TRANSACTION = "com.lengyel.richard.spendingtracking.tr";
    private static Transaction mTransaction;
    private static TextView mTimePicker;
    private static TextView mDatePicker;

    private static SimpleDateFormat mDateFormat = new SimpleDateFormat(TransactionManager.DISPLAY_DATE_FORMAT);
    private static SimpleDateFormat mTimeFormat = new SimpleDateFormat(TransactionManager.DISPLAY_TIME_FORMAT);


    public static NewTransactionDialog newInstance(Transaction transaction) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_TRANSACTION, transaction);
        NewTransactionDialog fragment = new NewTransactionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransaction = getArguments().getParcelable(EXTRA_TRANSACTION);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_transaction_dialog, null);

        final CheckBox mCheckBoxFood = (CheckBox) view.findViewById(R.id.is_food);
        CheckBox mCheckBoxRegular = (CheckBox) view.findViewById(R.id.is_regular);
        EditText mEditName = (EditText) view.findViewById(R.id.name_edit);
        EditText mEditValue = (EditText) view.findViewById(R.id.value_edit);
        mDatePicker = (TextView) view.findViewById(R.id.date_picker);
        mTimePicker = (TextView) view.findViewById(R.id.time_picker);

        // Setting the values of the dialog
        if (mTransaction != null){
            mDatePicker.setText(mDateFormat.format(mTransaction.getDate()));
            mTimePicker.setText(mTimeFormat.format(mTransaction.getDate()));

            mEditValue.setText(String.valueOf(mTransaction.getValue()));
            mEditName.setText(mTransaction.getName());
            mCheckBoxFood.setChecked(mTransaction.isFood());
            mCheckBoxRegular.setChecked(mTransaction.isRegular());
        }else{
            mDatePicker.setText(mDateFormat.format(new Date()));
            mTimePicker.setText(mTimeFormat.format(new Date()));
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(NewTransactionDialog.this);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(NewTransactionDialog.this);
                    }
                });

        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });


        mTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        mCheckBoxRegular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mCheckBoxFood.setChecked(false);
                }
            }
        });
        return dialogBuilder.create();
    }

    private static void setDate(Calendar calendar) {
        mDatePicker.setText(mDateFormat.format(calendar.getTime()));
    }

    private static void setTime(int hourOfDay, int minute) {
        String time = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
        mTimePicker.setText(time);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.set(year,month,day);
            setDate(cal);
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setTime(hourOfDay,minute);
        }
    }

}
