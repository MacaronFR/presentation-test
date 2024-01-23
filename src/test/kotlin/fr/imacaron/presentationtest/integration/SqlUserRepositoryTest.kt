package fr.imacaron.presentationtest.integration

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.exception.NotFoundException
import fr.imacaron.presentationtest.sql.SQLUserRepository
import fr.imacaron.presentationtest.sql.users
import fr.imacaron.presentationtest.utils.MariaDBContainers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class SqlUserRepositoryTest: MariaDBContainers() {

    @Test
    fun `Should save in database`() {
        val user = User(1, "Denis", 1)
        val userToAdd = UserCreation(user.name, user.scope)

        userRepository += userToAdd

        val dbUser = db.users.find { it.id eq user.id }?.let { User(it.id, it.name, it.scope) }

        assertThat(dbUser).isNotNull()
            .isEqualTo(user)
    }

    @Test
    fun `Should retrieve from database`() {
        val user = User(1, "Denis", 1)

        db.users.add(SQLUserRepository.UserEntity{
            id = user.id
            name = user.name
            scope = user.scope
        })

        val dbUser = userRepository[user.id]

        assertThat(dbUser).isNotNull()
            .isEqualTo(user)
    }

    @Test
    fun `Should not get unsaved user`() {
        val dbUser = userRepository[1]

        assertThat(dbUser).isNull()
    }

    @Test
    fun `Should delete user`() {
        val user = User(1, "Denis", 1)

        db.users.add(SQLUserRepository.UserEntity {
            id = user.id
            name = user.name
            scope = user.scope
        })

        val result = userRepository.delete(user.id)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNotNull()
            .isEqualTo(user)
    }

    @Test
    fun `Should not delete not found user`() {
        val result = userRepository.delete(1)

        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull()).isNotNull()
            .isExactlyInstanceOf(NotFoundException::class.java)
    }

}