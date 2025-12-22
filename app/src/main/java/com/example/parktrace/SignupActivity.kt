package com.example.parktrace

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.example.parktrace.databinding.ActivitySignupBinding
import com.example.parktrace.viewmodel.SignupViewModel
import androidx.lifecycle.ViewModelProvider
class SignupActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignupBinding
    private lateinit var viewModel: SignupViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        // Navigate back to login
        binding.loginBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Observe signup result
        viewModel.signupResult.observe(this) {success ->
            if (success) {
                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
        binding.btnRegister.setOnClickListener {
            val owner = binding.idOwenerName.text.toString().trim()
            val mobile = binding.idInputMobile.text.toString().trim()
            val address = binding.idInputAddress.text.toString().trim()
            val email = binding.idInputMail.text.toString().trim().replace(" ", "")
            val password = binding.idInputPassword.text.toString().trim()

            // Validate form via ViewModel
            val formState = viewModel.validateForm(owner, email, mobile, address, password)
            if (!formState.isValid) {
                Toast.makeText(this, formState.errorMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signup(owner, email, mobile, address, password)
        }

    }
}