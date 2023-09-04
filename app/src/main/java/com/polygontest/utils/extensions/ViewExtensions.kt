package com.polygontest.utils.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.polygontest.dependency_injection.ViewModelModule

inline fun <reified VM : ViewModel> Fragment.provideViewModel() =
    ViewModelModule.provideViewModel<VM>(this)


