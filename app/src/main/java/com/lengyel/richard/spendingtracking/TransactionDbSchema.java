package com.lengyel.richard.spendingtracking;

/**
 * Created by Richard on 2016.06.04..
 */
public class TransactionDbSchema {

    public static final class TransactionTable {
        public static final String NAME = "myTransactions";

        public static final class Cols {
            public static final String TRANSACTION_ID = "uuid";
            public static final String NAME = "name";
            public static final String VALUE = "value";
            public static final String DATE = "date";
            public static final String FOOD = "food";
            public static final String DAY = "day";
            public static final String REGULAR = "regular";
        }
    }
}
