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
    data object MainContent : AppDest {
        override val requieresAuth = false
    }

    @Serializable
    sealed interface TabKey : NavKey {
        @Serializable
        data object Home : TabKey
        @Serializable
        data object Vehicle : TabKey
        @Serializable
        data object Favourites : TabKey
        @Serializable
        data object Profile : TabKey
    }


}