package com.snappautomotive.maps

import android.app.Activity
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.core.app.ActivityCompat

import androidx.preference.PreferenceManager
import com.snappautomotive.maps.databinding.MainLayoutBinding

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.IRegisterReceiver
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider
import org.osmdroid.tileprovider.modules.TileWriter
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main Activity which is used by the dashboard view to display the map.
 */
class MainActivity : Activity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val mNetworkAvailabilityChecker  = NetworkAvailabilityChecker()

    private lateinit var binding : MainLayoutBinding
    private lateinit var locationTracker: LocationTracker

    // Callback class which allows us to track which networks are available.
    private val mNetworkCallback = object: ConnectivityManager.NetworkCallback() {
        val mCurrentConnectivityState = AtomicBoolean(false)

        override fun onAvailable(network: Network) {
            invalidateMapIfStateChange()
        }

        override fun onLost(network: Network) {
            invalidateMapIfStateChange()
        }

        fun invalidateMapIfStateChange() {
            val connectivityState = mNetworkAvailabilityChecker.isAnyNetworkConnected()
            if (mCurrentConnectivityState.compareAndSet(connectivityState, connectivityState)) {
                return
            }

            binding.mapView.postInvalidate()
        }
    }

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        Configuration.getInstance().load(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureOSMDroid()

        binding.mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        binding.mapView.controller.setZoom(16.0)

        locationTracker = LocationTracker(this, ::updateLocation)
        setDefaultLocation()
    }

    public override fun onStart() {
        super.onStart()
        locationTracker.startTracking()
    }

    public override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        mNetworkAvailabilityChecker.connectivityManager = connectivityManager

        binding.mapView.onResume()

        connectivityManager.registerDefaultNetworkCallback(mNetworkCallback)
    }

    public override fun onPause() {
        super.onPause()

        getSystemService(ConnectivityManager::class.java)
            .unregisterNetworkCallback(mNetworkCallback)

        Configuration.getInstance().save(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        binding.mapView.onPause()

        mNetworkAvailabilityChecker.connectivityManager = null
    }

    public override fun onStop() {
        super.onStop()
        locationTracker.stopTracking()
    }

    private fun configureOSMDroid() {
        val applicationContext = applicationContext
        val registerReceiver: IRegisterReceiver = SimpleRegisterReceiver(applicationContext)

        val tileSource: ITileSource = TileSourceFactory.DEFAULT_TILE_SOURCE

        val tileWriter = TileWriter()
        val fileSystemProvider = MapTileFilesystemProvider(registerReceiver, tileSource)

        val downloaderProvider = MapTileDownloader(
            tileSource, tileWriter, mNetworkAvailabilityChecker)

        val tileProviderArray = MapTileProviderArray(
                tileSource,
                registerReceiver,
                arrayOf(fileSystemProvider, downloaderProvider))

        binding.mapView.tileProvider = tileProviderArray
    }

    private fun setDefaultLocation() {
        updateLocation(48.129085, 11.5654545, 18.0)
    }

    private fun updateLocation(location: Location) {
        updateLocation(location.latitude, location.longitude, location.altitude)
    }

    private fun updateLocation(latitude:Double, longitude:Double, altitude:Double) {
        val position = GeoPoint(latitude, longitude, altitude)
        val mapController = binding.mapView.controller
        mapController.setCenter(position)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == LocationTracker.PERMISSION_RESULT_CODE) {
            locationTracker.handlePermissionResult(grantResults)
        }
    }
}