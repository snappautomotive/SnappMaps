package com.snappautomotive.maps

import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck

/**
 * Dummy network checker which always says a network is available.
 */
class NetworkAvailabilityChecker : INetworkAvailablityCheck {
    override fun getNetworkAvailable(): Boolean {
        return true;
    }

    override fun getWiFiNetworkAvailable(): Boolean {
        return true;
    }

    override fun getCellularDataNetworkAvailable(): Boolean {
        return true;
    }

    override fun getRouteToPathExists(hostAddress: Int): Boolean {
        return true;
    }
}