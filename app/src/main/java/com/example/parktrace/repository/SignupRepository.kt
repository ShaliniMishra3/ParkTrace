package com.example.parktrace.repository

import com.example.parktrace.model.RegistrationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun signup(
        owner: String,
        email: String,
        mobile: String,
        address: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid ?: return@addOnSuccessListener

                val registration = RegistrationModel(
                    id = uid,
                    ownerName = owner,
                    email = email,
                    mobile = mobile,
                    address = address,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("registrations")
                    .document(uid)
                    .set(registration)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener {
                        onError(it.message ?: "Failed to save registration")
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Signup failed")
            }
    }
}