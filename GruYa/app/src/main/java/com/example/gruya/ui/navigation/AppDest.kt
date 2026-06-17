package com.example.gruya.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDest : NavKey {
    val requieresAuth: Boolean

    @Serializable
    data object Login : AppDest {
        override val requieresAuth = false
    }

    @Serializable
    data object Register: AppDest {
        override val requieresAuth = false
    }

    @Serializable
    data object ProviderProfile : AppDest {
        override val requieresAuth = false
    }

    @Serializable
    data class LocationPicker(val initialLat: Double? = null, val initialLng: Double? = null) : AppDest {
        override val requieresAuth = false
    }

    @Serializable
    data class AddVehicle(val vehicleId: Int? = null) : AppDest {
        override val requieresAuth = true
    }

    @Serializable
    data class RequestAssistance(val providerId: Int? = null, val serviceType: String? = null) : AppDest {
        override val requieresAuth = true
    }

    @Serializable
    data class MapPicker(val isDestination: Boolean, val initialLat: Double? = null, val initialLng: Double? = null) : AppDest {
        override val requieresAuth = true
    }

    @Serializable
    data class Quote(val assistanceId: Int) : AppDest {
        override val requieresAuth = true
    }

    @Serializable
    data object MainContent : AppDest {
        override val requieresAuth = false
    }

    @Serializable
    sealed interface TabKey : NavKey {
        @Serializable
        data object Home : TabKey
        @Serializable
        data object Vehicles : TabKey
        @Serializable
        data object Favourites : TabKey
        @Serializable
        data object Profile : TabKey
    }


}