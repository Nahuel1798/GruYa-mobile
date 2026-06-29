package com.example.gruya.ui.navigation

sealed interface NavEvent {
    val assistanceId: Int

    data class NewAssistance(override val assistanceId: Int) : NavEvent
    data class DirectedAssistance(override val assistanceId: Int) : NavEvent
    data class NewQuote(override val assistanceId: Int) : NavEvent
    data class QuoteAcceptedProvider(override val assistanceId: Int) : NavEvent
    data class QuoteAcceptedClient(override val assistanceId: Int) : NavEvent
    data class QuoteRejected(override val assistanceId: Int) : NavEvent
    data class TripStarted(override val assistanceId: Int, val trackingSessionId: String) : NavEvent
    data class ProviderArrived(override val assistanceId: Int) : NavEvent
    data class ProviderHeadingToDestination(override val assistanceId: Int) : NavEvent
    data class ServiceCompleted(override val assistanceId: Int) : NavEvent
}
