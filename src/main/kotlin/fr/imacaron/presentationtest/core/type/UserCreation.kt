package fr.imacaron.presentationtest.core.type

import kotlinx.serialization.Serializable

@Serializable
data class UserCreation(
    val name: String,
    val scope: Int
)
