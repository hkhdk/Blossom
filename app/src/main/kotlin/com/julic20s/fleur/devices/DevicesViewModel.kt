package com.julic20s.fleur.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.julic20s.fleur.fleurContainer

class DevicesViewModel(application: Application) : AndroidViewModel(application) {

    val connectionState = fleurContainer.bluetoothHelper.connectionStateFlow.asLiveData()

}