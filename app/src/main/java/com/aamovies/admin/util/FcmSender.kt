package com.aamovies.admin.util

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FcmSender {

    private const val TAG = "FcmSender"
    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
    private const val TOPIC = "/topics/new_movies"

    /**
     * Sends a push notification to all "new_movies" topic subscribers.
     * Must be called from a background thread (or will spawn its own).
     *
     * @param movieTitle  The title of the newly added movie.
     * @param posterUrl   The poster image URL for the notification.
     * @param serverKey   FCM Server Key from Firebase Console (stored in /settings/global).
     */
    fun sendNewMovieNotification(movieTitle: String, posterUrl: String, serverKey: String) {
        Thread {
            try {
                val notification = JSONObject().apply {
                    put("title", "🎬 New: $movieTitle")
                    put("body", "$movieTitle is now available on AAMovies!")
                    if (posterUrl.isNotEmpty()) put("image", posterUrl)
                    put("sound", "default")
                }

                val data = JSONObject().apply {
                    put("title", movieTitle)
                    put("image", posterUrl)
                }

                val payload = JSONObject().apply {
                    put("to", TOPIC)
                    put("notification", notification)
                    put("data", data)
                    put("priority", "high")
                }

                val url = URL(FCM_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "key=$serverKey")
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val writer = OutputStreamWriter(conn.outputStream, Charsets.UTF_8)
                writer.write(payload.toString())
                writer.flush()
                writer.close()

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    Log.d(TAG, "FCM notification sent successfully for: $movieTitle")
                } else {
                    val response = conn.errorStream?.bufferedReader()?.readText() ?: ""
                    Log.e(TAG, "FCM failed [$responseCode]: $response")
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "FCM send error: ${e.message}")
            }
        }.start()
    }
}
