/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.data;

import android.provider.BaseColumns;

/**
 * Created by sokol on 17.02.2017.
 */

class ProductsContract {

    ProductsContract() {

    }

    class ProductsEntries implements BaseColumns {

        static final String TABLE_NAME = "products";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_PRICE = "price";
        static final String COLUMN_PIC = "pic";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " +
                        TABLE_NAME + "(" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT, " +
                        COLUMN_PRICE + " INTEGER, " +
                        COLUMN_PIC + " TEXT);";
        static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " +
                        TABLE_NAME;
    }

    class TimestampEntries implements BaseColumns {

        static final String TABLE_NAME = "last_update";
        static final String COLUMN_TIMESTAMP = "timestamp";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " +
                        TABLE_NAME + "(" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_TIMESTAMP + " TEXT);";

        static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " +
                        TABLE_NAME;

    }

}
