package com.jaehl

import com.google.gson.reflect.TypeToken
import com.jaehl.data.auth.PasswordHashingImp
import com.jaehl.data.auth.TokenManagerImp
import com.jaehl.data.database.Database
import com.jaehl.data.local.ObjectListJsonLoader
import com.jaehl.data.model.EnvironmentConfig
import com.jaehl.data.repositories.*
import com.jaehl.data.model.User
import com.jaehl.models.response.ErrorResponse
import com.jaehl.plugins.configureRouting
import com.jaehl.statuspages.gameStatusPages
import com.jaehl.statuspages.generalStatusPages
import com.jaehl.statuspages.imageStatusPage
import com.jaehl.statuspages.itemStatusPages
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    val environmentConfig = EnvironmentConfig(
        jwtSecret = environment.config.property("jwt.secret").getString(),
        jwtIssuer = environment.config.property("jwt.issuer").getString(),
        jwtAudience = environment.config.property("jwt.audience").getString(),
        jwtRealm = environment.config.property("jwt.realm").getString(),

        jdbcDriver = environment.config.property("database.driver").getString(),
        jdbcDatabaseUrl = environment.config.property("database.databaseUrl").getString(),
        databaseUsername = environment.config.property("database.userName").getString(),
        databasePassword = environment.config.property("database.password").getString(),

        userHomeDirectory = "gameToolsApi",
        debug = true
    )

    val database = Database(environmentConfig)

    val gameRepo: GameRepo = GameRepoImp(
        database = database,
        coroutineScope = this
    )

    val tokenManager = TokenManagerImp(
        environmentConfig = environmentConfig
    )

    val userRepo = UserRepoImp(
        userListLoader = ObjectListJsonLoader<User>(object : TypeToken<Array<User>>() {}.type),
        database = database,
        coroutineScope = this,
        passwordHashing = PasswordHashingImp()
    )

    val imageRepo = ImageRepoImp(
        database = database,
        coroutineScope = this,
        environmentConfig = environmentConfig
    )

    val itemRepo = ItemRepoImp(
        database = database,
        coroutineScope = this
    )

    val recipeRepo = RecipeRepoImp(
        database = database,
        coroutineScope = this
    )

    val collectionRepo = CollectionRepoImp(
        database = database,
        coroutineScope = this
    )

    install(Authentication) {
        jwt("auth-jwt") {
            realm = environmentConfig.jwtRealm
            verifier(
                tokenManager.createJWTVerifier()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(StatusPages) {
        generalStatusPages()
        gameStatusPages()
        imageStatusPage()
        itemStatusPages()
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                ErrorResponse(
                    code = HttpStatusCode.InternalServerError.value,
                    message = if(environmentConfig.debug) cause.message ?: "" else "InternalServerError"
                )
            )
        }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    configureRouting(
        tokenManager,
        userRepo,
        gameRepo,
        imageRepo,
        itemRepo,
        recipeRepo,
        collectionRepo
    )
}
