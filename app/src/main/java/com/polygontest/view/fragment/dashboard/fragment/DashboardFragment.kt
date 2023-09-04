package com.polygontest.view.fragment.dashboard.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.polygontest.R
import com.polygontest.databinding.FragmentMainBinding
import com.polygontest.model.ui_models.GpsLocation
import com.polygontest.utils.constants.Constants.EMPTY
import com.polygontest.utils.constants.FilePickerConstants
import com.polygontest.utils.extensions.provideViewModel
import com.polygontest.view.fragment.dashboard.viewmodel.MainViewModel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Math.round
import java.util.*


class DashboardFragment : Fragment() {

    private var binding: FragmentMainBinding? = null
    private val mainViewModel: MainViewModel by provideViewModel()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()

        binding?.loadFileBtn?.setOnClickListener{

            mainViewModel.errorManager.value = EMPTY // reset the file error manager

           loadFile()
        }

        binding?.removeBtn?.setOnClickListener{

            mainViewModel.errorManager.value = "" // reset the file error manager

            mainViewModel.clearPolygonDataAndFile()
        }

        binding?.checkDistanceBtn?.setOnClickListener{

            getGpsDeviceLocation()

        }
    }


    private fun setObservers() {

        setPolygonObserver()

        setFileErrorManagerObserver()

        setUserPointObserver()

    }

    private fun setUserPointObserver() {

        mainViewModel.userPoint.observe(viewLifecycleOwner) { deviceLocation ->

            if (deviceLocation != null) {

                val isInside = mainViewModel.checkPointInsidePolygon(
                    deviceLocation,
                    mainViewModel.polygonData.value!!
                )
                if (isInside) {

                    binding?.resultsTxt?.text = context!!.getString(R.string.in_area)

                } else {
                    val distance = getDistance()
                    val kmDistance = distance?.let {
                         it  * 11.1
                    }?: 0.0
                    binding?.resultsTxt?.text = context!!.getString(R.string.out_area) +
                            " ${round(kmDistance,2)}"
                }
            }
        }
    }
    //A solution from the Internet - stackoverflow
   private fun round(value: Double, places: Int): Double {
        var value = value
        require(places >= 0)
        val factor = Math.pow(10.0, places.toDouble()).toLong()
       value *= factor
        val tmp = round(value)
        return tmp.toDouble() / factor
    }

    private fun getDistance() :Double?{

            val distance = mainViewModel.getDistancePointToPolygon(
                mainViewModel.userPoint.value!!,
               mainViewModel.polygonData.value!!
            )

        return distance

    }

    private fun setFileErrorManagerObserver() {

        mainViewModel.errorManager.observe(viewLifecycleOwner){ errorString ->
            if (errorString != EMPTY){
                startErrorDialog(errorString)
            }

        }
    }

    private fun setPolygonObserver() {

        mainViewModel.polygonData.observe(viewLifecycleOwner) { polygonList ->

            if ( polygonList.isNotEmpty() &&  mainViewModel.kmlFileName.value != EMPTY
                && mainViewModel.checkIsPolygon(polygonList.size)) {

                binding?.removeBtn?.isEnabled = true
                binding?.checkDistanceBtn?.isEnabled = true

                binding?.fileNameTxt?.text = mainViewModel.kmlFileName.value

                binding?.resultsTxt?.text = context!!.getString(R.string.polygon_found)
            }else{
                if (polygonList.isNotEmpty() ||  mainViewModel.kmlFileName.value != EMPTY) {
                    mainViewModel.clearPolygonDataAndFile()
                }

                binding?.removeBtn?.isEnabled = false
                binding?.checkDistanceBtn?.isEnabled = false

                binding?.fileNameTxt?.text = EMPTY

                binding?.resultsTxt?.text = context!!.getString(R.string.polygon_not_found)

            }

        }

    }

    private fun getGpsDeviceLocation(){

        if (checkPermissions()) {
            if (isLocationEnabled()) {

                getLocation()

            }else {
                Toast.makeText(context, context!!.getString(R.string.turn_on_location), Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            mainViewModel.errorManager.value = context!!.getString(R.string.confirm_gps)
        }
    }

    private fun checkPermissions(): Boolean {
        var isPermissionsGranted = false

        if (ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            isPermissionsGranted = true
        }
        return isPermissionsGranted
    }
    //A solution from the Internet - stackoverflow
    // https://techpassmaster.com/get-current-location-in-android-studio-using-kotlin/
    private fun getLocation(){
        val locationClient = LocationServices.getFusedLocationProviderClient(context!!)

        locationClient.lastLocation.addOnCompleteListener(context!! as Activity) { task ->
            val location: Location? = task.result
            if (location != null) {
                val geocoder = Geocoder(context!!, Locale.getDefault())
                val list: List<Address> =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)

                Log.e("location", "Latitude\n${list[0].latitude}")
                Log.e("location", "Longitude\n${list[0].longitude}")
                Log.e("location", "Country Name\n${list[0].countryName}")
                Log.e("location", "Locality\n${list[0].locality}")
                Log.e("location", "Address\n${list[0].getAddressLine(0)}")

                mainViewModel.userPoint.value = GpsLocation(list[0].latitude,list[0].longitude)

            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun loadFile() {
        if ((ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)) {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, context!!.getString(R.string.select_file)), FilePickerConstants.FILE_PICKER_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var isKmlFile = false
        var coordinateString = EMPTY
        var fileName  = EMPTY


        if (requestCode == FilePickerConstants.FILE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {

            // The normal file loading caused problems with the KML files
            // I found a solution that works from stackoverflow

            try {
                data?.data?.let {// The URI with the location of the file

                     getFileName(it, context!!)?.let { name ->
                         fileName = name
                     }

                    isKmlFile = mainViewModel.checkFileName(fileName)

                    context?.contentResolver?.openInputStream(it)

                }?.let {

                    if (isKmlFile) {  // read from file only if it's kml file
                      coordinateString = readLineFromFile(it)
                    }
                }

                mainViewModel.kmlFileName.value = fileName
                mainViewModel.setPolygonData(coordinateString)

            } catch (e: Exception) { // If the app failed to attempt to retrieve the error file, throw an error alert
                mainViewModel.startFileError(e.stackTraceToString(),FilePickerConstants.READ_POLYGON_ERROR)

            }
        }
    }

    private fun readLineFromFile(inputStream: InputStream):String{

        var isCoordinateLine = false
        var coordinateString = ""

            val r = BufferedReader(InputStreamReader(inputStream))
            while (true) {

                val line: String = r.readLine() ?: break

                if (!isCoordinateLine && line.contains(FilePickerConstants.COORDINATE_START)) {
                    isCoordinateLine = true
                } else if (isCoordinateLine && line.contains(FilePickerConstants.COORDINATE_END)) {
                    isCoordinateLine = false
                } else if (isCoordinateLine) {
                    coordinateString += line
                }
            }
        return coordinateString
    }


    private fun getFileName(uri : Uri, context: Context): String? {

        val cursor: Cursor? = context.getContentResolver()?.query(uri, null, null, null, null)

        val nameIndex: Int? = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        cursor?.moveToFirst()

        val name: String? = nameIndex?.let { it1 -> cursor.getString(it1) }

        return name

    }

    private fun startErrorDialog(massage: String) {
        val alertDialog: AlertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.apply {
            setTitle(getString(R.string.error))
            setMessage(massage)
            setButton(
                AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }


}