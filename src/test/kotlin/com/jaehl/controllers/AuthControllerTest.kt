package com.jaehl.controllers


import com.jaehl.data.auth.TokenManagerMock
import com.jaehl.models.requests.UserRegisterRequest
import com.jaehl.repositories.UserRepoMock
import com.jaehl.statuspages.BadRequest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthControllerTest {

    private val userRepo = UserRepoMock()
    private val tokenManager = TokenManagerMock()

    private fun createAuthController() : AuthController {
        return AuthController(userRepo, tokenManager)
    }

    @BeforeEach
    fun beforeEach() {
        userRepo.clear()
    }

    @Test
    fun registrationValid() = runTest {
        userRepo.createUser(
            UserRegisterRequest(
                userName = "test1",
                email = "test@test.com",
                password = "password"
            )
        )

        val authController = createAuthController()
        val userTokens = authController.userRegister(
            UserRegisterRequest(
                userName = "test2",
                email = "tes2@test.com",
                password = "password"
            )
        )
    }

    @Test
    fun `userRegister fail userName & email already use`() = runTest {
        userRepo.createUser(
            UserRegisterRequest(
                userName = "test1",
                email = "test@test.com",
                password = "password"
            )
        )
        val authController = createAuthController()
        val exception = assertFailsWith<BadRequest>(){
            authController.userRegister(
                UserRegisterRequest(
                    userName = "test1",
                    email = "test@test.com",
                    password = "password"
                )
            )
        }
        assertEquals("Your User Name and Email are already in use", exception.message)
    }

    @Test
    fun `userRegister fail userName already use`() = runTest {
        userRepo.createUser(
            UserRegisterRequest(
                userName = "test1",
                email = "test@test.com",
                password = "password"
            )
        )
        val authController = createAuthController()
        val exception = assertFailsWith<BadRequest>(){
            authController.userRegister(
                UserRegisterRequest(
                    userName = "test1",
                    email = "test2@test.com",
                    password = "password"
                )
            )
        }
        assertEquals("Your User Name is already in use", exception.message)
    }

    @Test
    fun `userRegister fail email already use`() = runTest {
        userRepo.createUser(
            UserRegisterRequest(
                userName = "test1",
                email = "test@test.com",
                password = "password"
            )
        )
        val authController = createAuthController()
        val exception = assertFailsWith<BadRequest>(){
            authController.userRegister(
                UserRegisterRequest(
                    userName = "test2",
                    email = "test@test.com",
                    password = "password"
                )
            )
        }
        assertEquals("Your email is already in use", exception.message)
    }
}