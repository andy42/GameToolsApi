package com.jaehl.data.database

import com.jaehl.data.model.EnvironmentConfig
import com.jaehl.data.repositories.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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
            databaseName = environmentConfig.databaseName
        }

        databaseHandel = Database.connect(dataSource)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction (databaseHandel) { block() }
        }

    fun <T> dbQueryRunBlocking(block: () -> T): T =
        transaction (databaseHandel) { block()
    }


    fun createTables() = dbQueryRunBlocking {

        SchemaUtils.create(UserTable)

        SchemaUtils.create(CategorieTable)
        SchemaUtils.create(ImageTable)

        SchemaUtils.create(GameTable)
        SchemaUtils.create(GameCategoriesTable)

        SchemaUtils.create(ItemTable)
        SchemaUtils.create(ItemCategorieTable)

        SchemaUtils.create(RecipeTable)
        SchemaUtils.create(RecipeCraftedAtTable)
        SchemaUtils.create(RecipeInputTable)
        SchemaUtils.create(RecipeOutputTable)

        SchemaUtils.create(CollectionTable)
        SchemaUtils.create(CollectionGroupTable)
        SchemaUtils.create(CollectionItemAmountTable)
        SchemaUtils.create(GroupPreferencesTable)
        SchemaUtils.create(GroupItemPreferencesTable)

        SchemaUtils.create(ItemTable)
        SchemaUtils.create(ItemCategorieTable)
    }

    fun dropTables() = dbQueryRunBlocking {
        SchemaUtils.drop(GroupItemPreferencesTable)

        SchemaUtils.drop(RecipeOutputTable)
        SchemaUtils.drop(RecipeInputTable)
        SchemaUtils.drop(RecipeCraftedAtTable)
        SchemaUtils.drop(RecipeTable)

        SchemaUtils.drop(GroupPreferencesTable)

        SchemaUtils.drop(CollectionItemAmountTable)
        SchemaUtils.drop(CollectionGroupTable)
        SchemaUtils.drop(CollectionTable)

        SchemaUtils.drop(ItemCategorieTable)
        SchemaUtils.drop(ItemTable)

        SchemaUtils.drop(GameCategoriesTable)
        SchemaUtils.drop(GameTable)

        SchemaUtils.drop(GameTable)
        SchemaUtils.drop(ImageTable)
        SchemaUtils.drop(CategorieTable)

        SchemaUtils.drop(UserTable)
    }
}