package com.simats.schememasters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.schememasters.models.GoogleLoginRequest
import com.simats.schememasters.models.GoogleLoginResponse
import com.simats.schememasters.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContinueActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_continue)

        val name = intent.getStringExtra("USER_NAME") ?: "User"
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val googleId = intent.getStringExtra("GOOGLE_ID") ?: ""

        val tvWelcomeText = findViewById<TextView>(R.id.tvWelcomeText)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val tvSwitchAccount = findViewById<TextView>(R.id.tvSwitchAccount)

        tvWelcomeText.text = "Continue as $name"
        tvUserEmail.text = email

        btnContinue.setOnClickListener {
            performGoogleLogin(email, name, googleId)
        }

        tvSwitchAccount.setOnClickListener {
            finish()
        }
    }

    private fun performGoogleLogin(email: String, name: String, googleId: String) {
        val request = GoogleLoginRequest(email, name, googleId)
        
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()

        RetrofitClient.instance.googleLogin(request).enqueue(object : Callback<GoogleLoginResponse> {
            override fun onResponse(call: Call<GoogleLoginResponse>, response: Response<GoogleLoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginRes = response.body()!!
                    if (loginRes.status == "success") {
                        Toast.makeText(this@ContinueActivity, loginRes.message, Toast.LENGTH_SHORT).show()
                        
                        // Fix: handle nullable userId
                        val userId = loginRes.userId ?: -1
                        
                        // Save session for Google Login
                        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("USER_ID", userId)
                            putString("USER_NAME", name)
                            putString("USER_EMAIL", email)
                            apply()
                        }

                        val intent = Intent(this@ContinueActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ContinueActivity, loginRes.message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@ContinueActivity, "Server Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoogleLoginResponse>, t: Throwable) {
                Toast.makeText(this@ContinueActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
