/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.firebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebaseengage.MainActivity.Companion.LOG_TAG
import com.google.firebaseengage.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class UtilActivity : AppCompatActivity() {
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var btnConversion: Button
    private lateinit var btnGetToken: Button
    private lateinit var btnGetFid: Button
    private lateinit var btnWelcome: Button
    private lateinit var btnCrash: Button


    companion object {
        val threadPool: ExecutorService = Executors.newCachedThreadPool()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util)
        Log.d(LOG_TAG, "onCreate: $intent")

        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
        btnConversion = findViewById<Button>(R.id.btn_conversion).apply {
            setOnClickListener {
                // Single item description
                val purchaseItem = Bundle()
                purchaseItem.putString(FirebaseAnalytics.Param.ITEM_ID, "productId")
                purchaseItem.putString(FirebaseAnalytics.Param.ITEM_NAME, "productDisplayName")
                purchaseItem.putString(FirebaseAnalytics.Param.QUANTITY, "2")
                purchaseItem.putDouble(FirebaseAnalytics.Param.PRICE, 5.0)
                purchaseItem.putString(FirebaseAnalytics.Param.ITEM_VARIANT, "color_blue")

                // Adding single item to the array of items
                val purchaseParams = Bundle()
                purchaseParams.putParcelableArray(
                    FirebaseAnalytics.Param.ITEMS,
                    arrayOf(purchaseItem)
                )

                // Whole purchase description
                purchaseParams.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
                purchaseParams.putString(FirebaseAnalytics.Param.AFFILIATION, "UtilActivity")
                purchaseParams.putString(FirebaseAnalytics.Param.TRANSACTION_ID, "T12345")
                purchaseParams.putDouble(FirebaseAnalytics.Param.VALUE, 10.0)
                purchaseParams.putDouble(FirebaseAnalytics.Param.SHIPPING, 5.0)

                val firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, purchaseParams)
            }
        }
        btnGetToken = findViewById<Button?>(R.id.btn_get_token).apply {
            setOnClickListener {
                getToken()
            }
        }

        btnGetFid = findViewById<Button?>(R.id.btn_get_fid).apply {
            setOnClickListener {
                getFid()
            }
        }
        btnWelcome = findViewById<Button?>(R.id.btn_welcome).apply {
            setOnClickListener {
                firebaseAnalytics.logEvent("show_welcome", null)
            }
        }

        btnCrash = findViewById<Button?>(R.id.btn_crash).apply {
            setOnClickListener {
                throw java.lang.RuntimeException("Crashlytics Test")
            }
        }

        askNotificationPermission()
    }

    private fun getFid() {
        threadPool.submit {
            // Same as FIAM Installation ID
            firebaseAnalytics.firebaseInstanceId.also {
                Log.d(LOG_TAG, "firebaseInstanceId = $it")
            }

            firebaseAnalytics.appInstanceId.addOnCompleteListener {
                Log.d(LOG_TAG, "appInstanceId = ${it.result}")
            }

            firebaseAnalytics.sessionId.addOnCompleteListener {
                Log.d(LOG_TAG, "sessionId = ${it.result}")
            }
            FirebaseInstallations.getInstance().getToken(false).addOnCompleteListener {
                Log.d(LOG_TAG, "installationAuthToken = ${it.result.token}")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(LOG_TAG, "onNewIntent: $intent")
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
//                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // RC Demo 4: FCM Token
    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(LOG_TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(LOG_TAG, "FCM Token: $token")
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })
    }
}