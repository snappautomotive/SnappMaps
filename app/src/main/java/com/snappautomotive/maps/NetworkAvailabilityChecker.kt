package com.snappautomotive.maps

import android.net.ConnectivityManager
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck

/**
 * Dummy network checker which always says a network is available.
 */
class NetworkAvailabilityChecker :
        INetworkAvailablityCheck {

    var connectivityManager: ConnectivityManager? = null

    override fun getNetworkAvailable(): Boolean {
        return isAnyNetworkConnected()
    }

    override fun getWiFiNetworkAvailable(): Boolean {
        return isAnyNetworkConnected()
    }

    override fun getCellularDataNetworkAvailable(): Boolean {
        return isAnyNetworkConnected()
    }

    override fun getRouteToPathExists(hostAddress: Int): Boolean {
        return isAnyNetworkConnected()
    }

    fun isAnyNetworkConnected(): Boolean {
        return connectivityManager?.activeNetwork != null
    }
}