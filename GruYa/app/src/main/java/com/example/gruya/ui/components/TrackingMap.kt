package com.example.gruya.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

private const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

@Composable
fun TrackingMap(
    origin: Location,
    destination: Location,
    routeGeometry: String? = null,
    providerLocation: Location? = null,
    providerToOriginRoute: String? = null,
    providerToDestinationRoute: String? = null,
    isTracking: Boolean = false,
    isProvider: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(origin.longitude, origin.latitude),
            zoom = 13.0
        )
    )

    var previousLocation by remember { mutableStateOf<Location?>(null) }
    var currentBearing by remember { mutableDoubleStateOf(0.0) }

    // Trace the route if available
    val routePositions = remember(routeGeometry) {
        routeGeometry?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
    }

    val providerRoutePositions = remember(providerToOriginRoute) {
        providerToOriginRoute?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
    }

    val providerToDestPositions = remember(providerToDestinationRoute) {
        providerToDestinationRoute?.let { LocationUtils.parseRouteGeometry(it) } ?: emptyList()
    }

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
                    maxDelta > 1.0 -> 7.5
                    maxDelta > 0.5 -> 8.5
                    maxDelta > 0.2 -> 10.0
                    maxDelta > 0.1 -> 11.2
                    maxDelta > 0.05 -> 12.2
                    maxDelta > 0.02 -> 13.2
                    else -> 14.2
                }

                cameraState.animateTo(
                    CameraPosition(target = target, zoom = zoom)
                )
            }
        }
    }

    // Auto-zoom and street level tracking when the crane is moving
    LaunchedEffect(providerLocation, isTracking) {
        if (isTracking && providerLocation != null) {
            // Trim routes as the provider moves
            remainingRoute = trimPolyline(providerLocation, remainingRoute)
            remainingProviderRoute = trimPolyline(providerLocation, remainingProviderRoute)
            remainingProviderToDestRoute = trimPolyline(providerLocation, remainingProviderToDestRoute)

            val dist = previousLocation?.let { LocationUtils.calculateDistance(it, providerLocation) } ?: 0.0

            // Only update bearing if moving significantly to avoid jitter
            if (dist > 2.0 || previousLocation == null) {
                if (previousLocation != null) {
                    currentBearing = LocationUtils.calculateBearing(previousLocation!!, providerLocation)
                }
                previousLocation = providerLocation
            }

            cameraState.animateTo(
                CameraPosition(
                    target = Position(providerLocation.longitude, providerLocation.latitude),
                    zoom = if (cameraState.position.zoom < 15.0) 16.5 else cameraState.position.zoom,
                    bearing = currentBearing,
                    tilt = 45.0
                )
            )
        } else if (!isTracking) {
            // Reset rotation and routes when not tracking
            previousLocation = null
            currentBearing = 0.0
            remainingRoute = routePositions
            remainingProviderRoute = providerRoutePositions
            remainingProviderToDestRoute = providerToDestPositions
        }
    }


    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        cameraState = cameraState,
        baseStyle = if (isDarkTheme) BaseStyle.Uri(DARK_STYLE_URL) else BaseStyle.Uri(LIGHT_STYLE_URL)
    ) {
        val auxilioIcon = image(painterResource(R.drawable.ic_auxilio), drawAsSdf = true)
        val origenIcon = image(painterResource(R.drawable.ic_origin), drawAsSdf = true)
        val destinoIcon = image(painterResource(R.drawable.ic_destino), drawAsSdf = true)

        // Trace the route if available
        if (remainingRoute.isNotEmpty()) {
            key(remainingRoute) {
                val routeData = remember(remainingRoute) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(coordinates = remainingRoute),
                                    properties = null
                                )
                            )
                        )
                    )
                }
                val routeSource = rememberGeoJsonSource(data = routeData)

                // Route casing (border)
                LineLayer(
                    id = "assistance-route-casing",
                    source = routeSource,
                    color = const(Color.White),
                    width = const(9.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round)
                )

                LineLayer(
                    id = "assistance-route",
                    source = routeSource,
                    color = const(MaterialTheme.colorScheme.primary),
                    width = const(6.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round)
                )
            }
        }

        // Trace provider to origin route
        if (remainingProviderRoute.isNotEmpty()) {
            key(remainingProviderRoute) {
                val providerRouteData = remember(remainingProviderRoute) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(coordinates = remainingProviderRoute),
                                    properties = null
                                )
                            )
                        )
                    )
                }
                val providerRouteSource = rememberGeoJsonSource(data = providerRouteData)

                LineLayer(
                    id = "provider-to-origin-route",
                    source = providerRouteSource,
                    color = const(Color(0xFF3B82F6)),
                    width = const(6.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round),
                    dasharray = const(listOf(2f, 2f))
                )
            }
        }

        // Trace provider to destination route
        if (remainingProviderToDestRoute.isNotEmpty()) {
            key(remainingProviderToDestRoute) {
                val providerToDestData = remember(remainingProviderToDestRoute) {
                    GeoJsonData.Features(
                        geoJson = FeatureCollection(
                            features = listOf(
                                Feature(
                                    geometry = LineString(coordinates = remainingProviderToDestRoute),
                                    properties = null
                                )
                            )
                        )
                    )
                }
                val providerToDestSource = rememberGeoJsonSource(data = providerToDestData)

                // Route casing (border)
                LineLayer(
                    id = "provider-to-destination-route-casing",
                    source = providerToDestSource,
                    color = const(Color.White),
                    width = const(9.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round)
                )

                LineLayer(
                    id = "provider-to-destination-route",
                    source = providerToDestSource,
                    color = const(MaterialTheme.colorScheme.secondary),
                    width = const(6.dp),
                    join = const(LineJoin.Round),
                    cap = const(LineCap.Round)
                )
            }
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
            key(location) {
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
}

private fun trimPolyline(point: Location, polyline: List<Position>, threshold: Double = 30.0): List<Position> {
    if (polyline.size < 2) return polyline

    var closestIndex = -1
    var minDistance = Double.MAX_VALUE

    for (i in 0 until polyline.size - 1) {
        val distance = LocationUtils.distanceToSegment(point, polyline[i], polyline[i + 1])
        if (distance < minDistance) {
            minDistance = distance
            closestIndex = i
        }
    }

    return if (closestIndex != -1 && minDistance <= threshold) {
        polyline.drop(closestIndex + 1)
    } else {
        polyline
    }
}
