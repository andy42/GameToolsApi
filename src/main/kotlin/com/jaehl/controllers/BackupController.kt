package com.jaehl.controllers

import com.jaehl.data.model.*
import com.jaehl.data.repositories.*
import com.jaehl.models.ImageType
import com.jaehl.models.requests.NewCollectionRequest
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.NewRecipeRequest
import com.jaehl.models.requests.RecipeAmountRequest
import com.jaehl.statuspages.ServerError

class BackupController(
    val environmentConfig : EnvironmentConfig,
    val userRepo : UserRepo,
    val gameRepo : GameRepo,
    val imageRepo : ImageRepo,
    val itemRepo : ItemRepo,
    val recipeRepo : RecipeRepo,
    val collectionRepo: CollectionRepo,
    val backupRepo : BackupRepo
) : Controller {

    suspend fun getBackups(tokenData: TokenData): List<Backup> =
        accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin))
        {
            return@accessTokenCallWithRole backupRepo.getBackups()
        }

    suspend fun createBackup(tokenData: TokenData): Backup =
        accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin))
        {
            return@accessTokenCallWithRole backupRepo.createBackup(
                userRepo.getUsers(),
                imageRepo.getImages(),
                gameRepo.getGames(),
                itemRepo.getCategories(),
                itemRepo.getItems(),
                recipeRepo.getRecipes(null),
                collectionRepo.getCollections(tokenData.userId)
            )
        }

    suspend fun applyBackup(tokenData: TokenData, backupId : String) =
        accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin))
        {
            val userMap = hashMapOf<Int, User>()
            val imageMap = hashMapOf<Int, ImageMetaData>()
            val gameMap = hashMapOf<Int, Game>()
            val itemCategoryMap = hashMapOf<Int, ItemCategory>()
            val itemMap = hashMapOf<Int, Item>()

            collectionRepo.dropTables()
            recipeRepo.dropTables()
            itemRepo.dropTables()
            gameRepo.dropTables()
            imageRepo.dropTables()
            userRepo.dropTables()

            userRepo.createTables()
            imageRepo.createTables()
            gameRepo.createTables()
            itemRepo.createTables()
            recipeRepo.createTables()
            collectionRepo.createTables()

            backupRepo.getUsers(backupId)
                .forEach {
                    val user = userRepo.addUserFromBackup(it)
                    userMap[it.id] = user
                }

            backupRepo.getImages(backupId)
                .forEach {
                    val imageMetaData = imageRepo.addNew(
                        ImageType.from(it.imageType),
                        it.description,
                        backupRepo.getImageFile(backupId, it).readBytes()
                    )
                    imageMap[it.id] = imageMetaData
                }

            backupRepo.getGames(backupId)
                .forEach {
                    val game = gameRepo.addNew(
                        NewGameRequest(
                            name = it.name,
                            icon = imageMap[it.icon]?.id ?: throw ServerError("missing image Id ${it.icon}"),
                            banner = imageMap[it.banner]?.id ?: throw ServerError("missing image Id ${it.banner}"),
                        )
                    )
                    gameMap[it.id] = game
                }

            backupRepo.getItemCategories(backupId)
                .forEach {
                    val itemCategory = itemRepo.addCategory(it.name)
                    itemCategoryMap[it.id] = itemCategory
                }

            backupRepo.getItems(backupId)
                .forEach {
                    val item = itemRepo.addNewItem(
                        gameId = gameMap[it.game]?.id ?: throw ServerError("missing game Id ${it.game}"),
                        name = it.name,
                        imageId = imageMap[it.image]?.id ?: throw ServerError("missing image Id ${it.image}"),
                        categories = it.categories.map { itemCategory ->
                            itemCategoryMap[itemCategory.id]?.id ?: throw ServerError("missing itemCategory ${itemCategory.id}")
                        }
                    )
                    itemMap[it.id] = item
                }

            backupRepo.getRecipes(backupId)
                .forEach {
                    recipeRepo.addRecipe(
                        NewRecipeRequest(
                            gameId = gameMap[it.gameId]?.id ?: throw ServerError("missing game Id ${it.gameId}"),
                            craftedAt = it.craftedAt.map { craftedAtId ->
                                itemMap[craftedAtId]?.id ?: throw Exception("item not found : $craftedAtId")
                            },
                            input = it.input.map { recipeAmount ->
                                RecipeAmountRequest(
                                    itemId = itemMap[recipeAmount.itemId]?.id ?: throw Exception("item not found : ${recipeAmount.itemId}"),
                                    amount = recipeAmount.amount
                                )
                            },
                            output = it.output.map { recipeAmount ->
                                RecipeAmountRequest(
                                    itemId = itemMap[recipeAmount.itemId]?.id ?: throw Exception("item not found : ${recipeAmount.itemId}"),
                                    amount = recipeAmount.amount
                                )
                            }
                        )
                    )
                }

            backupRepo.getCollections(backupId)
                .forEach {
                    collectionRepo.addCollection(
                        userId = it.userId,
                        request = NewCollectionRequest(
                            gameId =  gameMap[it.gameId]?.id ?: throw ServerError("missing game Id ${it.gameId}"),
                            name = it.name,
                            groups = it.groups.map { group ->
                                NewCollectionRequest.Group(
                                    name = group.name,
                                    itemAmounts = group.itemAmounts.map { itemAmount ->
                                        NewCollectionRequest.ItemAmount(
                                            itemId = itemMap[itemAmount.itemId]?.id ?: throw Exception("item not found : ${itemAmount.itemId}"),
                                            amount = itemAmount.amount
                                        )
                                    }
                                )
                            }
                        )
                    )
                }

        }
}