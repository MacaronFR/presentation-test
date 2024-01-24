package fr.imacaron.presentationtest.end2end

import fr.imacaron.presentationtest.core.type.User
import fr.imacaron.presentationtest.core.type.UserCreation
import fr.imacaron.presentationtest.core.type.UserUpdate
import fr.imacaron.presentationtest.module
import fr.imacaron.presentationtest.sql.entity
import fr.imacaron.presentationtest.sql.users
import fr.imacaron.presentationtest.utils.MariaDBContainers
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ktorm.entity.add
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class UserRouterTest: MariaDBContainers() {
    fun testApp(test: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) = testApplication {
        environment {
            config = MapApplicationConfig(
                "db.url" to database.jdbcUrl,
                "db.user" to database.username,
                "db.password" to database.password
            )
        }

        application {
            module()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        test(client)
    }

    @Test
    fun `Should not get self`() = testApp { client ->
        val response = client.get("/user/me") {
            bearerAuth("1")
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }

    @Test
    fun `Should get self`() = testApp { client ->
        val user = User(0, "Denis", 1)
        db.users.add(user.entity)

        val response = client.get("/user/me") {
            bearerAuth(user.id.toString())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<User>()).isEqualTo(user)
    }

    @Test
    fun `Should get created user`() = testApp { client ->
        val caller = User(0, "Denis", 1)
        val userToCreate = UserCreation("Zoé", 1)

        db.users.add(caller.entity)

        val response = client.post("/user") {
            contentType(ContentType.Application.Json)
            setBody(userToCreate)
            bearerAuth(caller.id.toString())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.body<User>()).extracting("name", "scope").containsExactly(userToCreate.name, userToCreate.scope)
    }

    @Test
    fun `Should delete user`()  = testApp { client ->
        val caller = User(0, "Denis", 1)
        val user = User(1, "Zoé", 1)

        db.users.add(caller.entity)
        db.users.add(user.entity)

        val response = client.delete("/user/${user.id}") {
            bearerAuth(caller.id.toString())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val response2 = client.get("/user/${user.id}") {
            bearerAuth(caller.id.toString())
        }

        assertThat(response2.status).isEqualTo(HttpStatusCode.NotFound)
    }

    @Test
    fun `Should update user`() = testApp { client ->
        val caller = User(0, "Denis", 1)
        val user = User(1, "Zoé", 1)
        val updateUser = UserUpdate("Zöé", 0)
        val userUpdated = User(user.id, updateUser.name, updateUser.scope)

        db.users.add(caller.entity)
        db.users.add(user.entity)

        val response = client.put("/user/${user.id}") {
            bearerAuth(caller.id.toString())
            contentType(ContentType.Application.Json)
            setBody(updateUser)
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<User>()).isEqualTo(userUpdated)
    }
}