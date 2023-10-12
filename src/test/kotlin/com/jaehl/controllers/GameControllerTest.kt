package com.jaehl.controllers

import com.jaehl.data.auth.TokenType
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.models.requests.UserRegisterRequest
import com.jaehl.repositories.GameRepoMock
import com.jaehl.repositories.UserRepoMock
import com.jaehl.statuspages.AuthorizationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GameControllerTest {

    private val gameRepo = GameRepoMock()
    private val userRepo = UserRepoMock()

    private fun createGameController() : GameController {
         return GameController(gameRepo, userRepo)
    }

    @BeforeEach
    fun beforeEach() {
        gameRepo.clear()
        userRepo.clear()
    }

    @Test
    fun `addNewGame with admin account creates game`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.Admin)

        val gameName = "test1"
        val response = controller.addNewGame(
            tokenData = TokenData(
                userId = user.id,
                tokenType = TokenType.AccessToken
            ),
            newGameRequest = NewGameRequest(
                name = gameName,
                icon = 1,
                banner = 1
            )
        )
        assertEquals(gameName, response.name)
    }

    @Test
    fun `addNewGame with user account creates error`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val gameName = "test1"
        assertThrows<AuthorizationException> {
            controller.addNewGame(
                tokenData = TokenData(
                    userId = user.id,
                    tokenType = TokenType.AccessToken
                ),
                newGameRequest = NewGameRequest(
                    name = gameName,
                    icon = 1,
                    banner = 1
                )
            )
        }
    }

    @Test
    fun `updateGame with admin account updates game`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.Admin)

        val game = gameRepo.addNew(
            NewGameRequest("testGame", 1, 1)
        )

        val newName = "updatedName"
        val response = controller.updateGame(
            tokenData = TokenData(
                userId = user.id,
                tokenType = TokenType.AccessToken
            ),
            gameId = game?.id ?: -1,
            updateGameRequest = UpdateGameRequest(
                name = newName,
                icon = 1,
                banner = 1
            )
        )
        assertEquals(newName, response.name)
    }

    @Test
    fun `updateGame with user account throws error`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val game = gameRepo.addNew(
            NewGameRequest("testGame", 1, 1)
        )

        assertThrows<AuthorizationException> {
            controller.updateGame(
                tokenData = TokenData(
                    userId = user.id,
                    tokenType = TokenType.AccessToken
                ),
                gameId = game?.id ?: -1,
                updateGameRequest = UpdateGameRequest(
                    name = "updatedName",
                    icon = 1,
                    banner = 1
                )
            )
        }
    }

    @Test
    fun `deleteGame with admin account deletes game`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.Admin)

        val game = gameRepo.addNew(
            NewGameRequest("testGame", 1, 1)
        )

        assertEquals(1, gameRepo.getGames().size)
        controller.deleteGame(
            tokenData = TokenData(
                userId = user.id,
                tokenType = TokenType.AccessToken
            ),
            gameId = game?.id ?: -1
        )
        assertEquals(0, gameRepo.getGames().size)
    }

    @Test
    fun `deleteGame with user account throws error`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val game = gameRepo.addNew(
            NewGameRequest("testGame", 1, 1)
        )

        assertThrows<AuthorizationException> {
            controller.deleteGame(
                tokenData = TokenData(
                    userId = user.id,
                    tokenType = TokenType.AccessToken
                ),
                gameId = game?.id ?: -1
            )
        }
    }

    @Test
    fun `getGame response with game`() = runTest {
        val controller = createGameController()

        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val game = gameRepo.addNew(
            NewGameRequest("testGame", 1, 1)
        )
        val response = controller.getGame(
            tokenData = TokenData(
                userId = user.id,
                tokenType = TokenType.AccessToken
            ),
            gameId = game?.id ?: -1
        )

        assertEquals(0, response.id)
    }

    @Test
    fun `getAllGames response with list of games`() = runTest {
        val controller = createGameController()

        val user = userRepo.createUser(UserRegisterRequest(userName = "userName", email = "test@test.com", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        gameRepo.addNew(
            NewGameRequest("testGame1", 1, 1)
        )
        gameRepo.addNew(
            NewGameRequest("testGame2", 1, 1)
        )

        val response = controller.getAllGames(
            tokenData = TokenData(
                userId = user.id,
                tokenType = TokenType.AccessToken
            )
        )

        assertEquals(2, response.size)
    }
}