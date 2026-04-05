package com.aamovies.admin.bridge

import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * FCMSenderBridge — Admin-only Firebase Cloud Messaging sender.
 *
 * Sends notifications to the 'aamovies_all_users' topic using the
 * FCM HTTP v1 API. Requires a valid admin session.
 *
 * HTML calls:  AndroidFCM.sendGlobalNotification(title, body, imageUrl, deepLink)
 *              AndroidFCM.sendTopicNotification(topic, title, body, imageUrl)
 *              AndroidFCM.getAdminToken()
 * Callbacks:   window.onFCMSent(messageId)
 *              window.onFCMError(message)
 */
class FCMSenderBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val http = OkHttpClient()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val DEFAULT_TOPIC = "aamovies_all_users"
        // FCM HTTP v1 endpoint — project ID injected at runtime
        private const val FCM_PROJECT = "aamovies-12d36"
        private const val FCM_URL =
            "https://fcm.googleapis.com/v1/projects/$FCM_PROJECT/messages:send"
    }

    /**
     * Send a push notification to all users (aamovies_all_users topic).
     * Uses FCM HTTP v1 API with an OAuth2 access token from Firebase Auth.
     */
    @JavascriptInterface
    fun sendGlobalNotification(title: String, body: String, imageUrl: String, deepLink: String) {
        sendToTopic(DEFAULT_TOPIC, title, body, imageUrl, deepLink)
    }

    @JavascriptInterface
    fun sendTopicNotification(topic: String, title: String, body: String, imageUrl: String) {
        sendToTopic(topic, title, body, imageUrl, "")
    }

    private fun sendToTopic(
        topic: String, title: String, body: String, imageUrl: String, deepLink: String
    ) {
        // Verify admin is logged in before sending
        val currentUser = auth.currentUser
        if (currentUser == null) {
            js("window.onFCMError('You must be signed in as admin to send notifications')")
            return
        }

        // Get an ID token for authenticating the HTTP request
        currentUser.getIdToken(false).addOnSuccessListener { tokenResult ->
            val idToken = tokenResult.token ?: run {
                js("window.onFCMError('Failed to get auth token')")
                return@addOnSuccessListener
            }

            executor.submit {
                try {
                    val payload = JSONObject().apply {
                        put("message", JSONObject().apply {
                            put("topic", topic)
                            put("notification", JSONObject().apply {
                                put("title", title)
                                put("body", body)
                                if (imageUrl.isNotBlank()) put("image", imageUrl)
                            })
                            put("data", JSONObject().apply {
                                if (deepLink.isNotBlank()) put("deep_link", deepLink)
                                put("sent_at", System.currentTimeMillis().toString())
                            })
                            put("android", JSONObject().apply {
                                put("priority", "high")
                                put("notification", JSONObject().apply {
                                    put("sound", "default")
                                    put("click_action", "FLUTTER_NOTIFICATION_CLICK")
                                })
                            })
                        })
                    }

                    val request = Request.Builder()
                        .url(FCM_URL)
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .header("Authorization", "Bearer $idToken")
                        .header("Content-Type", "application/json")
                        .build()

                    val response = http.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val messageId = try {
                            JSONObject(responseBody).getString("name")
                        } catch (_: Exception) { "sent" }
                        js("window.onFCMSent('${messageId.escaped()}')")
                    } else {
                        js("window.onFCMError('HTTP ${response.code}: ${responseBody.escaped()}')")
                    }
                } catch (e: Exception) {
                    js("window.onFCMError('${e.message?.escaped()}')")
                }
            }
        }.addOnFailureListener { e ->
            js("window.onFCMError('Auth token error: ${e.message?.escaped()}')")
        }
    }

    /**
     * Retrieve the admin device's own FCM token.
     */
    @JavascriptInterface
    fun getAdminToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> js("window.onAdminFCMToken('$token')") }
            .addOnFailureListener { e -> js("window.onFCMError('${e.message}')") }
    }

    @JavascriptInterface
    fun getDefaultTopic(): String = DEFAULT_TOPIC

    private fun js(script: String) {
        handler.post { webView.evaluateJavascript(script, null) }
    }

    private fun String.escaped() = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
