package com.google.firebaseengage.ui.catalog;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebaseengage.R;
import com.google.firebaseengage.data.entities.Product;

import com.google.firebaseengage.data.entities.CartManager;

/**
 * Activity that displays details for a specific product.
 * Expects a Product object passed via Intent extra EXTRA_PRODUCT.
 */
public class ProductDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Retrieve the product passed from MainActivity or CatalogFragment
        Product product = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);

        if (product != null) {
            populateUi(product);
        } else {
            // Handle cases where the activity was started without a product
            Toast.makeText(this, "Product details not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Populates the UI components with the product details.
     * @param product The product to display.
     */
    private void populateUi(Product product) {
        ImageView imageView = findViewById(R.id.details_image);
        TextView nameTextView = findViewById(R.id.details_name);
        TextView priceTextView = findViewById(R.id.details_price);
        Button addToCartButton = findViewById(R.id.btn_details_add_to_cart);

        // Set image if URI is available
        if (product.getPic() != null && !product.getPic().isEmpty()) {
            imageView.setImageURI(Uri.parse(product.getPic()));
        } else {
            // Fallback to a placeholder icon if the picture path is missing
            imageView.setImageResource(R.drawable.ic_menu);
        }

        nameTextView.setText(product.getName());
        priceTextView.setText(product.getPrice() + "€");

        addToCartButton.setOnClickListener(v -> {
            CartManager.getInstance().add(product);
            Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        });
    }
}
