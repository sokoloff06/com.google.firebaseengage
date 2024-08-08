/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.background_tasks;

import android.os.AsyncTask;

import com.google.firebaseengage.api.ProductsApi;
import com.google.firebaseengage.ui.catalog.ProductsDisplayer;

import java.sql.Timestamp;

/**
 * Created by sokol on 08.03.2017.
 */
public class CheckLastUpdateTask extends AsyncTask<Void, Void, Boolean> {

    private final Timestamp localTimestamp;
    private final ProductsDisplayer productsDisplayer;
    private final ProductsApi productsApi;

    public CheckLastUpdateTask(ProductsApi productsApi, Timestamp localTimestamp, ProductsDisplayer productsDisplayer) {
        this.productsApi = productsApi;
        this.localTimestamp = localTimestamp;
        this.productsDisplayer = productsDisplayer;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        System.out.println("Checking local timestamp");
        if (localTimestamp == null) {
            return true;
        }
        System.out.println("Getting timestamp from server");
        Timestamp serverTimestamp = productsApi.getLastUpdatedTimestamp();
        return localTimestamp.before(serverTimestamp);
    }

    @Override
    protected void onPostExecute(Boolean isOutOfDate) {
        productsDisplayer.loadOrDownload(isOutOfDate);
    }
}
