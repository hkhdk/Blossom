package com.julic20s.fleur.data

interface PlantsRepository {

    val plants: List<PlantInfo>

    fun getPlant(id: Int): PlantInfo?

}