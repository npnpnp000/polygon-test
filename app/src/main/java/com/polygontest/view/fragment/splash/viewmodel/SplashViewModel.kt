package com.polygontest.view.fragment.splash.viewmodel


import androidx.lifecycle.*


class SplashViewModel() : ViewModel() {

    var isDataReady: MutableLiveData<Boolean> = MutableLiveData(false)

}
