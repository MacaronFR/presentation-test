package fr.imacaron.presentationtest.core.type

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val name: String,
    val scope: Int
)
