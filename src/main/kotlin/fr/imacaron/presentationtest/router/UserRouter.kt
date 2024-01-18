package fr.imacaron.presentationtest.router

import fr.imacaron.presentationtest.authName
import fr.imacaron.presentationtest.core.service.UserService
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.core.type.UserUpdate
import fr.imacaron.presentationtest.exception.IllegalCallerException
import fr.imacaron.presentationtest.exception.NotFoundException
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.put
import io.ktor.server.resources.post

class UserRouter(
    private val userService: UserService,
) {

    fun Application.route() {
        routing {
            authenticate(authName) {
                getSelf()
                updateUser()
                createUser()
            }
        }
    }

    private fun Route.getSelf() {
        get<User.Me> {
            val user = userService.getUser(call.principal<UserIdPrincipal>()!!.name.toLong()) ?: return@get
            call.respond(user)
        }
    }

    private fun Route.updateUser() {
        put<User.Id> { userId ->
            val update: UserUpdate = call.receive()
            val caller = userService.getUser(call.principal<UserIdPrincipal>()!!.name.toLong()) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@put
            }
            val result = userService.with(caller).updateUser(userId.id, update)
            result.onSuccess {
                call.respond(it)
            }
            result.onFailure {
                when(it) {
                    is NotFoundException -> call.respond(HttpStatusCode.NotFound)
                    is IllegalCallerException -> call.respond(HttpStatusCode.Forbidden)
                }
            }
        }
    }

    private fun Route.createUser() {
        post<User> {
            val creation: UserCreation = call.receive()
            val caller = userService.getUser(call.principal<UserIdPrincipal>()!!.name.toLong()) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            val result = userService.with(caller).createUser(creation)
            result.onSuccess {
                call.respond(HttpStatusCode.Created, it)
            }
            result.onFailure {
                when(it) {
                    is NotFoundException -> call.respond(HttpStatusCode.NotFound)
                    is IllegalCallerException -> call.respond(HttpStatusCode.Unauthorized)
                    is IllegalArgumentException -> call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }

    @Resource("/user")
    class User {
        @Resource("me")
        class Me(val parent: User = User())

        @Resource("{id}")
        class Id(val parent: User = User(), val id: Long)
    }
}