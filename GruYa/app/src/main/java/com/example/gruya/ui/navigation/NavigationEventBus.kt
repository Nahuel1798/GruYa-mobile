package com.example.gruya.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<NavEvent>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<NavEvent> = _events.asSharedFlow()

    @Volatile
    var isForeground: Boolean = false

    fun emit(event: NavEvent) {
        _events.tryEmit(event)
    }
}
