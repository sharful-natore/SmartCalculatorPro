package com.example.ui.islamic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class IslamicDetectedLocation(
    val districtBn: String,
    val districtEn: String,
    val latitude: Double,
    val longitude: Double,
    val offsetMinutes: Int,
    val cityNameEn: String,
    val cityNameBn: String,
    val isBangladesh: Boolean,
    val distanceKm: Double
)

object IslamicLocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Fetches current high-accuracy device location via Google Play Services Location (FusedLocationProviderClient).
     */
    suspend fun detectCurrentLocation(context: Context): Result<IslamicDetectedLocation> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Result.failure(SecurityException("Location permission not granted"))
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Try getting fresh current location with 10-second timeout
        val location = withTimeoutOrNull(10000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }

                try {
                    val locationRequest = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(60000)
                        .setDurationMillis(9000)
                        .build()

                    fusedLocationClient.getCurrentLocation(locationRequest, cancellationTokenSource.token)
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                if (continuation.isActive) continuation.resume(loc)
                            } else {
                                // Fallback to last known location
                                fusedLocationClient.lastLocation
                                    .addOnSuccessListener { lastLoc ->
                                        if (continuation.isActive) continuation.resume(lastLoc)
                                    }
                                    .addOnFailureListener {
                                        if (continuation.isActive) continuation.resume(null)
                                    }
                            }
                        }
                        .addOnFailureListener {
                            // Fallback to last known location on error
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (continuation.isActive) continuation.resume(lastLoc)
                                }
                                .addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                        }
                } catch (e: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        } ?: run {
            // If high accuracy timed out, try quick lastLocation
            try {
                suspendCancellableCoroutine<Location?> { cont ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                }
            } catch (e: Exception) {
                null
            }
        }

        if (location == null) {
            return@withContext Result.failure(Exception("Unable to retrieve device GPS location. Please ensure Location/GPS is turned on."))
        }

        val lat = location.latitude
        val lon = location.longitude

        // Reverse geocoding to find city / district name
        val geocodedInfo = getAddressFromCoordinates(context, lat, lon)

        // Find closest Bangladesh district
        var closestDistrict = allBdDistrictsList.first()
        var minDistance = Float.MAX_VALUE

        val results = FloatArray(1)
        for (district in allBdDistrictsList) {
            Location.distanceBetween(lat, lon, district.lat, district.lon, results)
            val dist = results[0]
            if (dist < minDistance) {
                minDistance = dist
                closestDistrict = district
            }
        }

        val distanceKm = (minDistance / 1000.0)
        // If within 250km of Bangladesh boundaries or geocoder says Bangladesh
        val isBangladesh = distanceKm < 250.0 || (geocodedInfo?.countryCode.equals("BD", ignoreCase = true))

        val detectedCityEn = geocodedInfo?.locality
            ?: geocodedInfo?.subAdminArea
            ?: geocodedInfo?.adminArea
            ?: closestDistrict.nameEn

        val detectedResult = IslamicDetectedLocation(
            districtBn = if (isBangladesh) closestDistrict.nameBn else detectedCityEn,
            districtEn = if (isBangladesh) closestDistrict.nameEn else detectedCityEn,
            latitude = lat,
            longitude = lon,
            offsetMinutes = if (isBangladesh) closestDistrict.offsetMinutes else 0,
            cityNameEn = detectedCityEn,
            cityNameBn = if (isBangladesh) closestDistrict.nameBn else detectedCityEn,
            isBangladesh = isBangladesh,
            distanceKm = distanceKm
        )

        Result.success(detectedResult)
    }

    private suspend fun getAddressFromCoordinates(context: Context, lat: Double, lon: Double): Address? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) {
                                    continuation.resume(addresses.firstOrNull())
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        })
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lon, 1)
                list?.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
