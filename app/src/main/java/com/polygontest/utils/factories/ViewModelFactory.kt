package com.polygontest.utils.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.polygontest.utils.extensions.DistanceCalculates
import com.polygontest.view.fragment.dashboard.viewmodel.MainViewModel
import com.polygontest.view.fragment.splash.viewmodel.SplashViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(val distanceCalculates : DistanceCalculates? =null) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when (modelClass) {
            MainViewModel::class.java -> MainViewModel(distanceCalculates) as T
            SplashViewModel::class.java -> SplashViewModel() as T
            else -> throw Exception("ViewModel not found")
        }
}