package com.example.gruya.data.service

import com.example.gruya.domain.model.Role
import com.example.gruya.ui.navigation.NavEvent
import com.example.gruya.ui.navigation.getRequiredRole
import com.example.gruya.ui.navigation.navEventFromExtras

/**
 * Pure result of evaluating a notification for user session, role, and type.
 *
 * @property shouldDrop if true, the notification should be silently dropped.
 * @property navEvent if non-null, the typed navigation event to emit.
 * @property showNotification whether to show a system notification.
 */
data class GuardResult(
    val shouldDrop: Boolean,
    val navEvent: NavEvent?,
    val showNotification: Boolean
)

/**
 * Pure function that evaluates session guard, role guard, and type mapping
 * for an incoming FCM notification. This is the core logic extracted from
 * [GruYaFirebaseService] so it can be unit tested without Android dependencies.
 *
 * @param jwt the user's JWT token (empty string if not logged in).
 * @param userRole the user's current role.
 * @param type the notification event type from the FCM data payload.
 * @param assistanceIdStr the assistance ID as a string from the FCM data payload.
 * @return [GuardResult] indicating whether to drop, show notification, and/or emit a NavEvent.
 */
fun evaluateNotification(
    jwt: String,
    userRole: Role?,
    type: String?,
    assistanceIdStr: String?
): GuardResult {
    // Session guard
    if (jwt.isBlank()) return GuardResult(shouldDrop = true, navEvent = null, showNotification = false)

    // Role guard
    val requiredRole = type?.let { getRequiredRole(it) }
    if (requiredRole == null) {
        // Unknown type — show notification but no NavEvent
        return GuardResult(shouldDrop = false, navEvent = null, showNotification = true)
    }
    if (userRole != requiredRole) {
        // Role mismatch — silent drop
        return GuardResult(shouldDrop = true, navEvent = null, showNotification = false)
    }

    // Build NavEvent
    val assistanceId = assistanceIdStr?.toIntOrNull() ?: -1
    val navEvent = type?.let { navEventFromExtras(it, assistanceId) }
    return GuardResult(shouldDrop = false, navEvent = navEvent, showNotification = true)
}
