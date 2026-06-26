package com.example.gruya.data.repository

import android.util.Log
import com.example.gruya.data.SessionManager
import com.example.gruya.data.remote.Constants
import com.example.gruya.domain.model.Location
import com.example.gruya.domain.model.TrackingState
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepository @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var hubConnection: HubConnection? = null
    private var connectionJob: Job? = null
    private var currentSessionId: String? = null
    private var currentIsProvider: Boolean = false

    private val _trackingState = MutableStateFlow<TrackingState>(TrackingState.Idle)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _locationUpdates = MutableSharedFlow<Location>(replay = 1, extraBufferCapacity = 10)
    val locationUpdates: SharedFlow<Location> = _locationUpdates.asSharedFlow()

    private val _sessionEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionEnded: SharedFlow<Unit> = _sessionEnded.asSharedFlow()

    fun connect(sessionId: String, isProvider: Boolean = false) {
        currentSessionId = sessionId
        currentIsProvider = isProvider
        
        val existingConnection = hubConnection
        val currentState = existingConnection?.connectionState
        
        if (currentState == HubConnectionState.CONNECTED) {
            Log.w("TrackingRepository", "Already connected. Ensuring group join for session: $sessionId")
            scope.launch {
                try {
                    existingConnection.send("WatchSession", sessionId)
                    if (isProvider) {
                        existingConnection.send("StartTracking", sessionId)
                        _trackingState.value = TrackingState.Tracking
                    } else {
                        _trackingState.value = TrackingState.Connected(sessionId)
                    }
                } catch (e: Exception) {
                    Log.e("TrackingRepository", "Failed to send session join on existing connection", e)
                }
            }
            return
        }

        if (currentState == HubConnectionState.CONNECTING) {
            Log.w("TrackingRepository", "Connection already in progress. Ignoring request for session: $sessionId")
            return
        }

        connectionJob?.cancel()
        connectionJob = scope.launch {
            _trackingState.value = TrackingState.Connecting

            // Ensure any previous connection is stopped
            existingConnection?.let { conn ->
                try {
                    Log.d("TrackingRepository", "Stopping existing connection...")
                    conn.stop().timeout(5, TimeUnit.SECONDS).blockingAwait()
                } catch (e: Exception) {
                    Log.e("TrackingRepository", "Error stopping previous connection", e)
                }
            }

            try {
                val baseUrl = Constants.BASE_URL.trimEnd('/')
                val hubUrl = "$baseUrl/locationHub"
                val jwt = sessionManager.getJwt()

                Log.d("TrackingRepository", "Building connection to $hubUrl")

                val newConnection = HubConnectionBuilder.create(hubUrl)
                    .withAccessTokenProvider(Single.just(jwt))
                    .build()
                
                hubConnection = newConnection

                // Listen for location updates from server. The hub sends one Location payload.
                newConnection.on("LocationUpdated", { location: Location ->
                    Log.d("TrackingRepository", "Location received: ${location.latitude}, ${location.longitude}")
                    scope.launch {
                        _locationUpdates.emit(location)
                    }
                }, Location::class.java)

                // Listen for session ended
                newConnection.on("SessionEnded") {
                    Log.d("TrackingRepository", "Session ended")
                    scope.launch {
                        _sessionEnded.emit(Unit)
                        _trackingState.value = TrackingState.Disconnected
                    }
                }

                newConnection.onClosed { exception ->
                    Log.d("TrackingRepository", "Connection closed: ${exception?.message}")
                    val sessionIdToReconnect = currentSessionId
                    if (sessionIdToReconnect != null && _trackingState.value != TrackingState.Disconnected) {
                        Log.d("TrackingRepository", "Unexpectedly disconnected. Attempting to reconnect...")
                        scope.launch {
                            delay(5000)
                            connect(sessionIdToReconnect, currentIsProvider)
                        }
                    } else if (_trackingState.value != TrackingState.Disconnected) {
                        _trackingState.value = TrackingState.Disconnected
                    }
                }

                var retryCount = 0
                val maxRetries = 5
                var connectedSuccessfully = false

                while (retryCount < maxRetries && !connectedSuccessfully) {
                    try {
                        Log.d("TrackingRepository", "Connecting to hub (attempt ${retryCount + 1})...")
                        newConnection.start()
                            .timeout(30, TimeUnit.SECONDS)
                            .blockingAwait()
                        
                        connectedSuccessfully = true
                        Log.d("TrackingRepository", "Connected successfully")

                        // Join session group
                        newConnection.send("WatchSession", sessionId)
                        Log.d("TrackingRepository", "Joined session: $sessionId")

                        // Only providers register as active trackers (send location)
                        if (isProvider) {
                            newConnection.send("StartTracking", sessionId)
                            Log.d("TrackingRepository", "Started tracking session: $sessionId")
                            _trackingState.value = TrackingState.Tracking
                        } else {
                            _trackingState.value = TrackingState.Connected(sessionId)
                        }
                    } catch (e: Exception) {
                        retryCount++
                        Log.e("TrackingRepository", "Connection attempt $retryCount failed", e)
                        if (retryCount >= maxRetries) {
                            _trackingState.value = TrackingState.Error("No se pudo conectar al servidor de rastreo.")
                        } else {
                            delay(2000L * retryCount)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TrackingRepository", "Failed to build hub connection", e)
                _trackingState.value = TrackingState.Error("Error: ${e.message}")
            }
        }
    }

    fun sendLocation(latitude: Double, longitude: Double) {
        val location = Location(latitude, longitude)
        _locationUpdates.tryEmit(location)

        val connection = hubConnection ?: return
        if (connection.connectionState != HubConnectionState.CONNECTED) return

        scope.launch {
            try {
                // Send a single Location object matching the server model (Latitude, Longitude)
                connection.send("UpdateLocation", location)
                Log.d("TrackingRepository", "Location sent: $latitude, $longitude")
            } catch (e: Exception) {
                Log.e("TrackingRepository", "Failed to send location", e)
            }
        }
    }

    fun disconnect() {
        currentSessionId = null
        val connection = hubConnection
        hubConnection = null
        connectionJob?.cancel()
        scope.launch {
            try {
                connection?.stop()?.timeout(5, TimeUnit.SECONDS)?.blockingAwait()
                Log.d("TrackingRepository", "Disconnected from hub")
            } catch (e: Exception) {
                Log.e("TrackingRepository", "Error disconnecting", e)
            } finally {
                _trackingState.value = TrackingState.Disconnected
            }
        }
    }

    fun cleanup() {
        disconnect()
    }
}
