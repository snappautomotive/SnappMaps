package com.snappautomotive.maps

import android.app.Activity
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

/**
 * Main Activity which is used by the dashboard view to display the map.
 */
class MainActivity : Activity() {
    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        Configuration.getInstance().load(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))
        //TODO check permissions
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
        if (mapView != null) {
            mapView.onResume()
        }
    }

    public override fun onPause() {
        super.onPause()
        Configuration.getInstance().save(applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext))
        if (mapView != null) {
            mapView.onPause()
        }
    }

    private fun configureOSMDroid() {
        val applicationContext = applicationContext
        val registerReceiver: IRegisterReceiver = SimpleRegisterReceiver(applicationContext)

// Create a custom tile source
        val tileSource: ITileSource = TileSourceFactory.DEFAULT_TILE_SOURCE

// Create a file cache modular provider
        val tileWriter = TileWriter()
        val fileSystemProvider = MapTileFilesystemProvider(registerReceiver, tileSource)

// Create a download modular tile provider
        val downloaderProvider = MapTileDownloader(
                tileSource, tileWriter, NetworkAvailabilityChecker())

// Create a custom tile provider array with the custom tile source and the custom tile providers
        val tileProviderArray = MapTileProviderArray(
                tileSource,
                registerReceiver,
                arrayOf(fileSystemProvider, downloaderProvider))

// Create the mapview with the custom tile provider array
        mapView.tileProvider = tileProviderArray
    }
}