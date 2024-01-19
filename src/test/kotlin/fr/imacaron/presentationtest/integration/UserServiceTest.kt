package fr.imacaron.presentationtest.integration

import fr.imacaron.presentationtest.core.service.UserService
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.core.type.UserUpdate
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserServiceTest {
    private val userRepository = InMemoryUserRepository()

    private val userService = UserService(userRepository)

    private val caller = User(1, "Denis", 1)

    @BeforeEach
    fun setup() {
        userRepository.clear()
        userRepository += UserCreation(caller.name, caller.scope)
    }

    @Test
    fun `Should get existing user`() {
        val result = userService.getUser(1)
        assertThat(result).isNotNull()
            .isEqualTo(caller)
    }

    @Test
    fun `Should not get non-existent user`() {
        val result = userService.getUser(2)
        assertThat(result).isNull()
    }

    @Test
    fun `Should get newly added user`() {
        val user = User(2, "Zoé", 1)
        val result1 = userService.getUser(user.id)
        userService.with(caller).createUser(UserCreation(user.name, user.scope))
        val result2 = userService.getUser(user.id)

        assertThat(result1).isNull()
        assertThat(result2).isNotNull()
            .isEqualTo(user)
    }

    @Test
    fun `Should get updated user`() {
        val user = User(2, "Zoé", 1)
        val updatedUser = User(2, "Zöé", 0)
        userService.with(caller).createUser(UserCreation(user.name, user.scope))
        val result1 = userService.getUser(user.id)
        userService.with(caller).updateUser(user.id, UserUpdate(updatedUser.name, updatedUser.scope))
        val result2 = userService.getUser(user.id)

        assertThat(result1).isNotNull()
            .isEqualTo(user)
        assertThat(result2).isNotNull()
            .isEqualTo(updatedUser)
    }

    @Test
    fun `Should not get deleted user`() {
        val user = User(2, "Zoé", 1)
        userService.with(caller).createUser(UserCreation(user.name, user.scope))
        val result1 = userService.getUser(user.id)
        val result2 = userService.with(caller).deleteUser(user.id)
        val result3 = userService.getUser(user.id)

        assertThat(result1).isNotNull()
            .isEqualTo(user)
        assertThat(result2.isSuccess).isTrue()
        assertThat(result2.getOrNull()).isNotNull()
            .isEqualTo(user)
        assertThat(result3).isNull()
    }
}