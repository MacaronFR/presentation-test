package fr.imacaron.presentationtest

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.core.service.UserService
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.router.UserRouter
import fr.imacaron.presentationtest.sql.SQLUserRepository
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.Resources
import org.ktorm.database.Database
import org.ktorm.support.mysql.MySqlDialect
import org.slf4j.event.Level

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    installAuth()
    installContentNegotiation()
    installCallLogging()
    installResources()
    val database = Database.connect("jdbc:mariadb://localhost:3306/test", driver = "org.mariadb.jdbc.Driver", dialect = MySqlDialect(), user = "root", password = "secret")
    val userRepository = SQLUserRepository(database)
    val userService = UserService(userRepository)
    UserRouter(userService).apply { this@module.route() }
}

const val authName = "auth"

fun Application.installAuth() {
    install(Authentication) {
        bearer(authName) {
            realm = "Présentation Test"
            authenticate { tokenCredential ->
                UserIdPrincipal(tokenCredential.token)
            }
        }
    }
}

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.installCallLogging() {
    install(CallLogging) {
        level = Level.INFO
    }
}

fun Application.installResources() {
    install(Resources)
}