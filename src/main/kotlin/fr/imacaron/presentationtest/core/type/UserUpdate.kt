package fr.imacaron.presentationtest.core.type

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdate(
    val name: String,
    val scope: Int
)
