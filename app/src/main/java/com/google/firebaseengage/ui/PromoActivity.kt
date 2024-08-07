/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

@file:JvmName("PromoActivity")
package com.google.firebaseengage.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebaseengage.R

class PromoActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var btnConversion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promo)
        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
        btnConversion = findViewById<Button>(R.id.btn_conversion).apply {
            setOnClickListener {
                // Report purchase using ParamBuilder from ktx
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
                    param(FirebaseAnalytics.Param.CURRENCY, "USD")
                    param(FirebaseAnalytics.Param.AFFILIATION, "PromoActivity")
                    param(FirebaseAnalytics.Param.SHIPPING, 0.0)
                    param(FirebaseAnalytics.Param.VALUE, 10.0)
                    param(FirebaseAnalytics.Param.TRANSACTION_ID, "T12345")
                }
            }
        }
    }
}