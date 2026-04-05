package com.aamovies.admin.bridge

import com.aamovies.admin.R
import android.content.Intent
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * AdminAuthBridge — Restricted Firebase Auth for the admin panel.
 *
 * Only whitelisted admin emails can successfully authenticate.
 * Google Sign-In result is handled in MainActivity and verified here.
 *
 * HTML calls:  AndroidAuth.adminLogin(email, pass)
 *              AndroidAuth.adminLoginGoogle()
 *              AndroidAuth.adminLogout()
 * Callbacks:   window.onAdminAuthSuccess(userJson)
 *              window.onAdminAuthError(message)
 *              window.onAdminAuthStateChanged(userJson | null)
 */
class AdminAuthBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler,
    private val googleLauncher: ActivityResultLauncher<Intent>
) {
    private val auth = FirebaseAuth.getInstance()

    // ── Whitelist of admin emails ──────────────────────────────────────────────
    private val adminEmails = setOf(
        "jdvijay.me@gmail.com"
        // Add more admin emails here if needed
    )

    fun isAdminEmail(email: String?): Boolean {
        if (email == null) return false
        return adminEmails.any { it.equals(email.trim(), ignoreCase = true) }
    }

    // ── Email / Password Admin Login ───────────────────────────────────────────

    @JavascriptInterface
    fun adminLogin(email: String, password: String) {
        if (!isAdminEmail(email)) {
            callbackError("Access denied. This account is not an admin.")
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user!!
                callbackSuccess(user)
            }
            .addOnFailureListener { e ->
                callbackError(e.message)
            }
    }

    @JavascriptInterface
    fun sendAdminPasswordReset(email: String) {
        if (!isAdminEmail(email)) {
            callbackError("Access denied.")
            return
        }
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { js("window.onAdminPasswordResetSent()") }
            .addOnFailureListener { e -> callbackError(e.message) }
    }

    // ── Google Sign-In (triggers Activity result → verified in MainActivity) ──

    @JavascriptInterface
    fun adminLoginGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        client.signOut().addOnCompleteListener {
            handler.post { googleLauncher.launch(client.signInIntent) }
        }
    }

    // ── Current User / Logout ─────────────────────────────────────────────────

    @JavascriptInterface
    fun getCurrentAdmin(): String {
        val user = auth.currentUser ?: return "null"
        if (!isAdminEmail(user.email)) return "null"
        return buildUserJson(user)
    }

    @JavascriptInterface
    fun isAdminLoggedIn(): Boolean {
        val user = auth.currentUser ?: return false
        return isAdminEmail(user.email)
    }

    @JavascriptInterface
    fun adminLogout() {
        auth.signOut()
        js("window.onAdminLogout()")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun callbackSuccess(user: FirebaseUser) {
        val json = buildUserJson(user)
        js("window.onAdminAuthSuccess($json)")
    }

    fun callbackError(msg: String?) {
        val safeMsg = (msg ?: "Unknown error").escaped()
        js("window.onAdminAuthError('$safeMsg')")
    }

    fun buildUserJson(user: FirebaseUser): String = """{
        "uid":"${user.uid}",
        "email":"${user.email ?: ""}",
        "displayName":"${(user.displayName ?: "Admin").escaped()}",
        "isAnonymous":${user.isAnonymous},
        "emailVerified":${user.isEmailVerified}
    }"""

    private fun js(script: String) {
        handler.post { webView.evaluateJavascript(script, null) }
    }

    private fun String.escaped() = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
