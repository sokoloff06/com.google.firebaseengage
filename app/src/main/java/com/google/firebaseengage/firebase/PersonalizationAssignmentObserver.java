/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.firebase;

import static com.google.firebaseengage.MainActivity.LOG_TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tagmanager.CustomTagProvider;

import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

// Personalization assignment happens when we read value of the personalized property from Remote Config, not when RC is fetched
public class PersonalizationAssignmentObserver implements CustomTagProvider {
    public static CountDownLatch latch = new CountDownLatch(1);
    public static Runnable listener = null;

    @Override
    public void execute(@NonNull Map<String, Object> map) {
        Log.d(LOG_TAG, new JSONObject(map).toString());
        if (Objects.equals(map.get("group"), "P13N")) {
            Log.d(LOG_TAG, "PERSONALIZED_USER");
            if (listener != null) {
                listener.run();
            }
            latch.countDown();
        } else {
            Log.d(LOG_TAG, "BASELINE_USER");
        }
    }
}
