/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.api;

import android.content.Context;

import com.google.firebaseengage.entities.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;

public class ProductsApiLocal implements ProductsApi {
    private final Context context;
    Timestamp updateTimestamp;

    public ProductsApiLocal(Context applicationContext) {
        this.context = applicationContext;
         updateTimestamp = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Timestamp getLastUpdatedTimestamp() {
        return updateTimestamp;
    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = null;
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            InputStream is = context.getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            products = mapper.readValue(json, new TypeReference<List<Product>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return products;
    }
}
