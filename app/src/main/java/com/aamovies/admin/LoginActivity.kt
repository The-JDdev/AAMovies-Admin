package com.aamovies.admin

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    private val allowedAdmins = setOf("jdvijay.me@gmail.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE)

        val savedUid = prefs.getString("admin_uid", null)
        if (savedUid != null && auth.currentUser != null) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_admin_login)

        etEmail = findViewById(R.id.et_admin_email)
        etPassword = findViewById(R.id.et_admin_password)
        btnLogin = findViewById(R.id.btn_admin_login)
        progressBar = findViewById(R.id.admin_progress_bar)

        btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim().lowercase()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        if (!allowedAdmins.contains(email)) {
            Toast.makeText(this, "Access denied. Unauthorized account.", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                prefs.edit().putString("admin_uid", uid).apply()
                goToMain()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, e.message ?: "Login failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
    }
}
