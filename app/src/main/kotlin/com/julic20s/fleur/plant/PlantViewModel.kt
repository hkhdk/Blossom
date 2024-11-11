package com.julic20s.fleur.plant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.julic20s.fleur.fleurContainer

class PlantViewModel(app: Application) : AndroidViewModel(app) {

    val plantsRepository get() = fleurContainer.plantsRepository

}