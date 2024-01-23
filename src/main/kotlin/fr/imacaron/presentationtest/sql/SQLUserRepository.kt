package fr.imacaron.presentationtest.sql

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.exception.NotFoundException
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

class SQLUserRepository(private val db: Database): UserRepository() {

    init {
        lastId = db.users.maxBy { it.id } ?: 0
    }

    override fun get(id: Long): User? {
        return db.users.find { it.id eq id }?.let { User(it) }
    }

    override fun set(id: Long, user: User) {
        db.users.find { it.id eq id }?.let {
            it.id = user.id
            it.scope = user.scope
            it.flushChanges()
        } ?: throw NotFoundException("User not found")
    }

    override fun delete(id: Long): Result<User> {
        val user = db.users.find { it.id eq id } ?: return Result.failure(NotFoundException("User not found"))
        user.delete()
        return Result.success(User(user))
    }

    override fun get(name: String): User? {
        return db.users.find { it.name eq name }?.let { User(it) }
    }

    override fun plusAssign(user: UserCreation) {
        val userEntity = UserEntity {
            id = lastId + 1
            name = user.name
            scope = user.scope
        }
        db.users.add(userEntity)
    }

    object Users: Table<UserEntity>("USERS") {
        val id = long("id").primaryKey().bindTo { it.id }
        val name = varchar("name").bindTo { it.name }
        val scope = int("scope").bindTo { it.scope }
    }

    private val User.entity
        get() = let { user ->
            UserEntity {
                id = user.id
                name = user.name
                scope = user.scope
            }
        }

    private operator fun User.Companion.invoke(entity: UserEntity) = User(entity.id, entity.name, entity.scope)

    interface UserEntity: Entity<UserEntity> {
        var id: Long
        var name: String
        var scope: Int

        companion object: Entity.Factory<UserEntity>()
    }

}

val Database.users get() = this.sequenceOf(SQLUserRepository.Users)
