package com.aamovies.admin

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.aamovies.admin.fragment.CategoriesFragment
import com.aamovies.admin.fragment.DashboardFragment
import com.aamovies.admin.fragment.MoviesFragment
import com.aamovies.admin.fragment.SendNotificationFragment
import com.aamovies.admin.fragment.UsersFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE)

        val adminUid = prefs.getString("admin_uid", null)
        if (adminUid == null || auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_admin_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.admin_toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.admin_nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_dashboard, R.string.nav_dashboard
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = navView.getHeaderView(0)
        val tvAdminEmail = headerView.findViewById<TextView>(R.id.tv_admin_email)
        tvAdminEmail.text = auth.currentUser?.email ?: "Admin"

        FirebaseMessaging.getInstance().subscribeToTopic("aamovies_admins")

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            navView.setCheckedItem(R.id.nav_dashboard)
            supportActionBar?.title = "Dashboard"
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> { loadFragment(DashboardFragment()); supportActionBar?.title = "Dashboard" }
            R.id.nav_movies -> { loadFragment(MoviesFragment()); supportActionBar?.title = "Movies" }
            R.id.nav_categories -> { loadFragment(CategoriesFragment()); supportActionBar?.title = "Categories" }
            R.id.nav_users -> { loadFragment(UsersFragment()); supportActionBar?.title = "Users" }
            R.id.nav_send_notification -> { loadFragment(SendNotificationFragment()); supportActionBar?.title = "Send Notification" }
            R.id.nav_logout -> {
                prefs.edit().remove("admin_uid").apply()
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
