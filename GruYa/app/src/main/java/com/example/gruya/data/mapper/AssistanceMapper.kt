package com.example.gruya.data.mapper

import com.example.gruya.data.remote.dtos.response.AssistanceResponse
import com.example.gruya.domain.model.Assistance

fun AssistanceResponse.toDomain(): Assistance = Assistance(
    id = id,
    serviceType = serviceType,
    issueType = issueType,
    status = status,
    vehicle = vehicle.toDomain(),
    origin = origin,
    destination = destination,
    isDirected = isDirected,
    createdAt = createdAt,
    clientName = runCatching { "${client.firstName} ${client.lastName}" }.getOrDefault(""),
    routeGeometry = routeGeometry
)

fun List<AssistanceResponse>.toDomain(): List<Assistance> = map { it.toDomain() }
