/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.ui


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.appsflyer.AppsFlyerLib
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDismissListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingImpressionListener
import com.google.firebase.inappmessaging.display.FiamListener
import com.google.firebase.inappmessaging.display.FirebaseInAppMessagingDisplay
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebaseengage.R
import com.google.firebaseengage.data.entities.Cart
import com.google.firebaseengage.firebase.UtilActivity
import com.google.firebaseengage.ui.cart.CartAdapter
import com.google.firebaseengage.ui.cart.CartFragment
import com.google.firebaseengage.ui.cart.CartHandler
import com.google.firebaseengage.ui.catalog.CatalogFragment
import com.iabtcf.decoder.TCString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : AppCompatActivity(), CartHandler {
    private lateinit var navigationView: NavigationView
    private var cart = Cart()
    private var cartAdapter = CartAdapter(this)
    private lateinit var navDrawer: DrawerLayout
    private lateinit var viewPager: ViewPager
    private lateinit var remoteConfig: FirebaseRemoteConfig

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        const val LOG_TAG = "firebaseengage"
        const val FIRST_LAUNCH_KEY = "is_first_launch"
        const val CONSENT_KEY = "consent_values"

        internal val fiamImpressionListener =
            FirebaseInAppMessagingImpressionListener { inAppMessage: InAppMessage? ->
                Log.d(
                    LOG_TAG,
                    "FIAM impression:\n" +
                            "Campaign ID: ${inAppMessage?.campaignMetadata?.campaignId}\n" +
                            "Camp ID: ${inAppMessage?.campaignId}\n" +
                            "Data: ${inAppMessage?.data}\n"
                )
            }
        internal val fiamDismissLister =
            FirebaseInAppMessagingDismissListener { inAppMessage: InAppMessage? ->
                Log.d(
                    LOG_TAG,
                    "FIAM dismiss:\n" +
                            "Campaign ID: ${inAppMessage?.campaignMetadata?.campaignId}\n" +
                            "Camp ID: ${inAppMessage?.campaignId}\n" +
                            "Data: ${inAppMessage?.data}\n"
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Firebase Remote Config
        // RC Demo 1: set up remote config
        setUpRemoteConfig()
        val sp = getSharedPreferences("fruttify_prefs", MODE_PRIVATE)
        if (!sp.contains(FIRST_LAUNCH_KEY)) {
            Log.d(LOG_TAG, "first_launch")
            FirebaseAnalytics.getInstance(this).apply {
                setUserId(UUID.randomUUID().toString())
                setUserProperty("test_property", "true")
//                setUserProperty(FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS, "true")
            }
            sp.edit()
                .putBoolean(FIRST_LAUNCH_KEY, false)
                .apply()
        }
        if (!sp.contains(CONSENT_KEY)) {
//            askUserConsentInHouse(sp)
            askAdMobConsent()
        } else {
            Log.d(LOG_TAG, "$CONSENT_KEY key is present in shared prefs")
        }
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
        askNotificationPermission()
        registerFiamListener()
        initAppsFlyer()
    }


    private fun logScreenView() {
        val eventParams = Bundle()
        eventParams.putString(FirebaseAnalytics.Param.SCREEN_NAME, "AllDeniedTestScreen")
        eventParams.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        FirebaseAnalytics.getInstance(this).logEvent("onResume", eventParams)
    }

    private fun initAppsFlyer() {
        AppsFlyerLib.getInstance().run {
            setDebugLog(true)
            init("LadcyeEpUmDJAMWDYEsZfH", null, this@MainActivity)
            enableTCFDataCollection(true)
        }
    }

    fun setAllConsent(granted: Boolean) {
        if (granted) {
            FirebaseAnalytics.getInstance(this).setConsent(
                mapOf(
                    FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED,
                    FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED,
                    FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.GRANTED,
                    FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.GRANTED,
                )
            )
        } else {
            FirebaseAnalytics.getInstance(this).setConsent(
                mapOf(
                    FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                    FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                    FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.DENIED,
                    FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED,
                )
            )
        }
        FirebaseAnalytics.getInstance(this).logEvent("set_all_consent_api_$granted", null)
    }

    private fun askAdMobConsent() {
        FirebaseAnalytics.getInstance(this).logEvent("consent_unspecified", null)
//        setAllConsent(false)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs.edit().apply {
            putInt("IABTCF_EnableAdvertiserConsentMode", 1)
            commit()
        }
//        monitorConsentString()
        var consentInformation = UserMessagingPlatform.getConsentInformation(this)
        // Set tag for under age of consent. false means users are not under age
        // of consent.
        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            // Success Listener
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                )
                // On consent dismissed listener
                { loadAndShowError ->
                    // Consent gathering failed.
                    if (loadAndShowError != null) {
                        Log.w(
                            LOG_TAG, String.format(
                                "%s: %s",
                                loadAndShowError.errorCode,
                                loadAndShowError.message
                            )
                        )
                    }
                    // Re-enable Firebase and AppsFlyer SDK
                    FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
                    AppsFlyerLib.getInstance().start(this)
                }
            },
            // Fail listener
            { requestConsentError ->
                // Consent gathering failed.
                Log.w(
                    LOG_TAG, String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
            })
    }


    private fun monitorConsentString() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val iabKey = "IABTCF_TCString"
        val readTcString = {
            @Suppress("DEPRECATION")
            lifecycleScope.launchWhenCreated {
                val tcString = withContext(Dispatchers.IO) { sharedPrefs.getString(iabKey, "") }
                Log.d(LOG_TAG, "TCF String: $tcString")
                if (tcString == "") return@launchWhenCreated
                /*
                * Legitimate interest is like opt-out, enabled by default
                * Consent is like opt-in, disabled by default
                */
                val adStorageAllowed = TCString.decode(tcString).purposesConsent.contains(1)
                // Always false for Google
                // val googleAllowed = TCString.decode(tcString).allowedVendors.contains(755)
                // Consent toggle in vendor setting (i.e. Google is not blocked from using consented data at vendor level)
                val googleConsent = TCString.decode(tcString).vendorConsent.contains(755)
                // Legitimate interest toggle in vendor setting (i.e. Google is not blocked from their legitimate interest)
                val googleInterest = TCString.decode(tcString).vendorLegitimateInterest.contains(755)
                // We are checking if consent for ad_storage was given and that Google as vendor has not been excluded from using consented data
                val consentStatus = if (adStorageAllowed && googleConsent)
                    FirebaseAnalytics.ConsentStatus.GRANTED
                else
                    FirebaseAnalytics.ConsentStatus.DENIED
                FirebaseAnalytics.getInstance(applicationContext)
                    .setConsent(
                        mapOf(FirebaseAnalytics.ConsentType.AD_STORAGE to consentStatus)
                    )
            }
        }

        // Init UI with current value
        readTcString()

        // Observe SharedPref changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == iabKey) {
                Log.w(LOG_TAG, "IABTCF_TCString changed")
                readTcString()
            }
        }

        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
            }

            override fun onCreate(owner: LifecycleOwner) {
                suspend {
                    val tcString = withContext(Dispatchers.IO) { sharedPrefs.getString(iabKey, "") }
                    Log.d(LOG_TAG, "TCF String: $tcString")
                }
                super.onCreate(owner)
            }
        })
    }

    private fun registerFiamListener() {
        FirebaseInAppMessagingDisplay.getInstance().setFiamListener(object : FiamListener {
            override fun onFiamTrigger() {
                Log.d(LOG_TAG, "onFiamTrigger")
            }

            override fun onFiamClick() {
                Log.d(LOG_TAG, "onFiamClick")
            }

            override fun onFiamDismiss() {
                Log.d(LOG_TAG, "onFiamDismiss")
            }

        })
    }

    private fun onSwipeUpdate() {
        swipeRefreshLayout.isRefreshing = true
        // RC Demo 3: Fetching Config
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.result) {
                Log.d(LOG_TAG, "Remote Config fetched and active")
            } else {
                Log.w(
                    LOG_TAG, "WARNING: minFetchInterval didn't pass or " + "Config didn't change. Using cached values!"
                )
            }
            onRemoteConfigComplete()
        }.addOnFailureListener { exception ->
            Log.d(
                LOG_TAG, "Remote Control FAILED to be fetched: $exception.localizedMessage"
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
        FirebaseInAppMessaging.getInstance().addImpressionListener(fiamImpressionListener)
        FirebaseInAppMessaging.getInstance().addDismissListener(fiamDismissLister)
        logScreenView()
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

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
//        if (isGranted) {
//            // FCM SDK (and your app) can post notifications.
//        } else {
//            // TODO: Inform user that that your app will not show notifications.
//        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications already.
//            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
//                // TODO: display an educational UI explaining to the user the features that will be enabled
//                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
//                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
//                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
//                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun askUserConsentInHouse(sp: SharedPreferences) {
        // Entries of the map are iterated in the order they were specified.
        val userConsentMap = mutableMapOf(
            FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
            FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
            FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.DENIED,
            FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED,
        )

        val consentMapping = mapOf(
            0 to FirebaseAnalytics.ConsentType.AD_STORAGE,
            1 to FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE,
            2 to FirebaseAnalytics.ConsentType.AD_PERSONALIZATION,
            3 to FirebaseAnalytics.ConsentType.AD_USER_DATA,
        )
        val userOptions = consentMapping.values.map { value -> value.name }.toTypedArray()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setCancelable(false)
            .setTitle("Choose consent signals")
            .setMultiChoiceItems(
                userOptions,
                null
            ) { _, position, isChecked ->
                run {
                    val consentType = consentMapping[position]!!
                    val selectedStatus: FirebaseAnalytics.ConsentStatus = if (isChecked) {
                        FirebaseAnalytics.ConsentStatus.GRANTED
                    } else {
                        FirebaseAnalytics.ConsentStatus.DENIED
                    }
                    userConsentMap[consentType] = selectedStatus
                }
            }
            .setPositiveButton("Confirm") { _, _ ->
                Log.d(LOG_TAG, "Consent choice confirmed")
                FirebaseAnalytics.getInstance(this).setConsent(userConsentMap)
                val stringifiedConsent = ObjectMapper().writeValueAsString(userConsentMap)
                sp.edit()
                    .putString(CONSENT_KEY, stringifiedConsent)
                    .apply()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}