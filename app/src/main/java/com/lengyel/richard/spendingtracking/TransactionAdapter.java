package com.lengyel.richard.spendingtracking;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Richard on 2016.06.04..
 */
public class TransactionAdapter  extends RecyclerView.Adapter<TransactionAdapter.TransactionHolder> {
    private List<Transaction> mTransactions;
    private MainActivity mContext;

    public class TransactionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mDateTextView;
        private TextView mNameTextView;
        private TextView mValueTextView;
        private TextView mTransactionCategoryTextView;
        private ImageView mCategoryImageView;

        private Transaction mTransaction;

        public TransactionHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_transaction_name);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_transaction_date);
            mValueTextView = (TextView) itemView.findViewById(R.id.list_item_transaction_value);
            mTransactionCategoryTextView = (TextView) itemView.findViewById(R.id.list_item_category_text);
            mCategoryImageView = (ImageView) itemView.findViewById(R.id.lis_item_category_image);
        }

        public void bindTransaction(Transaction transaction) {
            mTransaction = transaction;
            mNameTextView.setText(mTransaction.getName());
            SimpleDateFormat dateFormat = new SimpleDateFormat(TransactionManager.DATE_FORMAT);
            mDateTextView.setText(dateFormat.format(mTransaction.getDate()));
            mValueTextView.setText(String.valueOf(mTransaction.getValue()));
            mTransactionCategoryTextView.setText(getCategoryText(mTransaction));
            mCategoryImageView.setImageResource(getImageResource(mTransaction));
        }

        @Override
        public void onClick(View v) {
            NewTransactionDialog dialog = NewTransactionDialog.newInstance(mTransaction);
            dialog.show(mContext.getSupportFragmentManager(),"newTransactionDialog");
        }
    }

    public TransactionAdapter(List<Transaction> transactions, MainActivity context) {
        mTransactions = transactions;
        mContext = context;
    }

    @Override
    public TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionHolder holder, int position) {
        Transaction transaction = mTransactions.get(position);
        holder.bindTransaction(transaction);
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        mTransactions = transactions;
    }

    public static String getCategoryText(Transaction transaction){
        if (transaction.isFood())
            return "food";
        else if (transaction.isRegular())
            return "regular";
        else
            return "other";
    }


    public static int getImageResource(Transaction transaction) {
        if (transaction.isFood())
            return R.mipmap.initial_food;
        else if (transaction.isRegular())
            return R.mipmap.initial_regular;
        else
            return R.mipmap.initial_other;
    }

}

