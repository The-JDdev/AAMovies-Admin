package com.aamovies.admin.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AppBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler
) {
    @JavascriptInterface
    fun getPlatform(): String = "android-admin"

    @JavascriptInterface
    fun getAppVersion(): String {
        return try {
            val info = activity.packageManager.getPackageInfo(activity.packageName, 0)
            info.versionName ?: "1.0"
        } catch (e: Exception) { "1.0" }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        handler.post { Toast.makeText(activity, message, Toast.LENGTH_SHORT).show() }
    }

    @JavascriptInterface
    fun copyToClipboard(text: String, label: String) {
        handler.post {
            val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(label, text))
            Toast.makeText(activity, "Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun closeApp() {
        handler.post { activity.finish() }
    }

    @JavascriptInterface
    fun reloadPage() {
        handler.post { webView.reload() }
    }
}
