package com.simats.schememasters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.simats.schememasters.models.ProfileResponse
import com.simats.schememasters.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvWelcomeUser: TextView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)
        val savedName = sharedPref.getString("USER_NAME", "User")
        val savedEmail = sharedPref.getString("USER_EMAIL", "user@email.com")

        drawerLayout = findViewById(R.id.drawerLayout)
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        tvWelcomeUser.text = "Hello, $savedName! 👋"

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnNotification = findViewById<ImageView>(R.id.btnNotification)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        // Set initial header data from session
        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvHeaderName).text = savedName
        headerView.findViewById<TextView>(R.id.tvHeaderEmail).text = savedEmail
        headerView.findViewById<TextView>(R.id.tvHeaderInitial).text = savedName?.take(1)?.uppercase() ?: "U"

        if (userId != -1) {
            loadUserData(navigationView)
        }

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_all_schemes -> startActivity(Intent(this, AllSchemesActivity::class.java))
                R.id.nav_my_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_notifications -> startActivity(Intent(this, NotificationsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_help_support -> startActivity(Intent(this, HelpActivity::class.java))
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        findViewById<MaterialCardView>(R.id.cardStudent).setOnClickListener {
            startActivity(Intent(this, StudentSchemesActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardFarmer).setOnClickListener {
            startActivity(Intent(this, FarmerSchemesActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardPopularPostMatric).setOnClickListener {
            startActivity(Intent(this, PostMatricDetailsActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardPopularPMKisan).setOnClickListener {
            startActivity(Intent(this, PMKisanDetailsActivity::class.java))
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_schemes -> {
                    startActivity(Intent(this, AllSchemesActivity::class.java))
                    false
                }
                R.id.nav_upload -> {
                    startActivity(Intent(this, UploadDocsActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun loadUserData(navigationView: NavigationView) {
        RetrofitClient.instance.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val user = response.body()?.data
                    user?.let {
                        tvWelcomeUser.text = "Hello, ${it.name}! 👋"
                        val headerView = navigationView.getHeaderView(0)
                        headerView.findViewById<TextView>(R.id.tvHeaderName).text = it.name
                        headerView.findViewById<TextView>(R.id.tvHeaderEmail).text = it.email
                        headerView.findViewById<TextView>(R.id.tvHeaderInitial).text = it.name.take(1).uppercase()
                        
                        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        sharedPref.edit()
                            .putString("USER_NAME", it.name)
                            .putString("USER_EMAIL", it.email)
                            .apply()
                    }
                }
            }
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {}
        })
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home
        if (userId != -1) {
            val navView = findViewById<NavigationView>(R.id.navigationView)
            loadUserData(navView)
        }
    }
}
