package com.example.parktrace.repository

import android.content.Context
import com.example.parktrace.model.VehicleModel
import com.example.parktrace.storage.VehicleStorage

class VehicleRepository(private val context: Context) {

    fun loadVehicles(): ArrayList<VehicleModel>{
        return VehicleStorage.loadVehicles(context)
    }
    fun saveVehicles(list:ArrayList<VehicleModel>){
        VehicleStorage.saveVehicles(context,list)
    }
    fun addVehicle(
        list: ArrayList<VehicleModel>,
        vehicle: VehicleModel
    ){
        list.add(vehicle)
        saveVehicles(list)
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