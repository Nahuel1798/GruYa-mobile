package com.example.gruya.domain.model

data class User(
    val id : Int,
    val name : String,
    val lastname : String,
    val Email : String,
    val Password : String,
    val Role : Role
)