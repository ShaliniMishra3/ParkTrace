package com.example.parktrace

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.parktrace.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

   // private val ADMIN_EMAIL = "admin@parktrace.com"
   // private val ADMIN_PASSWORD = "admin@123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val currentUser = FirebaseAuth.getInstance().currentUser
        /*
        if (currentUser != null) {
            // User already logged in → go to Main
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

         */

        if (currentUser != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->

                    val role = document.getString("role")

                    if (role == "admin") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val tvRegister=findViewById<TextView>(R.id.tvLogin)

        val fullText=getString(R.string.text_register)
        val clickableText="Register here"

        val startIndex=fullText.indexOf(clickableText)
        val endIndex=startIndex+clickableText.length

        val spannable= SpannableString(fullText)

        val clickableSpan=object : ClickableSpan(){
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color= ContextCompat.getColor(this@LoginActivity,R.color.red)
                ds.isUnderlineText=false
            }
        }
        spannable.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvRegister.text=spannable
        tvRegister.movementMethod= LinkMovementMethod.getInstance()
        //Forgot Password screen Navigation
        val forgetScreen=findViewById<TextView>(R.id.forgotPsw)
        forgetScreen.setOnClickListener {
            val intent= Intent(this, PasswordActivity::class.java)
            startActivity(intent)

        }
        val inputBox = findViewById<EditText>(R.id.idInputBox)
        val passwordInput = findViewById<EditText>(R.id.idPswInputBox)
        passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eye_off, 0)

        passwordInput.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0, 0, R.drawable.eye_off, 0
        )
        fun showPopup
                    (message: String) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Invalid Input")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
        fun validatePassword(psw: String): String? {
            if (psw.length < 6) return "Password must be at least 6 characters"
            if (!psw.matches(".*[A-Z].*".toRegex())) return "Password must contain 1 uppercase letter"
            if (!psw.matches(".*[a-z].*".toRegex())) return "Password must contain 1 lowercase letter"
            if (!psw.matches(".*[0-9].*".toRegex())) return "Password must contain 1 digit"
            if (!psw.matches(".*[@#\$%^&+=].*".toRegex())) return "Password must contain 1 special character"

            return null // VALID
        }
        passwordInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                val drawableRight = passwordInput.compoundDrawablesRelative[2]
                if (drawableRight != null) {

                    val clickStart = passwordInput.width - passwordInput.paddingEnd - drawableRight.intrinsicWidth

                    if (event.x >= clickStart) {
                        togglePassword(passwordInput)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        inputBox.addTextChangedListener(object : TextWatcher {
            var dialogShown = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()

                if (text.isEmpty()) {
                    dialogShown = false
                    inputBox.filters = arrayOf()
                    return
                }
/*
                if (text.matches(Regex("\\d+"))) {
                    inputBox.filters = arrayOf(InputFilter.LengthFilter(10))
                    if (text.length == 10 && !dialogShown) {
                        dialogShown = true
                        showPopup("Mobile number cannot exceed 10 digits!")


                    }
                } else {
                    inputBox.filters = arrayOf()
                    /*
                    if (text.contains("@") && !text.endsWith("@gmail.com") && !dialogShown) {
                        dialogShown = true
                        showPopup("Please enter a valid Gmail ID ending with @gmail.com")

                    }

                     */
                }

 */
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        val psw = passwordInput.text.toString()
        val result = validatePassword(psw)

        if (result != null) {
            passwordInput.error = result
        } else {
            // password is valid → continue login
        }
        //Password
        inputBox.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val text = inputBox.text.toString().trim()

                if (text.isEmpty()) {
                    showPopup("This field cannot be empty!")
                    inputBox.postDelayed({
                        inputBox.requestFocus()
                    }, 150)
                    return@setOnFocusChangeListener
                }
/*
                if (text.matches(Regex("\\d+"))) {
                    if (text.length != 10) {
                        showPopup("Please Enter a valid 10-digit mobile number!")
                        inputBox.postDelayed({
                            inputBox.requestFocus()
                        }, 150)
                    }
                    return@setOnFocusChangeListener
                }
                /*
                if (text.contains("@")) {
                    if (!text.endsWith("@gmail.com")) {
                        showPopup("Please enter a valid Gmail ID ending with @gmail.com")
                        inputBox.postDelayed({
                            inputBox.requestFocus()
                        }, 150)
                    }
                    return@setOnFocusChangeListener
                }
                */

               // showPopup("Pleas enter a valid mobile number of Gmail ID!")
                inputBox.postDelayed({
                    inputBox.requestFocus()
                }, 150)

 */

            }


        }

        //
        /*
        binding.tvLogin.setOnClickListener {
            val email = binding.idInputBox.text.toString().trim()
            val password = binding.idPswInputBox.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Enter email & password",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this,"Error: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                    }
                }
        }

         */

        binding.loginBtn.setOnClickListener {

            val email = binding.idInputBox.text.toString().trim()
            val password = binding.idPswInputBox.text.toString().trim()

/*
             if(email==ADMIN_EMAIL && password== ADMIN_PASSWORD){
                 startActivity(Intent(this,AdminDashboardActivity::class.java))
                 finish()
                 return@setOnClickListener
             }

 */
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

             */

            /*
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }

             */
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->

                    val uid = authResult.user!!.uid

                    // 🔥 CHECK ROLE FROM FIRESTORE
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { document ->

                            val role = document.getString("role")

                            if (role == "admin") {
                                startActivity(Intent(this, AdminDashboardActivity::class.java))
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to fetch user role", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }

        }
    }

    private fun togglePassword(passwordInput: EditText) {
        val isVisible =
            passwordInput.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ==
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        if (isVisible) {
            passwordInput.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, R.drawable.eye_off, 0
            )
        } else {
            passwordInput.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, R.drawable.eye_on, 0
            )
        }
        passwordInput.setSelection(passwordInput.text.length)
    }
}