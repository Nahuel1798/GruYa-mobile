package com.example.gruya

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthEventBusTest {

    @Test
    fun emit_forceLogout_isCollectedByEvents() = runBlocking {
        val bus = AuthEventBus()
        bus.emit(AuthEvent.ForceLogout)
        val event = bus.events.first()
        assertEquals(AuthEvent.ForceLogout, event)
    }
}
