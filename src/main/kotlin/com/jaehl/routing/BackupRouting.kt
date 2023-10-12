package com.jaehl.routing

import com.jaehl.controllers.BackupController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.*
import com.jaehl.models.response.DataResponse
import com.jaehl.statuspages.BadRequest
import com.jaehl.statuspages.ImageIdBadRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.backupRoutingRouting(
    tokenManager : TokenManager,
    backupController : BackupController
) {
    routing {
        authenticate("auth-jwt") {
            get("/admin/backups") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val backups = backupController.getBackups(tokenData)
                call.respond(DataResponse(backups))
            }
            post("/admin/backups/create") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val backups = backupController.createBackup(tokenData)
                call.respond(DataResponse(backups))
            }
            post("/admin/backups/apply/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val backupId = call.parameters["id"] ?: throw BadRequest("can not convert to long")
                backupController.applyBackup(tokenData, backupId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}