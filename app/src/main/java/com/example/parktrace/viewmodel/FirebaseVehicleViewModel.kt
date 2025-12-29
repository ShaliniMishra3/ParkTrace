package com.example.parktrace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

import androidx.lifecycle.ViewModel
import com.example.parktrace.model.VehicleEntity
import com.example.parktrace.repository.FirebaseVehicleRepository

class FirebaseVehicleViewModel: ViewModel(){

    private val repository = FirebaseVehicleRepository()

    private val _vehicles = MutableLiveData<List<VehicleEntity>>()

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    private val _error = MutableLiveData<String>()

    val vehicles = MutableLiveData<List<VehicleEntity>>()
    val error = MutableLiveData<String>()
    private val firestore = FirebaseFirestore.getInstance()
    fun startListening() {
        repository.listenVehicles(
            onSuccess = { vehicles.postValue(it) },
            onError = { error.postValue(it) }
        )
    }

    fun addVehicle(vehicle: VehicleEntity) {
        repository.addVehicle(
            vehicle,
            onSuccess = { _success.value = true },
            onError = { _error.value = it }
        )
    }

    fun loadVehicles() {
        repository.fetchVehicles(
            onSuccess = { _vehicles.value = it },
            onError = { _error.value = it }
        )
    }
    fun updateVehicle(vehicle: VehicleEntity) {
        repository.updateVehicle(
            vehicle,
            onSuccess = { _success.value = true },
            onError = { _error.value = it }
        )
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        repository.deleteVehicle(
            vehicle,
            onSuccess = { _success.value = true },
            onError = { _error.value = it }
        )
    }
    fun getAllVehicles(onResult: (ArrayList<VehicleEntity>) -> Unit) {
        firestore.collection("vehicles")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = ArrayList<VehicleEntity>()
                for (doc in snapshot.documents) {
                    val vehicle = doc.toObject(VehicleEntity::class.java)
                    if (vehicle != null) list.add(vehicle)
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(ArrayList())
            }
    }





}