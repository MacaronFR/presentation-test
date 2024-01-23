package fr.imacaron.presentationtest.utils

import fr.imacaron.presentationtest.core.port.UserRepository
import fr.imacaron.presentationtest.sql.SQLUserRepository
import fr.imacaron.presentationtest.sql.users
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.ktorm.database.Database
import org.ktorm.entity.clear
import org.ktorm.support.mysql.MySqlDialect
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class MariaDBContainers {
    protected lateinit var db: Database

    protected lateinit var userRepository: UserRepository

    companion object {
        @Container
        val database: MariaDBContainer<*> = MariaDBContainer("mariadb:10.2")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("")
            .withInitScript("init-base.sql")
    }

    @BeforeAll
    fun connectToDatabase() {
        db = Database.connect(
            database.jdbcUrl,
            database.driverClassName,
            database.username,
            database.password,
            dialect = MySqlDialect()
        )
        userRepository = SQLUserRepository(db)
    }

    @BeforeEach
    fun cleanDataBase() {
        db.users.clear()
    }
}