package com.example.gruya.connectivity

data class MechanicalGuideIndex(
    val id: Int,
    val title: String,
    val fileName: String,
    val icon: String
)

data class MechanicalGuideDetail(
    val id: Int,
    val title: String,
    val description: String,
    val steps: List<String>,
    val warnings: List<String>
)
