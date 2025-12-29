package com.example.parktrace.repository


import com.example.parktrace.model.VehicleEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseVehicleRepository {
    private val firestore= FirebaseFirestore.getInstance()
    private val auth= FirebaseAuth.getInstance()

    private fun userVehicles(uid: String)=
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")

    fun addVehicle(
        vehicle: VehicleEntity,
        onSuccess:() ->Unit,
        onError: (String) -> Unit
    ){
        val uid = auth.currentUser?.uid?:run {
            onError("User not authenticated")
            return
        }
        val vehicleId = firestore.collection("vehicles").document().id
        vehicle.id = vehicleId
        vehicle.userId = uid
        vehicle.createdAt = System.currentTimeMillis()

        val userRef = userVehicles(uid).document(vehicleId)
        val globalRef = firestore.collection("vehicles").document(vehicleId)

        userRef.set(vehicle)
            .continueWithTask { globalRef.set(vehicle) }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to add vehicle")
            }

    }
    fun updateVehicle(
        vehicle: VehicleEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("User not logged in")
            return
        }
        val userRef = userVehicles(uid).document(vehicle.id)
        val globalRef = firestore.collection("vehicles").document(vehicle.id)

        userRef.set(vehicle)
            .continueWithTask { globalRef.set(vehicle) }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Update failed")
            }
    }
    fun deleteVehicle(
        vehicle: VehicleEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onError("User not logged in")
            return
        }
        val userRef = userVehicles(uid).document(vehicle.id)
        val globalRef = firestore.collection("vehicles").document(vehicle.id)

        userRef.delete()
            .continueWithTask { globalRef.delete() }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Failed to delete vehicle")
            }

    }

    fun fetchVehicles(
        onSuccess: (List<VehicleEntity>) -> Unit,
        onError: (String) -> Unit
    ){
        val uid = auth.currentUser?.uid ?: run {
            onError("User not logged in")
            return
        }
        userVehicles(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull {
                    it.toObject(VehicleEntity::class.java)
                }
                onSuccess(list)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load vehicles")
            }
    }
    fun listenVehicles(
        onSuccess: (List<VehicleEntity>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        userVehicles(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Listen failed")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(VehicleEntity::class.java)
                } ?: emptyList()

                onSuccess(list)
            }
    }

}