package com.julic20s.fleur.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.julic20s.fleur.fleurContainer

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    val connectionState = fleurContainer.bluetoothHelper.connectionStateFlow.asLiveData()

}