package com.julic20s.fleur

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.julic20s.fleur.bluetooth.BluetoothHelper
import com.julic20s.fleur.data.DefaultPlantsRepository
import com.julic20s.fleur.data.PlantsRepository

interface FleurContainer {

    val bluetoothHelper: BluetoothHelper

    val plantsRepository: PlantsRepository

}

class FleurApplication : Application(), FleurContainer {

    override val bluetoothHelper: BluetoothHelper by lazy { BluetoothHelper(this) }

    override val plantsRepository: PlantsRepository get() = DefaultPlantsRepository

}

inline val AndroidViewModel.fleurContainer : FleurContainer get() = getApplication<FleurApplication>()