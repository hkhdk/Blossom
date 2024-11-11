package com.julic20s.fleur.data

data class PlantInfo(
    val id: Int,
    val res: Int,
    val name: String,
    val type: String,
    val description: String,
    val info: Map<String, String>
)