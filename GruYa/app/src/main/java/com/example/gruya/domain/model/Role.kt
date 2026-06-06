package com.example.gruya.domain.model

import com.google.gson.annotations.SerializedName

enum class Role {
    @SerializedName("User")
    USER,
    @SerializedName("Admin")
    ADMIN,
    @SerializedName("Provider")
    PROVIDER
}