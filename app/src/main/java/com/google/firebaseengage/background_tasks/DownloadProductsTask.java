/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.background_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebaseengage.api.ProductsApi;
import com.google.firebaseengage.data.ProductsRepository;
import com.google.firebaseengage.data.entities.Product;
import com.google.firebaseengage.ui.catalog.ProductsDisplayer;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by sokol on 08.03.2017.
 *
 *
 * Data model:
 * [
 *   {
 *     "id": 1,
 *     "name": "Apples",
 *     "price": 100,
 *     "pic": "product_images/1"
 *   },
 *   {
 *     "id": 1,
 *     "name": "Bananas",
 *     "price": 100,
 *     "pic": "product_images/1"
 *   }
 * ]
 */
public class DownloadProductsTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "DownloadProductsTask";

    private final ProductsApi productsApi;
    private final ProductsRepository repository;
    private final String filesDir;
    private final ProductsDisplayer productsDisplayer;
    private final Context context;

    public DownloadProductsTask(Context context, ProductsApi productsApi, ProductsRepository repository, String filesDir, ProductsDisplayer productsDisplayer) {
        this.context = context;
        this.productsApi = productsApi;
        this.repository = repository;
        this.filesDir = filesDir;
        this.productsDisplayer = productsDisplayer;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.i(TAG, "Timestamp is out of date");
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Log.i(TAG, "Transferring task to Repository");
        List<Product> products = productsApi.getProducts();
        if (products == null) {
            return false;
        }
        if (!savePics(products)) {
            return false;
        }
        System.out.println("Updating DB...");
        repository.update(products, currentTimestamp);
        return true;
    }

    private boolean savePics(List<Product> products) {
        for (Product p : products) {
            try {
                //Output
                String filename = p.getName() + ".png";
                String filepath = filesDir + "/" + filename;
                FileOutputStream output = new FileOutputStream(filepath);

                //Input
                InputStream input = context.getAssets().open(p.getId() + ".png");

                //Buffer
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                Log.i(TAG, filename + " is downloaded");

                //Closing references
                p.setPic(filepath);
                System.out.println(p.getPic());
                input.close();
                output.close();

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        System.out.println("Pictures successfully saved");
        return true;
    }

    @Override
    protected void onPostExecute(Boolean IsLoadSucessful) {
        if (IsLoadSucessful) {
            productsDisplayer.load();
        } else {
            productsDisplayer.loadError();
        }
    }
}
