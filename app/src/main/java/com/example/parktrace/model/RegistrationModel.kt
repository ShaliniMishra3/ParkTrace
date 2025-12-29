package com.example.parktrace.model

data class RegistrationModel(
    var id:String="",
    var ownerName:String="",
    var email:String="",
    var mobile:String="",
    var address:String="",
    var password: String="",
    var createdAt:Long=0L
)