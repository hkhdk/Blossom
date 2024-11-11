package com.julic20s.fleur.wiki

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.julic20s.fleur.fleurContainer

class WikiViewModel(app: Application) : AndroidViewModel(app) {

    val plants = fleurContainer.plantsRepository.plants

}