package com.example.parktrace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VehicleDetailActivity : AppCompatActivity() {
    private var phoneNumber = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vehicle_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val imgLogo=findViewById<ImageView>(R.id.imgLogo)
        imgLogo.setOnClickListener {
            val intent=Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val txtDetails = findViewById<TextView>(R.id.txtDetails)
        val btnCall = findViewById<Button>(R.id.btnCall)
        val btnWhatsapp = findViewById<Button>(R.id.btnWhatsapp)

        val qrText = intent.getStringExtra("qrText") ?: ""
      //  txtDetails.text = qrText
        val formatted = qrText.replace(":", " : ")
            .replace("\n", "\n\n")
            .trim()

        txtDetails.text = formatted
        // Extract phone number from QR (assuming you store it as Number:XXXXXXXX)
        phoneNumber = qrText.substringAfter("MobileNo:").trim()
        phoneNumber = phoneNumber.replace("\n", "").replace(" ", "")

        btnCall.setOnClickListener {
            if(phoneNumber.isNotEmpty())
            {
                val callIntent=Intent(Intent.ACTION_DIAL)
                callIntent.data= Uri.parse("tel:$phoneNumber")
                startActivity(callIntent)
            }
        }

        btnWhatsapp.setOnClickListener {
            if(phoneNumber.isNotEmpty()){
                val uri = Uri.parse("https://wa.me/$phoneNumber?text=Your%20vehicle%20is%20parked%20wrongly.")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

    }
}