/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebaseengage.cart.CartAdapter
import com.google.firebaseengage.cart.CartFragment
import com.google.firebaseengage.cart.CartHandler
import com.google.firebaseengage.catalog.CatalogFragment
import com.google.firebaseengage.entities.Cart
import com.google.firebaseengage.firebase.UtilActivity
import java.util.*

class MainActivity : AppCompatActivity(), CartHandler {
    private lateinit var navigationView: NavigationView
    private var cart = Cart()
    private var cartAdapter = CartAdapter(this)
    private lateinit var navDrawer: DrawerLayout
    private lateinit var viewPager: ViewPager
    private lateinit var remoteConfig: FirebaseRemoteConfig

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Firebase Remote Config
        // RC Demo 1: set up remote config
        setUpRemoteConfig()
        FirebaseAnalytics.getInstance(this).setUserId(UUID.randomUUID().toString());
        navDrawer = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        swipeRefreshLayout.setOnRefreshListener { onSwipeUpdate() }
        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            navDrawer.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_promo -> {
                    val i = Intent(applicationContext, PromoActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_util -> {
                    val i = Intent(applicationContext, UtilActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_main -> {
                    val i = Intent(applicationContext, MainActivity::class.java)
                    startActivity(i)
                }
                R.id.nav_ads -> {
                    val i = Intent(applicationContext, AdsActivity::class.java)
                    startActivity(i)
                }
                else -> {}
            }
            true
        }
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        val pagerAdapter = MainPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        /**
         * The [ViewPager] that will host the section contents.
         */
        viewPager = findViewById(R.id.container)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        swipeRefreshLayout.setOnRefreshListener { this.onSwipeUpdate() }
        viewPager.adapter = pagerAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tabs_selector)
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun onSwipeUpdate() {
        swipeRefreshLayout.isRefreshing = true
        // RC Demo 3: Fetching Config
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.result) {
                Log.d("ENGAGE-DEBUG", "Remote Config fetched and active")
            } else {
                Log.w(
                    "ENGAGE-DEBUG", "WARNING: minFetchInterval didn't pass or " + "Config didn't change. Using cached values!"
                )
            }
            onRemoteConfigComplete()
        }.addOnFailureListener { exception ->
            Log.d(
                "ENGAGE-DEBUG", "Remote Control FAILED to be fetched: $exception.localizedMessage"
            )
            onRemoteConfigComplete()
        }
    }

    private fun onRemoteConfigComplete() {
        ((viewPager.adapter as MainPagerAdapter).getItem(0) as CatalogFragment).onSwipeUpdate()
        ((viewPager.adapter as MainPagerAdapter).getItem(1) as CartFragment).onSwipeUpdate()
        swipeRefreshLayout.isRefreshing = false
    }


    private fun setUpRemoteConfig() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        // Use only for development. See https://firebase.google.com/docs/remote-config/get-started?platform=android#throttling
        val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(5).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        // Optionally set defaults in code
//        remoteConfig.setDefaultsAsync(
//            mapOf(
//                KEY_PRICE_TAG_COLOR to "#2D3A4A",
//                KEY_BG_COLOR to "#FFFFFF",
//                KEY_PURCHASE_BTN_COLOR to "#FFFFFF"
//            )
//        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navDrawer.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(LOG_TAG, "Setting new intent: $intent \nReplacing old: ${getIntent()}")
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        navigationView.setCheckedItem(R.id.nav_main)
        Log.d(LOG_TAG, "onResume: $intent")
        intent.extras?.let {
            if (intent.extras!!.getString("redirect") == "promo" && !intent.hasExtra("consumed")) {
                intent.putExtra("consumed", true)
                val intent = Intent(applicationContext, PromoActivity::class.java)
                startActivity(intent)
            }
            /*    val keys = intent.extras!!.keySet()
                for (key in keys) {
                    intent.extras!!.getString(key).let {
                        Log.d(LOG_TAG, "$key value: $it")
                    }
                }*/
        }
    }

    override fun getCart(): Cart {
        return cart
    }

    override fun onDataHasChanged() {
        val sumTextView = findViewById<TextView>(R.id.sum_text_view)
        sumTextView.text = cart.sum.toString() + "€"
    }

    override fun getCartAdapter(): CartAdapter {
        return cartAdapter
    }

    companion object {
        const val LOG_TAG = "ENGAGE-DEBUG"
    }
}