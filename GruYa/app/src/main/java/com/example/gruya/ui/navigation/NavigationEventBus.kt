package com.example.gruya.ui.navigation

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationEventBus @Inject constructor() {

    private val _notificationEvents = MutableSharedFlow<NavEvent>(
        extraBufferCapacity = 1
    )
    val notificationEvents: SharedFlow<NavEvent> = _notificationEvents.asSharedFlow()

    private val _navigationEvents = MutableSharedFlow<NavEvent>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val navigationEvents: SharedFlow<NavEvent> = _navigationEvents.asSharedFlow()

    @Volatile
    var isForeground: Boolean = false

    fun emitNotification(event: NavEvent) {
        if (!_notificationEvents.tryEmit(event)) {
            Log.w("NavEventBus", "Dropped notification event (buffer full): $event")
        }
    }

    fun emitNavigation(event: NavEvent) {
        if (!_navigationEvents.tryEmit(event)) {
            Log.w("NavEventBus", "Dropped navigation event (buffer full): $event")
        }
    }
}
