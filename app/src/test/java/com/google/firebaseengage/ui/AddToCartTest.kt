package com.google.firebaseengage.ui

import android.content.Intent
import android.widget.Button
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebaseengage.R
import com.google.firebaseengage.data.entities.CartManager
import com.google.firebaseengage.data.entities.Product
import com.google.firebaseengage.ui.catalog.ProductDetailsActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(packageName = "com.google.firebaseengage", sdk = [33])
class AddToCartTest {

    @Before
    fun setup() {
        // Clear the cart before each test
        CartManager.getInstance().products.clear()
    }

    @Test
    fun testAddToCartFromProductDetails() {
        val product = Product(1, "Test Product", 10, "test_pic.png")
        val intent = Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), ProductDetailsActivity::class.java).apply {
            putExtra(ProductDetailsActivity.EXTRA_PRODUCT, product)
        }

        val activityController = Robolectric.buildActivity(ProductDetailsActivity::class.java, intent)
        activityController.create().start().resume()
        val activity = activityController.get()

        val addToCartBtn = activity.findViewById<Button>(R.id.btn_details_add_to_cart)
        addToCartBtn.performClick()

        val cartProducts = CartManager.getInstance().products
        assertEquals(1, cartProducts.size)
        assertEquals("Test Product", cartProducts.values.first().name)
        assertEquals(10, CartManager.getInstance().sum)
    }

    @Test
    fun testAddToCartFromCatalogFragment() {
        val product = Product(2, "Another Product", 20, "pic2.png")
        CartManager.getInstance().add(product)
        
        val cartProducts = CartManager.getInstance().products
        assertEquals(1, cartProducts.size)
        assertEquals("Another Product", cartProducts.values.first().name)
        assertEquals(20, CartManager.getInstance().sum)
    }
}
