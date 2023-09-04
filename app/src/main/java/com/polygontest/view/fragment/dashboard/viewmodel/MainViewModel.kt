package com.polygontest.view.fragment.dashboard.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.polygontest.model.ui_models.GpsLocation
import com.polygontest.utils.constants.FilePickerConstants
import com.polygontest.utils.constants.FilePickerConstants.FILE_NOT_KML_ERROR
import com.polygontest.utils.constants.FilePickerConstants.FILE_VALID_POLYGON_ERROR
import com.polygontest.utils.constants.FilePickerConstants.POLYGON_DATA_ERROR
import com.polygontest.utils.extensions.DistanceCalculates


class MainViewModel(private val distanceCalculates : DistanceCalculates?) : ViewModel() {

    val polygonData: MutableLiveData<ArrayList<GpsLocation>> = MutableLiveData<ArrayList<GpsLocation>>(arrayListOf())
    val kmlFileName: MutableLiveData<String> = MutableLiveData<String>("")
    val errorManager : MutableLiveData<String> = MutableLiveData("")
    val userPoint : MutableLiveData<GpsLocation?> = MutableLiveData(null)

    fun checkPointInsidePolygon(point : GpsLocation,poly :ArrayList<GpsLocation>) : Boolean{
        var isInside = false
        if (distanceCalculates?.checkInside(poly,poly.size,point) == 1){
            isInside = true
        }
        return isInside
    }

    fun getDistancePointToPolygon(point : GpsLocation,poly :ArrayList<GpsLocation>): Double? {
        return distanceCalculates?.getMinDistance(poly,point)
    }

    fun checkFileName(fileName: String?): Boolean {

        var isKmlFile = false

        fileName?.let { name ->
            val nameArr = name.split(".")
            if (nameArr[1].contains("kml")){
               isKmlFile = true
            }else{

                startFileError(FILE_NOT_KML_ERROR, FILE_NOT_KML_ERROR)

            }
        }

        return isKmlFile
    }

    fun startFileError(logText: String,text : String){
        Log.e("ERROR",logText)
        errorManager.value = text
        clearPolygonDataAndFile()
    }

    fun setPolygonData(coordinateString: String) {
        try {
            val polygonArray = arrayListOf<GpsLocation>()

            val coordinateArray = coordinateString.split(" ")

            for (i in 0 until coordinateArray.size-1){
                val pointArray = coordinateArray[0].split(",")
                polygonArray.add(GpsLocation(pointArray[0].toDouble(),pointArray[1].toDouble()))
            }

            Log.e("polygonArray", polygonArray.toString())
            polygonData.value = polygonArray

        }catch (e : Exception){
            startFileError(e.stackTraceToString(), POLYGON_DATA_ERROR)
        }
    }

    fun clearPolygonDataAndFile() {

        if (polygonData.value!!.isNotEmpty() ){
            polygonData.value = arrayListOf()
        }
        if (kmlFileName.value != ""){
            kmlFileName.value = ""
        }


    }

    fun checkIsPolygon(n :Int) : Boolean{

        var isPolygon = true

        // When polygon has less than 3 edge, it is not
        // polygon
        if (n < 3){
            isPolygon = false

            startFileError(FILE_VALID_POLYGON_ERROR, FILE_VALID_POLYGON_ERROR)
        }

        return isPolygon

    }
}


