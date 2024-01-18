package fr.imacaron.presentationtest.core.service

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.core.type.UserUpdate
import fr.imacaron.presentationtest.exception.IllegalCallerException
import fr.imacaron.presentationtest.exception.NotFoundException

class UserService(
    private val userRepository: UserRepository
) {

    private var caller: User? = null

    fun with(user: User): UserService {
        caller = user
        return this
    }

    fun getUser(id: Long): User? = userRepository[id]

    fun updateUser(id: Long, update: UserUpdate): Result<User> {
        val caller = this.caller ?: throw IllegalCallerException("Caller must be set using with function")
        val user = userRepository[id] ?: return Result.failure(NotFoundException("User not found"))
        if(caller.scope < user.scope || caller.scope < update.scope) {
            return Result.failure(IllegalCallerException("User have not the right to do that"))
        }
        val updatedUser = user.copy(name = update.name, scope = update.scope)
        userRepository[id] = updatedUser
        return Result.success(updatedUser)
    }

    fun createUser(user: UserCreation): Result<User> {
        val caller = this.caller ?: throw IllegalCallerException("Caller must be set using with function")
        if(userRepository[user.name] != null) {
            return Result.failure(IllegalArgumentException("User with name \"${user.name}\" already exist"))
        }
        if(caller.scope < user.scope) {
            return Result.failure(IllegalCallerException("User have not the right to do that"))
        }
        userRepository += user
        return Result.success(User(userRepository.lastId, user.name, user.scope))
    }
}