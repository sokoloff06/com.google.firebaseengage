/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage;

import com.google.firebaseengage.cart.CartFragment;
import com.google.firebaseengage.catalog.CatalogFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by sokol on 07.03.2017.
 */

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private static final CharSequence CART_TITLE = "CART";
    private static final CharSequence CATALOG_TITLE = "CATALOG";

    private final CartFragment cart = new CartFragment();
    private final CatalogFragment catalog = new CatalogFragment();

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 1) {
            return CART_TITLE;
        }
        return CATALOG_TITLE;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return cart;
        }
        return catalog;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
