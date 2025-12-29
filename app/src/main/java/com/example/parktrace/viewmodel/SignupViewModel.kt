package com.example.parktrace.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class SignupFormState(
    val isValid: Boolean,
    val errorMessage: String? = null
)

class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> get() = _signupResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    /*
    fun validateForm(
        owner: String,
        email: String,
        mobile: String,
        address: String,
        password: String
    ): SignupFormState {
        if (owner.isEmpty() || email.isEmpty() || mobile.isEmpty() || address.isEmpty() || password.isEmpty()) {
            return SignupFormState(false, "All fields are required")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return SignupFormState(false, "Enter a valid email")
        }
        if (password.length < 6) {
            return SignupFormState(false, "Password must be at least 6 characters")
        }
        return SignupFormState(true)
    }


     */


    // ✅ SIGNUP + FIRESTORE SAVE (FIXED)
    /*
    fun signup(
        owner: String,
        email: String,
        mobile: String,
        address: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // ✅ SAFE MAP (NO PASSWORD)
                    val registrationMap = hashMapOf(
                        "id" to uid,
                        "ownerName" to owner,
                        "email" to email,
                        "mobile" to mobile,
                        "address" to address,
                        "createdAt" to System.currentTimeMillis()
                    )

                    // ✅ STORE FOR ADMIN REPORT
                    firestore.collection("registrations")
                        .document(uid)
                        .set(registrationMap)
                        .addOnSuccessListener {
                            _signupResult.value = true
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Firestore Error: ${e.message}"
                        }

                } else {
                    _errorMessage.value = task.exception?.message ?: "Signup failed"
                }
            }

     */
    fun signup(
        owner: String,
        email: String,
        mobile: String,
        address: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                // ✅ AUTH SUCCESS – UID EXISTS NOW
                val uid = authResult.user!!.uid
                val userMap = hashMapOf(
                    "ownerName" to owner,
                    "email" to email,
                    "mobile" to mobile,
                    "address" to address,
                    "role" to "user",
                    "password" to password,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(uid)      // ✅ UID AS DOCUMENT ID
                    .set(userMap)
                    .addOnSuccessListener {
                        _signupResult.postValue(true)
                    }
                    .addOnFailureListener { e ->
                        _errorMessage.postValue(e.message)
                    }

            }
            .addOnFailureListener { e ->
                _errorMessage.postValue(e.message)
            }
    }

    fun validateForm(
        owner: String,
        email: String,
        mobile: String,
        address: String,
        password: String
    ): FormState {
        if (owner.isEmpty()) return FormState(false, "Enter owner name")
        if (email.isEmpty()) return FormState(false, "Enter email")
        if (mobile.length < 10) return FormState(false, "Invalid mobile")
        if (address.isEmpty()) return FormState(false, "Enter address")
        if (password.length < 6) return FormState(false, "Password must be 6+ chars")

        return FormState(true, "")
    }

}
data class FormState(
    val isValid: Boolean,
    val errorMessage: String
)