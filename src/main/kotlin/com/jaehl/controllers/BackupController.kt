package com.jaehl.controllers

import com.jaehl.data.database.Database
import com.jaehl.data.model.*
import com.jaehl.data.model.Collection
import com.jaehl.data.repositories.*
import com.jaehl.models.ImageType
import com.jaehl.models.requests.*
import com.jaehl.statuspages.ServerError

class BackupController(
    val environmentConfig : EnvironmentConfig,
    private val database: Database,
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
                collectionRepo.getCollections(tokenData.userId),
                collectionRepo.getCollectionsGroupPreference()
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
            val collectionMap = hashMapOf<Int, Collection>()
            val collectionGroupMap = hashMapOf<Int, Collection.Group>()

            database.dropTables()
            database.createTables()

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

            backupRepo.getItemCategories(backupId)
                .forEach {
                    val itemCategory = itemRepo.addCategory(it.name)
                    itemCategoryMap[it.id] = itemCategory
                }

            backupRepo.getGames(backupId)
                .forEach {
                    val game = gameRepo.addNew(
                        NewGameRequest(
                            name = it.name,
                            itemCategories = it.itemCategories
                                .map {itemCategory ->
                                    itemCategory.id
                                },
                            icon = imageMap[it.icon]?.id ?: throw ServerError("missing image Id ${it.icon}"),
                            banner = imageMap[it.banner]?.id ?: throw ServerError("missing image Id ${it.banner}"),
                        )
                    )
                    gameMap[it.id] = game
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
                    val collection = collectionRepo.addCollection(
                        userId = it.userId,
                        request = NewCollectionRequest(
                            gameId =  gameMap[it.gameId]?.id ?: throw ServerError("missing game Id ${it.gameId}"),
                            name = it.name,
                            groups = listOf()
                        )
                    )
                    collectionMap[it.id] = collection
                    it.groups.forEach { oldGroup ->
                        val group = collectionRepo.addGroup(
                            collection.userId,
                            collection.id,
                            NewCollectionGroupRequest(
                                name = oldGroup.name,
                                itemAmounts = oldGroup.itemAmounts.map { itemAmount ->
                                    NewCollectionRequest.ItemAmount(
                                        itemId = itemMap[itemAmount.itemId]?.id ?: throw Exception("item not found : ${itemAmount.itemId}"),
                                        amount = itemAmount.amount
                                    )
                                }
                            )
                        )
                        collectionGroupMap[oldGroup.id] = group
                    }
                }

            backupRepo.getCollectionsGroupPreference(backupId).forEach {
                collectionRepo.updateGroupPreferences(
                    userId = userMap[it.userId]?.id ?: throw ServerError("missing userId  ${it.userId}"),
                    collectionId = collectionMap[it.collectionId]?.id ?: throw ServerError("missing collectionId  ${it.collectionId}"),
                    groupId = collectionGroupMap[it.groupId]?.id ?: throw ServerError("missing groupId  ${it.groupId}"),
                    request = UpdateGroupPreferencesRequest(
                        showBaseIngredients = it.showBaseIngredients ,
                        collapseIngredients = it.collapseIngredients,
                        costReduction = it.costReduction,
                        itemRecipePreferenceMap = it.groupItemPreferences
                    )
                )
            }
        }
}