package fr.imacaron.presentationtest.utils

import io.ktor.client.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

//suspend inline fun <reified T> HttpResponse.body() = Json.decodeFromString<T>(bodyAsText())