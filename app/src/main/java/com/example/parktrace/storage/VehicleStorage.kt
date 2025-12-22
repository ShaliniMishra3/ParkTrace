package com.example.parktrace.storage

import android.content.Context
import com.example.parktrace.model.VehicleModel
import com.google.gson.reflect.TypeToken

import com.google.gson.Gson


object VehicleStorage {
    private const val PREF_NAME="vehicle_prefs"
    private const val VEHICLE_KEY="vehicle_list"

    fun saveVehicles(context: Context,list: ArrayList<VehicleModel>){
        val prefs=context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
        val editor=prefs.edit()
        val json= Gson().toJson(list)
        editor.putString(VEHICLE_KEY,json)
        editor.apply()
    }

    fun loadVehicles(context: Context): ArrayList<VehicleModel>{
        val prefs=context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json=prefs.getString(VEHICLE_KEY,null)

        return if (json!=null){
            val type= object :TypeToken<ArrayList<VehicleModel>>(){}.type
            Gson().fromJson(json,type)

        }else{
            ArrayList()
        }
    }
}