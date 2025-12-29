package com.example.parktrace.model

data class Vehicle(
    val ownerName:String="",
    val type:String="",
    val make:String="",
    val model:String="",
    val year:String="",
    val number:String="",
    val mobile: String="",
    val createdAt:Long=System.currentTimeMillis()

    )