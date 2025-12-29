package com.example.parktrace.repository

import android.content.Context
import com.example.parktrace.model.Vehicle
import com.example.parktrace.model.VehicleModel
import com.example.parktrace.storage.VehicleStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VehicleRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun loadVehicles(): ArrayList<VehicleModel>{
        return VehicleStorage.loadVehicles(context)
    }
    fun saveVehicles(list:ArrayList<VehicleModel>){
        VehicleStorage.saveVehicles(context,list)
    }

    fun addVehicle(
        vehicle: Vehicle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not logged in")
            return
        }

        firestore.collection("users")
            .document(userId)
            .collection("vehicles")
            .add(vehicle)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Unknown error")
            }
    }

    fun deleteVehicle(
        list: ArrayList<VehicleModel>,
        position:Int,
    ): VehicleModel{
        val removed=list[position]
        list.removeAt(position)
        saveVehicles(list)
        return removed
    }
}