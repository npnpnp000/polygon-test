package com.polygontest.dependency_injection

import com.polygontest.utils.extensions.DistanceCalculates

object DistanceModule {

    fun provideDistanceCalculates() : DistanceCalculates {
        return DistanceCalculates()
    }
}