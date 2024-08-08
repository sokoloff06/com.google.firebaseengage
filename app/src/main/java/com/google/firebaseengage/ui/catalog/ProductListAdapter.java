/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.ui.catalog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebaseengage.R;
import com.google.firebaseengage.data.ProductsRepository;
import com.google.firebaseengage.data.entities.Product;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

/**
 * Created by sokol on 20.02.2017.
 */

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductListAdapterViewHolder> {


    //TODO: sorting by price/category/name
    private ProductsRepository repository;
    private ProductsDisplayer productsDisplayer;
    private SortedList<Product> productSortedListByName;
    private String priceColor;

    //TODO: add categories

    public ProductListAdapter(ProductsRepository repository, ProductsDisplayer productsDisplayer) {
        this.repository = repository;
        this.productsDisplayer = productsDisplayer;
        this.productSortedListByName = new SortedList<>(Product.class, new SortedList.Callback<Product>() {
            @Override
            public void onInserted(int position, int count) {

            }

            @Override
            public void onRemoved(int position, int count) {

            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {

            }

            @Override
            public int compare(Product o1, Product o2) {
                return o1.getName().compareTo(o2.getName());
            }

            @Override
            public void onChanged(int position, int count) {

            }

            @Override
            public boolean areContentsTheSame(Product oldItem, Product newItem) {
                return (oldItem.getName().equals(newItem.getName()) &&
                        oldItem.getId() == newItem.getId() &&
                        oldItem.getPic().equals(newItem.getPic()) &&
                        oldItem.getPrice() == newItem.getPrice()
                );
            }

            @Override
            public boolean areItemsTheSame(Product item1, Product item2) {
                return areContentsTheSame(item1, item2);
            }
        });
    }

    public void loadProducts(String priceColor) {
        this.priceColor = priceColor;
        Log.d("ENGAGE-DEBUG", "Applied " + CatalogFragment.KEY_PRICE_TAG_COLOR + "=" + priceColor + " from Remote Config (for soon-to-happen ViewHolder rendering)");
        List<Product> products = repository.getProducts();
        productSortedListByName.addAll(products);
        notifyDataSetChanged();
    }

    @Override
    public ProductListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductListAdapterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductListAdapterViewHolder holder, int position) {
        GradientDrawable dr = (GradientDrawable) holder.textPrice.getBackground();
        dr.setColor(Color.parseColor(priceColor));
        Product p = productSortedListByName.get(position);
        holder.textView.setText(p.getName());
        holder.textPrice.setText(String.valueOf(p.getPrice()));
        holder.imageView.setImageURI(Uri.parse(p.getPic()));
    }

    @Override
    public int getItemCount() {
        int size = productSortedListByName.size();
        if (size == 0) {
            return 0;
        } else {
            return size;
        }
    }

    public Product getProduct(int position) {
        return productSortedListByName.get(position);
    }

    class ProductListAdapterViewHolder extends RecyclerView.ViewHolder {

        final TextView textView;
        final TextView textPrice;
        final ImageView imageView;

        ProductListAdapterViewHolder(final View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.item_text);
            this.textPrice = itemView.findViewById(R.id.item_price);
            this.imageView = itemView.findViewById(R.id.item_image);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                productsDisplayer.onItemClicked(position);
            });
        }
    }
}
