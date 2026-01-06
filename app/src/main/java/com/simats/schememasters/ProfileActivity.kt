package com.simats.schememasters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.schememasters.models.ProfileResponse
import com.simats.schememasters.models.RegisterResponse
import com.simats.schememasters.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Retrieve saved user details from session
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)
        val savedName = sharedPref.getString("USER_NAME", "User")
        val savedEmail = sharedPref.getString("USER_EMAIL", "user@email.com")

        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)

        // Set initial data from session immediately
        tvName.text = "Hello, $savedName"
        tvEmail.text = savedEmail

        // Header Back Button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Edit Profile Button
        findViewById<MaterialButton>(R.id.btnEditProfileSmall).setOnClickListener {
            showEditProfileDialog()
        }

        // Settings section
        findViewById<LinearLayout>(R.id.btnSettings).setOnClickListener {
            showEditProfileDialog()
        }

        // Menu Click Listeners
        findViewById<LinearLayout>(R.id.btnMyApplications).setOnClickListener {
            startActivity(Intent(this, AllSchemesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnUploadedDocuments).setOnClickListener {
            startActivity(Intent(this, UploadDocsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnHelpSupport).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Logout
        findViewById<LinearLayout>(R.id.btnLogout).setOnClickListener {
            logout()
        }

        // Initial fetch for latest data from server
        if (userId != -1) {
            loadProfileData()
        } else {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            logout()
        }
    }

    private fun loadProfileData() {
        RetrofitClient.instance.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val user = response.body()?.data
                    user?.let {
                        tvName.text = "Hello, ${it.name}"
                        tvEmail.text = it.email
                        
                        // Update session with latest data
                        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("USER_NAME", it.name).putString("USER_EMAIL", it.email).apply()
                    }
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                // If offline, we keep showing the cached SharedPreferences data
            }
        })
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etEditPhone)
        val etCaste = dialogView.findViewById<EditText>(R.id.etEditCaste)

        etName.setText(tvName.text.toString().replace("Hello, ", ""))

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()
                val newCaste = etCaste.text.toString().trim()
                
                if (newName.isNotEmpty()) {
                    updateProfileOnServer(newName, newPhone, newCaste)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfileOnServer(name: String, phone: String, caste: String) {
        RetrofitClient.instance.updateProfile(userId, name, phone, caste).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@ProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                    val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("USER_NAME", name).apply()
                    loadProfileData()
                } else {
                    Toast.makeText(this@ProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
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
}
