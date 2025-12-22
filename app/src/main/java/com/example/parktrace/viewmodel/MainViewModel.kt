package com.example.parktrace.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.parktrace.model.VehicleModel
import com.example.parktrace.repository.VehicleRepository
import com.example.parktrace.utils.QrUtils

class MainViewModel(application: Application): AndroidViewModel(application) {
    private var lastDeletedVehicle: VehicleModel? = null
    private var lastDeletedPosition: Int = -1
    private val repository = VehicleRepository(application)
    private val allVehicles = ArrayList<VehicleModel>()
    val filteredVehicles = MutableLiveData<ArrayList<VehicleModel>>()
    private val _qrSaved = MutableLiveData<Boolean>()
    val qrSaved: LiveData<Boolean> get() = _qrSaved

    private val _editVehicleRequest = MutableLiveData<VehicleModel?>()
    val editVehicleRequest: LiveData<VehicleModel?> get() = _editVehicleRequest

    fun requestEditVehicle(position: Int) {
        val vehicle = filteredVehicles.value?.get(position)
        _editVehicleRequest.value = vehicle
    }


    fun clearEditRequest() {
        _editVehicleRequest.value = null
    }
    fun loadVehicles() {
        allVehicles.clear()
        allVehicles.addAll(repository.loadVehicles())
        filteredVehicles.value = ArrayList(allVehicles)
    }

    fun addVehicle(vehicle: VehicleModel) {
        allVehicles.add(vehicle)
        repository.saveVehicles(allVehicles)
        filteredVehicles.value = ArrayList(allVehicles)
    }

    fun deleteVehicle(position: Int) {
        lastDeletedVehicle = filteredVehicles.value?.get(position)
        lastDeletedPosition = position
        lastDeletedVehicle?.let {
            allVehicles.remove(it)
            repository.saveVehicles(allVehicles)
            filteredVehicles.value = ArrayList(allVehicles)
        }
    }

    fun undoDelete() {
        lastDeletedVehicle?.let {
            allVehicles.add(lastDeletedPosition, it)
            repository.saveVehicles(allVehicles)
            filteredVehicles.value = ArrayList(allVehicles)
        }
    }

    fun updateVehicle(position: Int, updatedVehicle: VehicleModel) {
        val oldVehicle = filteredVehicles.value?.get(position) ?: return
        val indexInAll = allVehicles.indexOf(oldVehicle)
        if (indexInAll != -1) {
            allVehicles[indexInAll] = updatedVehicle
            repository.saveVehicles(allVehicles)
            filteredVehicles.value = ArrayList(allVehicles)
        }
    }

    fun filterByType(type: String) {
        filteredVehicles.value = if (type == "All") {
            ArrayList(allVehicles)
        } else {
            ArrayList(
                allVehicles.filter
                { it.type == type })
        }
    }

    fun generateVehicleQR(vehicle: VehicleModel) {
        QrUtils(getApplication()).generateVehicleQR(vehicle)
        _qrSaved . value = true
    }
    fun updateVehicleData(oldVehicle: VehicleModel, updatedVehicle: VehicleModel) {
        val index = allVehicles.indexOf(oldVehicle)
        if (index != -1) {
            allVehicles[index] = updatedVehicle
            repository.saveVehicles(allVehicles)
            filteredVehicles.value = ArrayList(allVehicles)
        }
    }



}

