package com.saif.scheduleapp.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class TaskFormRoute(val taskId: Long? = null)

@Serializable
data object ImportRoute
