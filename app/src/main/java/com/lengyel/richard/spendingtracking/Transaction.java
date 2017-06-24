package com.lengyel.richard.spendingtracking;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Richard on 2016.06.04..
 */
public class Transaction  implements Parcelable {

    private ParcelUuid mId;
    private String mName;
    private double mValue;
    private String mDay; // in SQLite, it would be hard to group on a day, so I'll group on this
    private Date mDate;
    private boolean mIsFood;
    private boolean mIsRegular;

    public Transaction() {
        this(ParcelUuid.fromString(UUID.randomUUID().toString()));
    }

    public Transaction(ParcelUuid id) {
        mId = id;
        mDate = new Date();
        setDay(mDate);
    }

    public Date getDate() {
        return mDate;
    }

    public ParcelUuid getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public double getValue() {
        return mValue;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setValue(double value) {
        mValue = value;
    }

    public void setDate(Date date) {

        mDate = date;
    }

    public void setIsFood(boolean isFood){
        mIsFood = isFood;
    }

    public void setIsRegular(boolean isRegular){
        mIsRegular = isRegular;
    }

    public boolean isFood() {
        return mIsFood;
    }
    public boolean isRegular() {
        return mIsRegular;
    }

    public String getDay() {
        return mDay;
    }

    public void setDay(Date day) {
        SimpleDateFormat dayFormat = new SimpleDateFormat(TransactionManager.DAY_FORMAT);
        mDay = dayFormat.format(day);
    }

    public void setDay(String day) {
        mDay = day;
    }

    public static final Parcelable.Creator<Transaction> CREATOR = new Creator<Transaction>() {
        public Transaction createFromParcel(Parcel source) {
            return new Transaction(source);
        }
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    public Transaction(Parcel source) {
        mId = source.readParcelable(ParcelUuid.class.getClassLoader());
        mValue = source.readDouble();
        mName = source.readString();
        mDay = source.readString();
        mDate = new Date(source.readLong());
        mIsFood = source.readInt() != 0;
        mIsRegular = source.readInt() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mId, flags);
        dest.writeString(mName);
        dest.writeString(mDay);
        dest.writeDouble(mValue);
        dest.writeLong(mDate.getTime());
        dest.writeInt(mIsFood ? 1 : 0);
        dest.writeInt(mIsRegular ? 1 : 0);
    }

}
