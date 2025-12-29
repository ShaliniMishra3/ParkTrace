package com.example.parktrace.model

data class VehicleEntity (
    var id: String="",
    val ownerName: String = "",
    val type: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val number: String = "",
    val mobile: String = "",
    val qrUrl: String= "",
    var userId: String = "",
    var createdAt: Long = 0L
)