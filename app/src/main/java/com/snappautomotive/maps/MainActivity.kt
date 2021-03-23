package com.snappautomotive.maps

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle

import androidx.preference.PreferenceManager

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

import kotlinx.android.synthetic.main.main.mapView
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main Activity which is used by the dashboard view to display the map.
 */
class MainActivity : Activity() {

    private val mNetworkAvailabilityChecker  = NetworkAvailabilityChecker()

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

            mapView.postInvalidate()
        }
    }

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        Configuration.getInstance().load(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.main)

        configureOSMDroid()

        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val startPosition = GeoPoint(48.129085, 11.5654545, 18.0)
        val mapController = mapView.controller
        mapController.setZoom(16.0)
        mapController.setCenter(startPosition)
    }

    public override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        mNetworkAvailabilityChecker.connectivityManager = connectivityManager

        if (mapView != null) {
            mapView.onResume()
        }

        connectivityManager.registerDefaultNetworkCallback(mNetworkCallback)
    }

    public override fun onPause() {
        super.onPause()

        getSystemService(ConnectivityManager::class.java)
            .unregisterNetworkCallback(mNetworkCallback)

        Configuration.getInstance().save(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))

        if (mapView != null) {
            mapView.onPause()
        }

        mNetworkAvailabilityChecker.connectivityManager = null
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

        mapView.tileProvider = tileProviderArray
    }
}