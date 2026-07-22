package com.example.gruya.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gruya.R
import com.example.gruya.domain.model.Location
import com.example.gruya.utils.LocationUtils
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.*
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@Composable
fun TrackingMap(
    origin: Location,
    destination: Location,
    modifier: Modifier = Modifier,
    routePositions: List<Position> = emptyList(),
    providerLocation: Location? = null,
    providerRoutePositions: List<Position> = emptyList(),
    providerToDestPositions: List<Position> = emptyList(),
    isTracking: Boolean = false,
    isProvider: Boolean = false
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(origin.longitude, origin.latitude),
            zoom = 15.0
        )
    )

    var previousLocation by remember { mutableStateOf<Location?>(null) }
    var currentBearing by remember { mutableDoubleStateOf(0.0) }

    var remainingRoute by remember(routePositions) { mutableStateOf(routePositions) }
    var remainingProviderRoute by remember(providerRoutePositions) { mutableStateOf(providerRoutePositions) }
    var remainingProviderToDestRoute by remember(providerToDestPositions) { mutableStateOf(providerToDestPositions) }

    // Initial overview or when not tracking
    LaunchedEffect(origin, destination, routePositions, providerRoutePositions, providerToDestPositions) {
        if (!isTracking) {
            val points = mutableListOf<Position>()
            points.add(Position(origin.longitude, origin.latitude))
            if (destination.latitude != 0.0) {
                points.add(Position(destination.longitude, destination.latitude))
            }
            points.addAll(routePositions)
            points.addAll(providerRoutePositions)
            points.addAll(providerToDestPositions)

            if (points.isNotEmpty()) {
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLon = points.minOf { it.longitude }
                val maxLon = points.maxOf { it.longitude }

                val target = Position((minLon + maxLon) / 2.0, (minLat + maxLat) / 2.0)
                val deltaLat = maxLat - minLat
                val deltaLon = maxLon - minLon
                
                val maxDelta = maxOf(deltaLat, deltaLon)
                val zoom = when {
                    maxDelta > 2.0 -> 7.5
                    maxDelta > 1.0 -> 8.5
                    maxDelta > 0.5 -> 10.0
                    maxDelta > 0.2 -> 11.5
                    maxDelta > 0.1 -> 13.0
                    maxDelta > 0.05 -> 14.0
                    maxDelta > 0.02 -> 15.0
                    maxDelta > 0.01 -> 16.0
                    else -> 17.0
                }

                cameraState.animateTo(
                    CameraPosition(target = target, zoom = zoom),
                    duration = 1000.milliseconds
                )
            }
        }
    }

    // Auto-zoom and street level tracking when the crane is moving
    LaunchedEffect(providerLocation, isTracking) {
        if (isTracking && providerLocation != null) {
            val dist = previousLocation?.let { LocationUtils.calculateDistance(it, providerLocation) } ?: Double.MAX_VALUE

            // Optimization: Only update routes and camera if moved significantly (> 2 meters)
            // to reduce recompositions and improve performance.
            if (dist > 2.0 || previousLocation == null) {
                // Trim routes as the provider moves
                remainingRoute = trimPolyline(providerLocation, remainingRoute)
                remainingProviderRoute = trimPolyline(providerLocation, remainingProviderRoute)
                remainingProviderToDestRoute = trimPolyline(providerLocation, remainingProviderToDestRoute)

                // Only update bearing if moving significantly to avoid jitter
                if (dist > 5.0 || previousLocation == null) {
                    if (previousLocation != null) {
                        currentBearing = LocationUtils.calculateBearing(previousLocation!!, providerLocation)
                    }
                }
                previousLocation = providerLocation

                // Dynamic zoom and camera target based on role and distance to target
                val targetPoint = when {
                    remainingProviderRoute.isNotEmpty() -> remainingProviderRoute.last()
                    remainingProviderToDestRoute.isNotEmpty() -> remainingProviderToDestRoute.last()
                    else -> null
                }

                if (isProvider) {
                    val distanceToTarget = targetPoint?.let {
                        LocationUtils.calculateDistance(providerLocation, Location(it.latitude, it.longitude))
                    } ?: 0.0

                    // Provider (Grúa): Zoomed in for navigation, but slightly out if far to see the path
                    val targetZoom = when {
                        distanceToTarget > 5000 -> 14.5
                        distanceToTarget > 2000 -> 15.5
                        distanceToTarget > 1000 -> 16.5
                        else -> 18.0
                    }

                    cameraState.animateTo(
                        CameraPosition(
                            target = Position(providerLocation.longitude, providerLocation.latitude),
                            zoom = targetZoom,
                            bearing = currentBearing,
                            tilt = 55.0
                        ),
                        duration = 800.milliseconds
                    )
                } else {
                    // Client (Usuario): Wants to see both the provider and the target point (Origin/Destination)
                    if (targetPoint != null) {
                        val minLat = minOf(providerLocation.latitude, targetPoint.latitude)
                        val maxLat = maxOf(providerLocation.latitude, targetPoint.latitude)
                        val minLon = minOf(providerLocation.longitude, targetPoint.longitude)
                        val maxLon = maxOf(providerLocation.longitude, targetPoint.longitude)

                        val center = Position((minLon + maxLon) / 2.0, (minLat + maxLat) / 2.0)
                        val deltaLat = maxLat - minLat
                        val deltaLon = maxLon - minLon
                        val maxDelta = maxOf(deltaLat, deltaLon)

                        val zoom = when {
                            maxDelta > 2.0 -> 7.5
                            maxDelta > 1.0 -> 8.5
                            maxDelta > 0.5 -> 10.0
                            maxDelta > 0.2 -> 11.5
                            maxDelta > 0.1 -> 13.0
                            maxDelta > 0.05 -> 14.0
                            maxDelta > 0.02 -> 15.0
                            maxDelta > 0.01 -> 16.0
                            else -> 17.0
                        }

                        cameraState.animateTo(
                            CameraPosition(target = center, zoom = zoom, bearing = 0.0, tilt = 0.0),
                            duration = 800.milliseconds
                        )
                    } else {
                        // Fallback: Follow provider if no target is found
                        cameraState.animateTo(
                            CameraPosition(
                                target = Position(providerLocation.longitude, providerLocation.latitude),
                                zoom = 16.0,
                                bearing = 0.0,
                                tilt = 0.0
                            ),
                            duration = 800.milliseconds
                        )
                    }
                }
            }
        } else if (!isTracking) {
            // Reset rotation and routes when not tracking
            previousLocation = null
            currentBearing = 0.0
            remainingRoute = routePositions
            remainingProviderRoute = providerRoutePositions
            remainingProviderToDestRoute = providerToDestPositions
        }
    }


    // Visual improvement: Connect the provider location to the route for a smoother look.
    // This avoids the gap between the vehicle and the start of the route line.
    val displayRoute = remainingRoute

    val displayProviderRoute = remainingProviderRoute

    val displayProviderToDestRoute = remember(remainingProviderToDestRoute, providerLocation, isTracking) {
        connectProviderToRoute(providerLocation, remainingProviderToDestRoute, isTracking)
    }

    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        cameraState = cameraState,
        baseStyle = if (isDarkTheme) BaseStyle.Uri(DARK_STYLE_URL) else BaseStyle.Uri(LIGHT_STYLE_URL),
        options = MapOptions(
            ornamentOptions = OrnamentOptions(
                isCompassEnabled = false,
                isScaleBarEnabled = false,
                isLogoEnabled = false,
                isAttributionEnabled = false
            )
        )
    ) {
        val auxilioIcon = image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
        val origenIcon = image(painterResource(R.drawable.ic_origin), drawAsSdf = true)
        val destinoIcon = image(painterResource(R.drawable.ic_destino), drawAsSdf = true)

        // Optimized Route Source handling
        val providerRouteSource = rememberGeoJsonSource(
            data = remember(displayProviderRoute) {
                GeoJsonData.Features(
                    geoJson = FeatureCollection(
                        features = if (displayProviderRoute.size >= 2) listOf(
                            Feature(
                                geometry = LineString(coordinates = displayProviderRoute),
                                properties = null
                            )
                        ) else emptyList()
                    )
                )
            }
        )

        val providerToDestSource = rememberGeoJsonSource(
            data = remember(displayProviderToDestRoute) {
                GeoJsonData.Features(
                    geoJson = FeatureCollection(
                        features = if (displayProviderToDestRoute.size >= 2) listOf(
                            Feature(
                                geometry = LineString(coordinates = displayProviderToDestRoute),
                                properties = null
                            )
                        ) else emptyList()
                    )
                )
            }
        )

        // Trace the route if available
        if (displayRoute.size >= 2) {
            val routeSource = rememberGeoJsonSource(
                data = remember(displayRoute) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(coordinates = displayRoute),
                                    properties = null
                                )
                            )
                        )
                    )
                }
            )

            // Route casing (border)
            LineLayer(
                id = "assistance-route-casing",
                source = routeSource,
                color = const(Color.White),
                width = const(11.dp),
                join = const(LineJoin.Round),
                cap = const(LineCap.Round)
            )

            LineLayer(
                id = "assistance-route",
                source = routeSource,
                color = const(MaterialTheme.colorScheme.primary),
                width = const(8.dp),
                join = const(LineJoin.Round),
                cap = const(LineCap.Round)
            )
        }

        // Trace provider to origin route
        if (displayProviderRoute.size >= 2) {
            LineLayer(
                id = "provider-to-origin-route",
                source = providerRouteSource,
                color = const(Color(0xFF3B82F6)),
                width = const(8.dp),
                join = const(LineJoin.Round),
                cap = const(LineCap.Round),
                dasharray = const(listOf(2f, 2f))
            )
        }

        // Trace provider to destination route
        if (displayProviderToDestRoute.size >= 2) {
            // Route casing (border)
            LineLayer(
                id = "provider-to-destination-route-casing",
                source = providerToDestSource,
                color = const(Color.White),
                width = const(11.dp),
                join = const(LineJoin.Round),
                cap = const(LineCap.Round)
            )

            LineLayer(
                id = "provider-to-destination-route",
                source = providerToDestSource,
                color = const(MaterialTheme.colorScheme.secondary),
                width = const(8.dp),
                join = const(LineJoin.Round),
                cap = const(LineCap.Round)
            )
        }

        // Origin and Destination markers
        val markersSource = rememberGeoJsonSource(
            data = remember(origin, destination) {
                GeoJsonData.Features(
                    geoJson = FeatureCollection(
                        features = buildList {
                            add(
                                Feature(
                                    geometry = Point(Position(origin.longitude, origin.latitude)),
                                    properties = buildJsonObject { put("type", "origin") }
                                )
                            )
                            if (destination.latitude != 0.0) {
                                add(
                                    Feature(
                                        geometry = Point(Position(destination.longitude, destination.latitude)),
                                        properties = buildJsonObject { put("type", "destination") }
                                    )
                                )
                            }
                        }
                    )
                )
            }
        )

        SymbolLayer(
            id = "markers",
            source = markersSource,
            iconImage = switch(
                input = feature["type"].asString(),
                case("origin", origenIcon),
                case("destination", destinoIcon),
                fallback = origenIcon
            ),
            iconColor = const(MaterialTheme.colorScheme.primary),
            iconSize = const(1.3f),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconAnchor = const(SymbolAnchor.Bottom)
        )

        // Provider marker (The one being tracked) - Show on top
        providerLocation?.let { location ->
            val providerSource = rememberGeoJsonSource(
                data = remember(location) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = Point(Position(location.longitude, location.latitude)),
                                    properties = buildJsonObject { put("type", "provider") }
                                )
                            )
                        )
                    )
                }
            )

            SymbolLayer(
                id = "provider-marker-layer",
                source = providerSource,
                iconImage = auxilioIcon,
                iconColor = const(
                    if (isProvider)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.tertiary
                ),
                iconSize = const(1.5f),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                iconAnchor = const(SymbolAnchor.Center)
            )
        }
    }
}

private fun trimPolyline(point: Location, polyline: List<Position>, threshold: Double = 80.0): List<Position> {
    if (polyline.size < 2) return polyline

    // Optimization: Since the polyline is already being trimmed as we move, the provider
    // is usually near the beginning of the list. We check the first 100 segments
    // to handle cases where the provider might have moved significantly between updates.
    val searchLimit = minOf(polyline.size - 1, 100)
    var closestIndex = -1
    var minDistance = Double.MAX_VALUE

    for (i in 0 until searchLimit) {
        val distance = LocationUtils.distanceToSegment(point, polyline[i], polyline[i + 1])
        if (distance < minDistance) {
            minDistance = distance
            closestIndex = i
        }
        // Early exit if we found a very close segment (within 5 meters)
        if (minDistance < 5.0) break
    }

    return if (closestIndex != -1 && minDistance <= threshold) {
        polyline.drop(closestIndex + 1)
    } else {
        polyline
    }
}

private fun connectProviderToRoute(
    providerLocation: Location?,
    route: List<Position>,
    isTracking: Boolean
): List<Position> {
    if (isTracking && providerLocation != null && route.isNotEmpty()) {
        val first = route[0]
        val d = LocationUtils.calculateDistance(providerLocation, Location(first.latitude, first.longitude))
        // Increased threshold to handle sparse waypoints (e.g., long straight roads)
        // If the provider is within 1.5km of the next waypoint, we connect them to avoid gaps.
        return if (d < 1500.0) {
            listOf(Position(providerLocation.longitude, providerLocation.latitude)) + route
        } else {
            route
        }
    }
    return route
}
