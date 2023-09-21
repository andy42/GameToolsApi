package com.jaehl.controllers

import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
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
        val user = userRepo.createUser(UserCredentials(userName = "userName", password = "password"))
        userRepo.setUserRole(user.id, User.Role.Admin)

        val gameName = "test1"
        val response = controller.addNewGame(
            userId = user.id,
            newGameRequest = NewGameRequest(
                name = gameName
            )
        )
        assertEquals(gameName, response.name)
    }

    @Test
    fun `addNewGame with user account creates error`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserCredentials(userName = "userName", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val gameName = "test1"
        assertThrows<AuthorizationException> {
            controller.addNewGame(
                userId = user.id,
                newGameRequest = NewGameRequest(
                    name = gameName
                )
            )
        }
    }

    @Test
    fun `updateGame with admin account updates game`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserCredentials(userName = "userName", password = "password"))
        userRepo.setUserRole(user.id, User.Role.Admin)

        val game = gameRepo.addNew("testGame")

        val newName = "updatedName"
        val response = controller.updateGame(
            userId = user.id,
            gameId = game?.id ?: -1,
            updateGameRequest = UpdateGameRequest(
                name = newName
            )
        )
        assertEquals(newName, response.name)
    }

    @Test
    fun `updateGame with user account throws error`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserCredentials(userName = "userName", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val game = gameRepo.addNew("testGame")

        assertThrows<AuthorizationException> {
            controller.updateGame(
                userId = user.id,
                gameId = game?.id ?: -1,
                updateGameRequest = UpdateGameRequest(
                    name = "updatedName"
                )
            )
        }
    }

    @Test
    fun `deleteGame with admin account deletes game`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserCredentials(userName = "userName", password = "password"))
        userRepo.setUserRole(user.id, User.Role.Admin)

        val game = gameRepo.addNew("testGame")

        assertEquals(1, gameRepo.getGames().size)
        controller.deleteGame(
            userId = user.id,
            gameId = game?.id ?: -1
        )
        assertEquals(0, gameRepo.getGames().size)
    }

    @Test
    fun `deleteGame with user account throws error`() = runTest {
        val controller = createGameController()
        val user = userRepo.createUser(UserCredentials(userName = "userName", password = "password"))
        userRepo.setUserRole(user.id, User.Role.User)

        val game = gameRepo.addNew("testGame")

        assertThrows<AuthorizationException> {
            controller.deleteGame(
                userId = user.id,
                gameId = game?.id ?: -1
            )
        }
    }

    @Test
    fun `getGame response with game`() = runTest {
        val controller = createGameController()

        val game = gameRepo.addNew("testGame")
        val response = controller.getGame(
            gameId = game?.id ?: -1
        )

        assertEquals(0, response.id)
    }

    @Test
    fun `getAllGames response with list of games`() = runTest {
        val controller = createGameController()

        gameRepo.addNew("testGame1")
        gameRepo.addNew("testGame2")

        val response = controller.getAllGames()

        assertEquals(2, response.size)
    }
}