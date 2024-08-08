/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebaseengage.data.ProductsContract.ProductsEntries;
import com.google.firebaseengage.data.ProductsContract.TimestampEntries;
import com.google.firebaseengage.data.entities.Product;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by sokol on 17.02.2017.
 */

public class ProductsRepositoryImpl implements ProductsRepository {

    //TODO: open Database HERE

    private static final String TAG = "Products Repository";
    private SQLiteDatabase db;

    public ProductsRepositoryImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public Timestamp getTimestamp() {
        Cursor timestampCursor = null;
        try {
            timestampCursor = db.query(
                    TimestampEntries.TABLE_NAME,
                    new String[]{TimestampEntries.COLUMN_TIMESTAMP},
                    null,
                    null,
                    null,
                    null,
                    null,
                    "1"
            );
            if (!timestampCursor.moveToFirst()) {
                return null;
            }
            int timestampCursorIndex = timestampCursor.getColumnIndexOrThrow(TimestampEntries.COLUMN_TIMESTAMP);
            return Timestamp.valueOf(timestampCursor.getString(timestampCursorIndex));
        } finally {
            if (timestampCursor != null) {
                timestampCursor.close();
            }
        }
    }

    private void setTimestamp(Timestamp timestamp) {
        ContentValues values = new ContentValues();
        values.put(TimestampEntries.COLUMN_TIMESTAMP, timestamp.toString());
        values.put(TimestampEntries._ID, 1);
        db.beginTransaction();
        try {
            db.delete(
                    TimestampEntries.TABLE_NAME,
                    null,
                    null);
            System.out.println("TIMESTAMP ROW ID EDITED: " +
                    db.insert(
                            TimestampEntries.TABLE_NAME,
                            null,
                            values
                    )
            );
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        Cursor productsCursor = db.query(
                ProductsEntries.TABLE_NAME,
                new String[]{ProductsEntries._ID, ProductsEntries.COLUMN_NAME, ProductsEntries.COLUMN_PRICE, ProductsEntries.COLUMN_PIC},
                null,
                null,
                null,
                null,
                null);
        if (!productsCursor.moveToFirst()) {
            productsCursor.close();
            return null;
        }
        int productsIdCursorIndex = productsCursor.getColumnIndex(ProductsEntries._ID);
        int productsNameCursorIndex = productsCursor.getColumnIndexOrThrow(ProductsEntries.COLUMN_NAME);
        int productsPriceCursorIndex = productsCursor.getColumnIndexOrThrow(ProductsEntries.COLUMN_PRICE);
        int productsPicCursorIndex = productsCursor.getColumnIndexOrThrow(ProductsEntries.COLUMN_PIC);
        while (!productsCursor.isAfterLast()) {
            products.add(
                    new Product(
                            productsCursor.getInt(productsIdCursorIndex),
                            productsCursor.getString(productsNameCursorIndex),
                            productsCursor.getInt(productsPriceCursorIndex),
                            productsCursor.getString(productsPicCursorIndex)
                    )
            );
            productsCursor.moveToNext();
        }
        productsCursor.close();
        return products;
    }

    @Override
    public void update(List<Product> products, Timestamp currentTimestamp) {

        Log.i(TAG, "Starting transaction");
        db.beginTransaction();
        try {
            db.delete(
                    ProductsEntries.TABLE_NAME,
                    null,
                    null
            );
            ContentValues row = new ContentValues();
            for (Product p : products) {
                row.put(ProductsEntries.COLUMN_NAME, p.getName());
                row.put(ProductsEntries.COLUMN_PRICE, p.getPrice());
                row.put(ProductsEntries.COLUMN_PIC, p.getPic());
                db.insert(
                        ProductsEntries.TABLE_NAME,
                        null,
                        row
                );
            }
            setTimestamp(currentTimestamp);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i(TAG, "Transaction has ended");
        }
    }
}
