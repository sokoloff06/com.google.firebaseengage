/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebaseengage.R
import com.google.firebaseengage.ui.MainActivity
import com.google.firebaseengage.ui.MainActivity.Companion.LOG_TAG
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class UtilActivity : AppCompatActivity() {

    private lateinit var btnConversion: Button
    private lateinit var btnGetToken: Button
    private lateinit var btnGetFid: Button
    private lateinit var btnDelToken: Button
    private lateinit var btnWelcome: Button
    private lateinit var btnCrash: Button
    private lateinit var btnConsent: Button
    private lateinit var btnWebView: Button

    companion object {
        val threadPool: ExecutorService = Executors.newCachedThreadPool()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util)
        Log.d(LOG_TAG, "onCreate: $intent")
        btnConversion = findViewById<Button>(R.id.btn_conversion).apply {
            setOnClickListener {
                // Firebase Analytics logging removed
            }
        }
        btnGetToken = findViewById<Button?>(R.id.btn_get_token).apply {
            setOnClickListener {
                getToken()
            }
        }

        btnDelToken = findViewById<Button?>(R.id.btn_del_token).apply {
            setOnClickListener {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                    Log.d(LOG_TAG, "FCM Token deleted")
                }
            }
        }

        btnGetFid = findViewById<Button?>(R.id.btn_get_fid).apply {
            setOnClickListener {
                getFid()
            }
        }
        btnWelcome = findViewById<Button?>(R.id.btn_welcome).apply {
            setOnClickListener {
                // Firebase Analytics logging removed
            }
        }

        btnCrash = findViewById<Button?>(R.id.btn_crash).apply {
            setOnClickListener {
                throw java.lang.RuntimeException("Crashlytics Test")
            }
        }

        btnConsent = findViewById<Button?>(R.id.btn_consent).apply {
            setOnClickListener {
                UserMessagingPlatform.showPrivacyOptionsForm(this@UtilActivity) {
                    Log.d(LOG_TAG, "UMP Consent Form dismissed")
                }
            }
        }

        btnWebView = findViewById<Button?>(R.id.btn_webview).apply {
            setOnClickListener {
                val intent = Intent(this@UtilActivity, com.google.firebaseengage.ui.WebViewActivity::class.java)
                intent.putExtra("EXTRA_URL", "https://gh.visokolov.com/test/webview")
                startActivity(intent)
            }
        }
    }

    private fun getFid() {
        threadPool.submit {
            FirebaseInstallations.getInstance().getToken(false).addOnCompleteListener {
                Log.d(LOG_TAG, "installationAuthToken = ${it.result.token}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseInAppMessaging.getInstance().addImpressionListener(MainActivity.fiamImpressionListener)
        FirebaseInAppMessaging.getInstance().addDismissListener(MainActivity.fiamDismissLister)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(LOG_TAG, "onNewIntent: $intent")
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