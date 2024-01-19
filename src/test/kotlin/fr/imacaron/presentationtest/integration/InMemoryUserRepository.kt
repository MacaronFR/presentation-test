package fr.imacaron.presentationtest.integration

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.exception.NotFoundException

class InMemoryUserRepository: UserRepository() {
    private val map = mutableMapOf<Long, User>()
    override fun get(id: Long): User? = map[id]

    override fun set(id: Long, user: User) {
        map[id] = user
    }

    override fun delete(id: Long): Result<User> {
        return map.remove(id)?.let {
            Result.success(it)
        } ?: Result.failure(NotFoundException("User not found"))
    }

    override fun get(name: String): User? = map.values.find { it.name == name }

    override fun plusAssign(user: UserCreation) {
        map[lastId + 1] = User(lastId + 1, user.name, user.scope)
        lastId++
    }

    fun clear() {
        map.clear()
        lastId = 0
    }
}