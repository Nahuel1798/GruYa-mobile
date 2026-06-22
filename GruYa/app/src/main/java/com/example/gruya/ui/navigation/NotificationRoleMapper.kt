package com.example.gruya.ui.navigation

import com.example.gruya.domain.model.Role

/**
 * Maps a notification event type string to the required user role.
 * Returns null for unknown/unsupported event types.
 */
fun getRequiredRole(eventType: String): Role? = when (eventType) {
    "new_assistance", "directed_assistance", "quote_accepted_provider", "quote_rejected" -> Role.PROVIDER
    "new_quote", "quote_accepted_client" -> Role.USER
    else -> null
}

/**
 * Converts a raw notification type + assistance ID into the corresponding typed NavEvent.
 * Returns null for unknown event types.
 */
fun navEventFromExtras(type: String, assistanceId: Int): NavEvent? = when (type) {
    "new_assistance" -> NavEvent.NewAssistance(assistanceId)
    "directed_assistance" -> NavEvent.DirectedAssistance(assistanceId)
    "new_quote" -> NavEvent.NewQuote(assistanceId)
    "quote_accepted_provider" -> NavEvent.QuoteAcceptedProvider(assistanceId)
    "quote_accepted_client" -> NavEvent.QuoteAcceptedClient(assistanceId)
    "quote_rejected" -> NavEvent.QuoteRejected(assistanceId)
    else -> null
}

/** Intent extra key for the notification event type (String). */
const val EXTRA_NAV_TYPE = "nav_event_type"

/** Intent extra key for the assistance ID (Int). */
const val EXTRA_ASSISTANCE_ID = "nav_assistance_id"
