package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocationCheckResult(
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float,
    val isWithinRadius: Boolean,
    val allowedRadiusMeters: Double,
    val errorMessage: String? = null
)

object LocationHelper {

    fun calculateDistance(
        currentLat: Double,
        currentLon: Double,
        targetLat: Double,
        targetLon: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(currentLat, currentLon, targetLat, targetLon, results)
        return results[0]
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { continuation ->
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            // Check last known location first for immediate response
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            var bestLastKnown: Location? = null
            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLastKnown == null || loc.accuracy < bestLastKnown.accuracy) {
                                bestLastKnown = loc
                            }
                        }
                    }
                } catch (_: SecurityException) { }
            }

            if (bestLastKnown != null) {
                continuation.resume(bestLastKnown)
                return@suspendCancellableCoroutine
            }

            // Otherwise request a single update
            var resumed = false
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!resumed) {
                        resumed = true
                        locationManager.removeUpdates(this)
                        continuation.resume(location)
                    }
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    LocationManager.NETWORK_PROVIDER
                } else {
                    null
                }

                if (provider != null) {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                if (!resumed) {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                try {
                    locationManager.removeUpdates(listener)
                } catch (_: Exception) {}
            }
        }
}
