/*
 * **This is not an officially supported Google product.***
 * Copyright 2021 Google LLC. This solution, including any related sample code or data, is made available on an “as is,” “as available,” and “with all faults” basis, solely for illustrative purposes, and without warranty or representation of any kind. This solution is experimental, unsupported and provided solely for your convenience. Your use of it is subject to your agreements with Google, as applicable, and may constitute a beta feature as defined under those agreements. To the extent that you make any data available to Google in connection with your use of the solution, you represent and warrant that you have all necessary and appropriate rights, consents and permissions to permit Google to use and process that data. By using any portion of this solution, you acknowledge, assume and accept all risks, known and unknown, associated with its usage, including with respect to your deployment of any portion of this solution in your systems, or usage in connection with your business, if at all.
 */

package com.google.firebaseengage.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebaseengage.MainActivity
import com.google.firebaseengage.MainActivity.Companion.LOG_TAG

// FCM Demo 2: processing logic
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(LOG_TAG, "From: ${message.from}")

        // Check if message contains a data payload.
        val data = message.data
        if (data.isNotEmpty()) {
            Log.d(LOG_TAG, "Message data payload: $data")
            if (data["redirect"] == "promo") {
                // TODO: show message
//                val intent = Intent(applicationContext, PromoActivity::class.java)
//                intent.flags = FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
            }

        }

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d(LOG_TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(LOG_TAG, "onNewToken: $token")
    }
}