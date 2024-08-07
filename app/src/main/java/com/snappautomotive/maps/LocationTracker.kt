/*
 * Copyright (C) 2020-2024 Snapp Automotive Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.snappautomotive.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

/**
 * Responsible for initiating the tracking the users location and informing the
 * main activity when there is a change.
 *
 * This is optimised for automotive use, where power is constantly and so
 * we don't have to worry about battery related optimisations.
 */
// As a system app we know we'll get the permissions we need.
@SuppressLint("MissingPermission")
class LocationTracker(private val activity: Activity, callback: (Location) -> Unit) {

    private val locationManager = activity.getSystemService(LocationManager::class.java)
    private val listener = ListenerSimplifier(callback)

    fun startTracking() {
        when {
            ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                registerLocationListener()
            }
            shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION) -> showEducationalUI()
            else -> requestPermissions()
        }
    }

    private fun showEducationalUI() {
        AlertDialog.Builder(activity)
                .setMessage(R.string.location_permission_text)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions() }
                .show()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_RESULT_CODE)
    }

    fun handlePermissionResult(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerLocationListener()
        }
    }

    fun stopTracking() {
        locationManager.removeUpdates(listener)
    }

    private fun registerLocationListener() {
        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER,
            TimeUnit.SECONDS.toMillis(1L), // 1s updates
            1.0F,                                    // 1 meter updates
            listener)
        locationManager.getLastKnownLocation(
            LocationManager.FUSED_PROVIDER
        )
    }

    private class ListenerSimplifier(private val locationUpdateListener: (Location) -> Unit)
        : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationUpdateListener(location)
        }
    }

    companion object {
        const val PERMISSION_RESULT_CODE = 1000
    }
}