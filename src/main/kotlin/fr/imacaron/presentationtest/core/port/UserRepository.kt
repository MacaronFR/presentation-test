package fr.imacaron.presentationtest.core.port

import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation

abstract class UserRepository {
    var lastId: Long = 0
        protected set

    abstract operator fun get(id: Long): User?

    abstract operator fun set(id: Long, user: User)

    abstract fun delete(id: Long): Result<User>

    abstract operator fun get(name: String): User?

    abstract operator fun plusAssign(user: UserCreation)
}