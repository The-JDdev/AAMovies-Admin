package com.aamovies.admin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.aamovies.admin.bridge.AdminAuthBridge
import com.aamovies.admin.bridge.AppBridge
import com.aamovies.admin.bridge.FCMSenderBridge
import com.aamovies.admin.services.MyFirebaseMessagingService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView
    val mainHandler = Handler(Looper.getMainLooper())
    private var lastBackPressTime: Long = 0

    private lateinit var adminAuthBridge: AdminAuthBridge
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        MyFirebaseMessagingService.subscribeIfNeeded(this)

        // Register Google Sign-In result handler
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user!!
                            // Verify admin access
                            if (adminAuthBridge.isAdminEmail(user.email)) {
                                val json = buildUserJson(user)
                                evaluateJs("window.onAdminAuthSuccess($json)")
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                evaluateJs("window.onAdminAuthError('Access denied. Admin accounts only.')")
                            }
                        }
                        .addOnFailureListener { e ->
                            evaluateJs("window.onAdminAuthError('${escape(e.message)}')")
                        }
                } catch (e: ApiException) {
                    evaluateJs("window.onAdminAuthError('Google sign-in failed: ${e.statusCode}')")
                }
            }
        }

        // Instantiate bridges
        adminAuthBridge = AdminAuthBridge(this, webView, mainHandler, googleSignInLauncher)
        val fcmSender = FCMSenderBridge(this, webView, mainHandler)
        val appBridge = AppBridge(this, webView, mainHandler)

        setupWebView(adminAuthBridge, fcmSender, appBridge)
        loadApp()

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null && adminAuthBridge.isAdminEmail(user.email)) {
                val json = buildUserJson(user)
                evaluateJs("window.onAdminAuthStateChanged($json)")
            } else {
                evaluateJs("window.onAdminAuthStateChanged(null)")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(vararg bridges: Any) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.addJavascriptInterface(bridges[0], "AndroidAuth")      // AdminAuthBridge
        webView.addJavascriptInterface(bridges[1], "AndroidFCM")       // FCMSenderBridge
        webView.addJavascriptInterface(bridges[2], "AndroidApp")       // AppBridge

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                return if (url.startsWith("file://") || url.startsWith("about:")) false
                else { view.loadUrl(url); true }
            }
        }

        webView.webChromeClient = WebChromeClient()
    }

    private fun loadApp() {
        if (isOnline()) webView.loadUrl("file:///android_asset/index.html")
        else Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
    }

    fun evaluateJs(script: String) {
        mainHandler.post { webView.evaluateJavascript(script, null) }
    }

    private fun buildUserJson(user: com.google.firebase.auth.FirebaseUser): String {
        return """{
            "uid":"${user.uid}",
            "email":"${user.email ?: ""}",
            "displayName":"${escape(user.displayName)}",
            "isAnonymous":${user.isAnonymous}
        }"""
    }

    private fun escape(s: String?): String =
        (s ?: "").replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) { webView.goBack(); return true }
            val now = System.currentTimeMillis()
            return if (now - lastBackPressTime < 2000) { finish(); true }
            else { lastBackPressTime = now; Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show(); true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() { super.onResume(); webView.onResume() }
    override fun onPause() { super.onPause(); webView.onPause() }
    override fun onDestroy() { webView.destroy(); super.onDestroy() }
}
