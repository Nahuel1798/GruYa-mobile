package com.example.gruya.utils

import com.example.gruya.domain.model.Location
import org.maplibre.spatialk.geojson.Position
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

object LocationUtils {

    fun parseRouteGeometry(routeGeometry: String): List<Position> {
        if (routeGeometry.isBlank()) return emptyList()
        val trimmed = routeGeometry.trim()
        if (trimmed == "null" || trimmed == "[]") return emptyList()
        
        return try {
            if (trimmed.startsWith("[")) {
                val json = JSONArray(trimmed)
                val result = mutableListOf<Position>()
                for (i in 0 until json.length()) {
                    val coord = json.optJSONArray(i)
                    if (coord != null && coord.length() >= 2) {
                        result.add(
                            Position(
                                longitude = coord.getDouble(0),
                                latitude = coord.getDouble(1)
                            )
                        )
                    }
                }
                result
            } else if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)
                val geometry = when (json.optString("type")) {
                    "Feature" -> json.optJSONObject("geometry")
                    "FeatureCollection" -> {
                        val features = json.optJSONArray("features")
                        if (features != null && features.length() > 0) {
                            features.optJSONObject(0)?.optJSONObject("geometry")
                        } else null
                    }
                    else -> json
                }

                val coords = geometry?.optJSONArray("coordinates")
                if (coords != null) {
                    val result = mutableListOf<Position>()
                    for (i in 0 until coords.length()) {
                        val coord = coords.optJSONArray(i)
                        if (coord != null && coord.length() >= 2) {
                            result.add(
                                Position(
                                    longitude = coord.getDouble(0),
                                    latitude = coord.getDouble(1)
                                )
                            )
                        }
                    }
                    result
                } else emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calculateBearing(start: Location, end: Location): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val deltaLon = lon2 - lon1

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }

    fun calculateDistance(loc1: Location, loc2: Location): Double {
        val r = 6371e3 // metres
        val phi1 = Math.toRadians(loc1.latitude)
        val phi2 = Math.toRadians(loc2.latitude)
        val deltaPhi = Math.toRadians(loc2.latitude - loc1.latitude)
        val deltaLambda = Math.toRadians(loc2.longitude - loc1.longitude)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    fun distanceToSegment(p: Location, a: Position, b: Position): Double {
        val lat = p.latitude
        val lon = p.longitude
        val lat1 = a.latitude
        val lon1 = a.longitude
        val lat2 = b.latitude
        val lon2 = b.longitude

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        if (dLat == 0.0 && dLon == 0.0) {
            return calculateDistance(p, Location(lat1, lon1))
        }

        val t = ((lon - lon1) * dLon + (lat - lat1) * dLat) / (dLon * dLon + dLat * dLat)

        return when {
            t < 0.0 -> calculateDistance(p, Location(lat1, lon1))
            t > 1.0 -> calculateDistance(p, Location(lat2, lon2))
            else -> {
                val projLat = lat1 + t * dLat
                val projLon = lon1 + t * dLon
                calculateDistance(p, Location(projLat, projLon))
            }
        }
    }

    fun isDeviated(location: Location, polyline: List<Position>, thresholdMeters: Double = 50.0): Boolean {
        if (polyline.size < 2) return false
        
        var minDistance = Double.MAX_VALUE
        for (i in 0 until polyline.size - 1) {
            val distance = distanceToSegment(location, polyline[i], polyline[i + 1])
            if (distance < minDistance) {
                minDistance = distance
            }
            if (minDistance <= thresholdMeters) return false
        }
        
        return minDistance > thresholdMeters
    }
}
