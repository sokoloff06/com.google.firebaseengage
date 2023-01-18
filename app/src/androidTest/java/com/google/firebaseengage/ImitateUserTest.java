/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage;

import static com.google.firebaseengage.MainActivity.LOG_TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebaseengage.cart.CartFragment;
import com.google.firebaseengage.firebase.PersonalizationAssignmentObserver;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class ImitateUserTest {

    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseAnalytics analytics;

    @Before
    public void setup() {
        PersonalizationAssignmentObserver.latch = new CountDownLatch(1);
        PersonalizationAssignmentObserver.listener = () -> logPurchase("");
        analytics = FirebaseAnalytics.getInstance(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void generatePurchasesBasedOnRcValues() throws InterruptedException {
        // Given
        Context ctx = ApplicationProvider.getApplicationContext();
        Log.d(LOG_TAG, "package name = " + ctx.getPackageName());

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(ctx);
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        CountDownLatch rcFetchLatch = new CountDownLatch(1);
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    Log.d("ENGAGE-DEBUG", "Remote Control fetched and active");
                    rcFetchLatch.countDown();
                })
                .addOnFailureListener(exception -> Log.d("ENGAGE-DEBUG",
                        "Remote Control FAILED to be fetched: " + exception.getLocalizedMessage()));

        rcFetchLatch.await(5000, TimeUnit.MILLISECONDS);
        final CountDownLatch appIdLatch = new CountDownLatch(1);
        analytics.getAppInstanceId().addOnCompleteListener(task -> {
            Log.d(LOG_TAG, "App Instance ID = " + task.getResult());
            appIdLatch.countDown();
        });
        String firebaseInstanceId = analytics.getFirebaseInstanceId();
        Log.d(LOG_TAG, "Firebase Instance ID = " + firebaseInstanceId);
        //TODO: create function logIf(param_name, param_values)
        // A/B Testing (bgColor) results logic
//        String bgColor = remoteConfig.getString(CatalogFragment.BG_COLOR_KEY).toUpperCase();
//        Log.d(LOG_TAG, "bgColor = " + bgColor);
//        if (bgColor.equals("#E0F7FA")) {
//            logPurchase(bgColor);
//        } else {
//            double random = Math.random();
//            if (random <= 0.5) {
//                logPurchase(bgColor);
//            }
//        }

        // Personalization (btn_buy_color) results logic
        String btnColor = remoteConfig.getString(CartFragment.KEY_PURCHASE_BTN_COLOR).toUpperCase();
        Log.d(LOG_TAG, CartFragment.KEY_PURCHASE_BTN_COLOR + " = " + btnColor);
        // Log Purchase is sent via listener
        appIdLatch.await(5000, TimeUnit.MILLISECONDS);
        PersonalizationAssignmentObserver.latch.await(5000, TimeUnit.MILLISECONDS);
    }

    private void logPurchase(String bgColor) {
        String btnColor = remoteConfig.getString(CartFragment.KEY_PURCHASE_BTN_COLOR).toUpperCase();
        Bundle eventParams = new Bundle();
        eventParams.putString(FirebaseAnalytics.Param.CURRENCY, "EUR");
        eventParams.putDouble(FirebaseAnalytics.Param.VALUE, 10);
//        eventParams.putString(FirebaseAnalytics.Param.TRANSACTION_ID, analytics.getFirebaseInstanceId());
        eventParams.putString(FirebaseAnalytics.Param.AFFILIATION, bgColor);
        eventParams.putString(FirebaseAnalytics.Param.COUPON, btnColor);
        Log.d(LOG_TAG, "Logging event");
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE, eventParams);
    }
}
