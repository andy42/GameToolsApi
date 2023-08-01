package com.jaehl.data.database

import com.jaehl.data.model.EnvironmentConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource



class Database(
    private val environmentConfig: EnvironmentConfig
) {
    private val databaseHandel : Database

    init {
        val dataSource = PGSimpleDataSource().apply {
            user = environmentConfig.databaseUsername
            password = environmentConfig.databasePassword
            databaseName = "game_tools"
        }

        databaseHandel = Database.connect(dataSource)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction (databaseHandel) { block() }
        }
}