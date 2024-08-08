/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.google.firebaseengage.ui.MainActivity.Companion.LOG_TAG
import com.google.firebaseengage.databinding.ActivityAdsBinding
import java.util.concurrent.atomic.AtomicBoolean

class AdsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdsBinding
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAdMobConsentAndLoadAd()
    }

    private fun checkAdMobConsentAndLoadAd() {
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        if (consentInformation.canRequestAds()) {
            Log.d(LOG_TAG, "consentInfo: user choice was collected")
//            var consentStatus = consentInformation.consentStatus
            initializeMobileAdsSdk()
            loadAd()
        } else {
            Log.d(LOG_TAG, "consentInfo: user choice was not collected")
//            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, null)
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(this)
    }

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()
        //Test ad Unit: ca-app-pub-3940256099942544/1033173712
        // My Ad Unit: ca-app-pub-6765186714303261/6581109817
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError.toString().let { Log.d(LOG_TAG, it) }
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(LOG_TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
                mInterstitialAd!!.show(this@AdsActivity)
            }
        })
    }

}
