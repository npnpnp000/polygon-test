package com.polygontest.view.fragment.splash.fragment

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.polygontest.R
import com.polygontest.databinding.FragmentSplashBinding
import com.polygontest.utils.constants.GpsConstants
import com.polygontest.utils.extensions.provideViewModel
import com.polygontest.view.fragment.splash.viewmodel.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    var binding: FragmentSplashBinding? = null

    private val splashViewModel: SplashViewModel by provideViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeDataReadyNavigation()

        checkGpsPermission()
    }

    private fun checkGpsPermission(){
        requestGpsPermissionIfNeed{granted ->
            if(granted){
                splashViewModel.isDataReady.value = true
            }else{
                startErrorDialog(context!!.getString(R.string.gps_error))
            }
        }
    }

    private fun requestGpsPermissionIfNeed(onResult: ((Boolean) -> Unit)){
        if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            onResult(true)
        }else{
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun observeDataReadyNavigation() {
        splashViewModel.isDataReady.observe(viewLifecycleOwner) { isDataReady ->
            if (isDataReady.not()) return@observe
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        lifecycle.coroutineScope.launch(Dispatchers.Default) {
            Thread.sleep(1_500) // add waiting time to show the splash page

            lifecycle.coroutineScope.launch(Dispatchers.Main) {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            }
        }
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
                activity?.finish()
            }
            show()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
