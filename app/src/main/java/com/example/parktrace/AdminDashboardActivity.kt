package com.example.parktrace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnRegistrationReport = findViewById<CardView>(R.id.btnRegistrationReport)
        val btnVehicleReport = findViewById<CardView>(R.id.btnVehicleReport)
        val btnLogout = findViewById<CardView>(R.id.btnAdminLogout)
        val backToLogin=findViewById<TextView>(R.id.backLogin)
        backToLogin.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        // 📄 Registration Report
        btnRegistrationReport.setOnClickListener {
            startActivity(Intent(this, RegistrationReportActivity::class.java))
        }
        //🚗Vehicle Report
        btnVehicleReport.setOnClickListener {
           startActivity(Intent(this, VehicleReportActivity::class.java))
        }
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }
    override fun onBackPressed() {
        // Redirect to LoginActivity
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        // Clear any existing activities so the user cannot go back to dashboard
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}