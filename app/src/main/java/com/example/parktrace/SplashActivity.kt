package com.example.parktrace

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.view.animation.AnimationUtils

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val logo=findViewById<ImageView>(R.id.logoImage)
        val fade= AnimationUtils.loadAnimation(this,R.anim.fade_in)
        val slide= AnimationUtils.loadAnimation(this,R.anim.slide_up)

        logo.startAnimation(fade)
        logo.startAnimation(slide)
       Handler(Looper.getMainLooper()).postDelayed({
           startActivity(Intent(this, LoginActivity::class.java))
           finish()
       },3000)
    }
}