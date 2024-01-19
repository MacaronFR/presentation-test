package fr.imacaron.presentationtest.unit

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.core.service.UserService
import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.core.type.UserUpdate
import fr.imacaron.presentationtest.exception.IllegalCallerException
import fr.imacaron.presentationtest.exception.NotFoundException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserServiceTest(
    @MockK(relaxUnitFun = true) private val userRepository: UserRepository
) {

    @InjectMockKs
    lateinit var userService: UserService

    @BeforeEach
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun `Should get a user`() {
        val userId = 1L
        val user = User(userId, "Denis", 0)
        every { userRepository[userId] } returns user
        val response = userService.getUser(userId)
        assertThat(response).isEqualTo(user)
    }

    @Test
    fun `Should not get a user`() {
        val userId = -1L
        every { userRepository[userId] } returns null
        val response = userService.getUser(userId)
        assertThat(response).isNull()
    }

    @Test
    fun `Should not update non-existent user`() {
        val userId = -1L
        val update = UserUpdate("Zöé", 0)
        val caller = User(1, "Denis", 1)
        every { userRepository[userId] } returns null
        val response = userService.with(caller).updateUser(userId, update)
        assertThat(response.isSuccess).isFalse()
        assertThat(response.exceptionOrNull()).isNotNull().isExactlyInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `Should not update user with greater scope`() {
        val userId = 2L
        val update = UserUpdate("Zöé", 0)
        val userToUpdate = User(userId, "Zoé", 5)
        val caller = User(1, "Denis", 1)
        every { userRepository[userId] } returns userToUpdate
        val result = userService.with(caller).updateUser(userId, update)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull())
            .isNotNull()
            .isExactlyInstanceOf(IllegalCallerException::class.java)
    }

    @Test
    fun `Should not update user with a new scope greater`() {
        val userId = 2L
        val update = UserUpdate("Zöé", 5)
        val userToUpdate = User(userId, "Zoé", 0)
        val caller = User(1, "Denis", 1)
        every { userRepository[userId] } returns userToUpdate
        val result = userService.with(caller).updateUser(userId, update)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull())
            .isNotNull()
            .isExactlyInstanceOf(IllegalCallerException::class.java)
    }

    @Test
    fun `Should throw with no caller`() {
        val userId = 2L
        val update = UserUpdate("Zöé", 10)
        assertThatExceptionOfType(IllegalCallerException::class.java).isThrownBy {
            userService.updateUser(userId, update)
        }.withMessage("Caller must be set using with function")
    }

    @Test
    fun `Should update user`() {
        val userId = 2L
        val update = UserUpdate("Zöé", 1)
        val userToUpdate = User(userId, "Zoé", 0)
        val caller = User(1, "Denis", 1)
        val updatedUser = User(userId, "Zöé", 1)
        every { userRepository[userId] } returns userToUpdate
        val result = userService.with(caller).updateUser(userId, update)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull())
            .isNotNull()
            .isEqualTo(updatedUser)
        verify(exactly = 1) { userRepository[userId] }
        verify(exactly = 1) { userRepository[userId] = updatedUser }
        confirmVerified(userRepository)
    }

    @Test
    fun `Should not create user with same name`() {
        val caller = User(1, "Denis", 1)
        val userName = "Zoé"
        val userToCreate = UserCreation(userName, 1)
        val user = User(0, userName, 1)
        every { userRepository[userName] } returns user
        val result = userService.with(caller).createUser(userToCreate)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull())
            .isNotNull()
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Should not create user with higher scope`() {
        val caller = User(1, "Denis", 1)
        val userToCreate = UserCreation("Zoé", 10)
        every { userRepository[any() as String] } returns null
        val result = userService.with(caller).createUser(userToCreate)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull())
            .isNotNull()
            .isExactlyInstanceOf(IllegalCallerException::class.java)
    }

    @Test
    fun `Should throw with no caller on creation`() {
        val userToCreate = UserCreation("Zoé", 10)
        assertThatExceptionOfType(IllegalCallerException::class.java).isThrownBy {
            userService.createUser(userToCreate)
        }.withMessage("Caller must be set using with function")
    }

    @Test
    fun `Should create user`() {
        val caller = User(1, "Denis", 1)
        val userToCreate = UserCreation("Zoé", 1)
        val createdUser = User(2, "Zoé", 1)
        every { userRepository[any() as String] } returns null
        every { userRepository.lastId } returns 2
        val result = userService.with(caller).createUser(userToCreate)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull())
            .isNotNull()
            .isEqualTo(createdUser)
        verify(exactly = 1) { userRepository += any() }
        verify { userRepository[any() as String] }
        verify { userRepository.lastId }
        confirmVerified(userRepository)
    }

    @Test
    fun `Should not delete user without caller`() {
        val userId = 1L
        assertThatExceptionOfType(IllegalCallerException::class.java).isThrownBy {
            userService.deleteUser(userId)
        }.withMessage("Caller must be set set using with function")
    }

    @Test
    fun `Should not delete user not found`() {
        val caller = User(1, "Denis", 1)
        val userId = 2L
        every { userRepository[userId] } returns null
        val result = userService.with(caller).deleteUser(userId)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull()).isNotNull()
            .isExactlyInstanceOf(NotFoundException::class.java)
        verify(exactly = 1) { userRepository[userId] }
        verify(exactly = 0) { userRepository.delete(userId) }
        confirmVerified(userRepository)
    }

    @Test
    fun `Should not delete user with higher scope`() {
        val caller = User(1, "Denis", 1)
        val userId = 2L
        val user = User(userId, "Zoé", 2)
        every { userRepository[userId] } returns user
        val result = userService.with(caller).deleteUser(userId)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.exceptionOrNull()).isNotNull()
            .isExactlyInstanceOf(IllegalCallerException::class.java)
        verify { userRepository[userId] }
        verify(exactly = 0) { userRepository.delete(userId) }
    }

    @Test
    fun `Should delete user`() {
        val caller = User(1, "Denis", 1)
        val userId = 2L
        val user = User(userId, "Zoé", 1)
        every { userRepository[userId] } returns user
        every { userRepository.delete(userId) } returns Result.success(user)
        val result = userService.with(caller).deleteUser(userId)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNotNull()
            .isEqualTo(user)
    }
}