package com.polygontest.dependency_injection

import android.content.Context
import android.net.ConnectivityManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.polygontest.utils.application.App
import com.polygontest.utils.factories.ViewModelFactory
import com.polygontest.view.fragment.dashboard.viewmodel.MainViewModel
import com.polygontest.view.fragment.splash.viewmodel.SplashViewModel

object ViewModelModule {

    inline fun <reified VM : ViewModel> provideViewModel(fragment: Fragment): Lazy<VM> {
        val viewModelFactory = when (VM::class.java) {
             SplashViewModel::class.java -> {
                ViewModelFactory()
            }
            MainViewModel::class.java -> {
                ViewModelFactory(DistanceModule.provideDistanceCalculates())
            }
            else -> throw RuntimeException("ViewModel does not exist")
        }
        return lazy { ViewModelProvider(fragment, viewModelFactory)[VM::class.java] }
    }
}