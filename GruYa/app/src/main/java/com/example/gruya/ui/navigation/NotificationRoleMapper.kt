package com.example.gruya.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.example.gruya.domain.model.Role
import com.example.gruya.ui.screens.provider_quotes.ProviderQuoteFilter

/**
 * Maps a notification event type string to the required user role.
 * Returns null for unknown/unsupported event types.
 */
fun getRequiredRole(eventType: String): Role? = when (eventType) {
    "new_assistance", "directed_assistance", "quote_accepted_provider", "quote_rejected" -> Role.PROVIDER
    "new_quote", "quote_accepted_client", "trip_started", "provider.arrived", "provider.heading_to_destination", "provider.service_completed" -> Role.USER
    else -> null
}

/**
 * Converts a raw notification type + assistance ID into the corresponding typed NavEvent.
 * Returns null for unknown event types.
 */
fun navEventFromExtras(type: String, assistanceId: Int, trackingSessionId: String? = null): NavEvent? = when (type) {
    "new_assistance" -> NavEvent.NewAssistance(assistanceId)
    "directed_assistance" -> NavEvent.DirectedAssistance(assistanceId)
    "new_quote" -> NavEvent.NewQuote(assistanceId)
    "quote_accepted_provider" -> NavEvent.QuoteAcceptedProvider(assistanceId)
    "quote_accepted_client" -> NavEvent.QuoteAcceptedClient(assistanceId)
    "quote_rejected" -> NavEvent.QuoteRejected(assistanceId)
    "trip_started" -> {
        val sessionId = trackingSessionId ?: return null
        NavEvent.TripStarted(assistanceId, sessionId)
    }
    "provider.arrived" -> NavEvent.ProviderArrived(assistanceId)
    "provider.heading_to_destination" -> NavEvent.ProviderHeadingToDestination(assistanceId)
    "provider.service_completed" -> NavEvent.ServiceCompleted(assistanceId)
    else -> null
}

/** Intent extra key for the notification event type (String). */
const val EXTRA_NAV_TYPE = "nav_event_type"

/** Intent extra key for the assistance ID (Int). */
const val EXTRA_ASSISTANCE_ID = "nav_assistance_id"

/** Intent extra key for the tracking session ID (String). */
const val EXTRA_TRACKING_SESSION_ID = "trackingSessionId"

/**
 * Parses a tracking session ID from a notification's JSON data payload.
 * Checks both "trackingSessionId" and "TrackingSessionId" keys for compatibility.
 */
fun parseTrackingSessionId(dataJson: String?): String? {
    if (dataJson == null) return null
    return try {
        val json = org.json.JSONObject(dataJson)
        json.optString("trackingSessionId", "")
            .ifEmpty { json.optString("TrackingSessionId", "").ifEmpty { null } }
    } catch (e: Exception) {
        null
    }
}

fun destForEvent(event: NavEvent, assistanceId: Int): NavKey = when (event) {
    is NavEvent.NewAssistance -> AppDest.Quote(assistanceId)
    is NavEvent.DirectedAssistance -> AppDest.Quote(assistanceId)
    is NavEvent.NewQuote -> AppDest.TabKey.QuotesList(assistanceId)
    is NavEvent.QuoteAcceptedProvider -> AppDest.AssistanceTracking(assistanceId)
    is NavEvent.QuoteAcceptedClient -> AppDest.TabKey.QuotesList(assistanceId)
    is NavEvent.QuoteRejected -> AppDest.TabKey.ProviderQuotes(ProviderQuoteFilter.FINALIZADAS)
    is NavEvent.TripStarted -> AppDest.TabKey.QuotesList(assistanceId)
    is NavEvent.ProviderArrived -> AppDest.TabKey.QuotesList(assistanceId)
    is NavEvent.ProviderHeadingToDestination -> AppDest.TabKey.QuotesList(assistanceId)
    is NavEvent.ServiceCompleted -> AppDest.TabKey.QuotesList(assistanceId)
}
