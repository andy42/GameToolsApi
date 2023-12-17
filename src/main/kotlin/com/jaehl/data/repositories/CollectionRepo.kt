package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import com.jaehl.data.model.Collection
import com.jaehl.data.model.CollectionsGroupPreference
import com.jaehl.models.requests.*
import com.jaehl.statuspages.NotFound
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface CollectionRepo {
    suspend fun addCollection(userId : Int, request : NewCollectionRequest) : Collection
    suspend fun updateCollection(userId: Int, collectionId : Int, request : UpdateCollectionRequest) : Collection
    suspend fun deleteCollection(collectionId : Int)
    suspend fun getAdminAllCollections(userId: Int) : List<Collection>
    suspend fun getCollections(userId: Int) : List<Collection>
    suspend fun getCollections(userId : Int, gameId : Int) : List<Collection>
    suspend fun getCollection(userId: Int, collectionId: Int) : Collection
    suspend fun addGroup(userId: Int, collectionId : Int, request : NewCollectionGroupRequest) : Collection.Group
    suspend fun updateGroup(userId: Int, groupId : Int, request : UpdateCollectionGroupRequest) : Collection.Group
    suspend fun deleteGroup(groupId: Int)
    suspend fun getGroup(userId: Int, groupId : Int) : Collection.Group
    suspend fun updateItemAmount(userId: Int, groupId : Int, itemId : Int, request : UpdateCollectionItemAmountRequest) : Collection.Group
    suspend fun deleteItemAmount(userId: Int, groupId : Int, itemId : Int) : Collection.Group
    suspend fun updateGroupPreferences(userId : Int, collectionId: Int, groupId: Int, request : UpdateGroupPreferencesRequest) : Collection.Group
    suspend fun getCollectionsGroupPreference() : List<CollectionsGroupPreference>
}

class CollectionRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope,
) : CollectionRepo {

    override suspend fun addCollection(userId : Int, request: NewCollectionRequest) : Collection = database.dbQuery {

        val user = UserEntity.findById(userId) ?: throw NotFound("user id not found : ${userId}")
        val game = GameEntity.findById(request.gameId) ?: throw NotFound("game id not found : ${request.gameId}")

        val collectionEntity = CollectionEntity.new {
            this.name = request.name
            this.user = user.id
            this.game = game.id
        }

        request.groups.forEach { groupModel ->
            val groupEntity =CollectionGroupEntity.new {
                this.collection = collectionEntity
                this.name = groupModel.name
            }

            groupModel.itemAmounts.forEach { itemAmounts ->
                val itemEntity = ItemEntity.findById(itemAmounts.itemId)
                    ?: throw NotFound("item id not found : ${itemAmounts.itemId}")
                CollectionItemAmountTable.insert {
                    it[group] = groupEntity.id
                    it[item] = itemEntity.id
                    it[amount] = itemAmounts.amount
                }
            }
        }
        return@dbQuery collectionEntity.toCollection(userId)
    }

    override suspend fun updateCollection(userId: Int, collectionId : Int, request: UpdateCollectionRequest) : Collection = database.dbQuery {

        val collectionEntity = CollectionEntity.findById(collectionId) ?: throw NotFound("collection not found : $collectionId ")
        collectionEntity.name = request.name

        if(request.groups != null) {
            val groupSet = request.groups.map { it.id }.toSet()
            collectionEntity.groups.forEach { groupEntity ->

                //TODO replace this to only delete were items have been removed, then insert/update  were needed
                CollectionItemAmountTable.deleteWhere { (group eq groupEntity.id) }

                if (!groupSet.contains(groupEntity.id.value)) {
                    GroupPreferencesTable.deleteWhere { (group eq groupEntity.id) }
                    GroupItemPreferencesTable.deleteWhere { (group eq groupEntity.id) }
                    groupEntity.delete()
                }
            }

            request.groups.forEach { groupModel ->
                val groupEntity = if (groupModel.id == null) CollectionGroupEntity.new {
                    this.collection = collectionEntity
                    this.name = groupModel.name
                } else {
                    CollectionGroupEntity.findById(groupModel.id)
                        ?: throw NotFound("CollectionGroup not found : ${groupModel.id} ")
                }
                groupEntity.name = groupModel.name

                groupModel.itemAmounts.forEach { itemAmounts ->
                    val itemEntity = ItemEntity.findById(itemAmounts.itemId)
                        ?: throw NotFound("item id not found : ${itemAmounts.itemId}")
                    CollectionItemAmountTable.insert {
                        it[group] = groupEntity.id
                        it[item] = itemEntity.id
                        it[amount] = itemAmounts.amount
                    }
                }
            }
        }

        return@dbQuery collectionEntity.toCollection(userId)
    }

    override suspend fun deleteCollection(collectionId: Int) = database.dbQuery {
        val collectionEntity = CollectionEntity.findById(collectionId) ?: throw NotFound("collection not found : $collectionId ")
        collectionEntity.groups.forEach { groupModel ->
            CollectionItemAmountTable.deleteWhere { group eq groupModel.id.value }
            GroupPreferencesTable.deleteWhere { (group eq groupModel.id.value) }
            GroupItemPreferencesTable.deleteWhere { (group eq groupModel.id.value) }
            groupModel.delete()
        }
        collectionEntity.delete()
    }

    override suspend fun getAdminAllCollections(userId: Int): List<Collection> = database.dbQuery {
        return@dbQuery CollectionEntity.all().map { it.toCollection(userId) }
    }

    override suspend fun getCollections(userId: Int) : List<Collection> = database.dbQuery {
        return@dbQuery CollectionEntity.find { (CollectionTable.user eq userId) }.map { it.toCollection(userId) }
    }

    override suspend fun getCollections(userId: Int, gameId: Int) : List<Collection> = database.dbQuery {
        return@dbQuery CollectionEntity.find { (CollectionTable.user eq userId) and (CollectionTable.game eq gameId) }.map { it.toCollection(userId) }
    }

    override suspend fun getCollection(userId: Int, collectionId: Int): Collection = database.dbQuery {
        return@dbQuery CollectionEntity.findById(collectionId)?.toCollection(userId) ?: throw NotFound("collection not found $collectionId")
    }

    override suspend fun addGroup(userId: Int, collectionId: Int, request: NewCollectionGroupRequest): Collection.Group = database.dbQuery {
        val collectionEntity = CollectionEntity.findById(collectionId) ?: throw NotFound("collection not found : $request.collectionId ")

        val groupEntity = CollectionGroupEntity.new {
            this.collection = collectionEntity
            this.name = request.name
        }

        request.itemAmounts.forEach { itemAmounts ->
            val itemEntity = ItemEntity.findById(itemAmounts.itemId)
                ?: throw NotFound("item id not found : ${itemAmounts.itemId}")
            CollectionItemAmountTable.insert {
                it[group] = groupEntity.id
                it[item] = itemEntity.id
                it[amount] = itemAmounts.amount
            }
        }

        return@dbQuery groupEntity.toGroup(userId)
    }

    override suspend fun updateGroup(
        userId: Int,
        groupId: Int,
        request: UpdateCollectionGroupRequest
    ): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        groupEntity.name = request.name
        return@dbQuery groupEntity.toGroup(userId)
    }

    override suspend fun deleteGroup(groupId: Int) = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        GroupPreferencesTable.deleteWhere { (group eq groupEntity.id) }
        GroupItemPreferencesTable.deleteWhere { (group eq groupEntity.id) }
        CollectionItemAmountTable.deleteWhere { (group eq groupEntity.id) }
        groupEntity.delete()
    }

    override suspend fun getGroup(userId: Int, groupId: Int): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        return@dbQuery groupEntity.toGroup(userId)
    }

    override suspend fun updateItemAmount(
        userId: Int,
        groupId: Int,
        itemId: Int,
        request: UpdateCollectionItemAmountRequest
    ): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $request.groupId ")
        val itemEntity = ItemEntity.findById(itemId) ?: throw NotFound("item id not found : $request.itemId")

        if(CollectionItemAmountTable.select({(CollectionItemAmountTable.group eq groupEntity.id) and (CollectionItemAmountTable.item eq itemEntity.id) }).empty()){
            CollectionItemAmountTable.insert {
                it[group] = groupEntity.id
                it[item] = itemEntity.id
                it[CollectionItemAmountTable.amount] = request.amount
            }
        } else {
            CollectionItemAmountTable.update( {(CollectionItemAmountTable.group eq groupEntity.id) and (CollectionItemAmountTable.item eq itemEntity.id) }) {
                it[CollectionItemAmountTable.amount] = amount
            }
        }

        return@dbQuery groupEntity.toGroup(userId)
    }

    override suspend fun deleteItemAmount(userId: Int, groupId: Int, itemId: Int): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        CollectionItemAmountTable.deleteWhere { (CollectionItemAmountTable.group eq groupId) and (CollectionItemAmountTable.item eq itemId) }
        return@dbQuery groupEntity.toGroup(userId)
    }

    override suspend fun updateGroupPreferences(
        userId: Int,
        collectionId: Int,
        groupId: Int,
        request: UpdateGroupPreferencesRequest
    ) : Collection.Group = database.dbQuery {
        val group = CollectionGroupEntity.findById(groupId) ?: throw NotFound("group not found $groupId")
        val user = UserEntity.findById(userId) ?: throw NotFound("user not found $userId")

        if(GroupPreferencesTable.select({(GroupPreferencesTable.group eq group.id) and (GroupPreferencesTable.user eq user.id) }).empty()){
            GroupPreferencesTable.insert {
                it[GroupPreferencesTable.group] = group.id
                it[GroupPreferencesTable.user] = user.id
                it[showBaseIngredients] = request.showBaseIngredients
                it[collapseIngredients] = request.collapseIngredients
                it[costReduction] = request.costReduction
            }
        }
        else {
            GroupPreferencesTable.update( {(GroupPreferencesTable.group eq group.id) and (GroupPreferencesTable.user eq user.id) }) {
                it[showBaseIngredients] = request.showBaseIngredients
                it[collapseIngredients] = request.collapseIngredients
                it[costReduction] = request.costReduction
            }
        }

        GroupItemPreferencesTable.deleteWhere { (GroupItemPreferencesTable.group eq group.id) and (GroupItemPreferencesTable.user eq user.id) }
        request.itemRecipePreferenceMap.forEach{entry: Map.Entry<Int, Int?> ->
            val item = ItemEntity.findById(entry.key) ?: throw NotFound("item not found ${entry.key}")
            val recipeId = entry.value
            val recipe = if(recipeId != null) RecipeEntity.findById(recipeId) else null
            GroupItemPreferencesTable.insert {
                it[GroupItemPreferencesTable.group] = group.id
                it[GroupItemPreferencesTable.user] = user.id
                it[GroupItemPreferencesTable.item] = item.id
                it[GroupItemPreferencesTable.recipe] = recipe?.id
            }
        }

        return@dbQuery group.toGroup(userId)
    }

    override suspend fun getCollectionsGroupPreference(): List<CollectionsGroupPreference> = database.dbQuery {
        return@dbQuery GroupPreferencesTable.selectAll().map {
            val userId = it[GroupPreferencesTable.user].value
            val groupId = it[GroupPreferencesTable.group].value
            val itemPreferencesMap = hashMapOf<Int, Int?>()
            GroupItemPreferencesTable.select( (GroupItemPreferencesTable.group eq groupId) and (GroupItemPreferencesTable.user eq userId) )
                .forEach { resultRow ->
                    itemPreferencesMap[resultRow[GroupItemPreferencesTable.item].value] = resultRow[GroupItemPreferencesTable.recipe]?.value
                }

            val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw Exception("groupId not found $groupId")

            return@map CollectionsGroupPreference(
                userId = userId,
                collectionId = groupEntity.collection.id.value,
                groupId = groupId,
                showBaseIngredients = it[GroupPreferencesTable.showBaseIngredients],
                collapseIngredients = it[GroupPreferencesTable.collapseIngredients],
                costReduction = it[GroupPreferencesTable.costReduction],
                groupItemPreferences = itemPreferencesMap
            )
        }
    }
}

object CollectionTable : IntIdTable("Collection") {
    val name : Column<String> = varchar("name",  50)
    val user = reference("user_id", UserTable)
    val game = reference("game_id", GameTable)
}

class CollectionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CollectionEntity>(CollectionTable)
    var name by CollectionTable.name
    var user by CollectionTable.user
    var game by CollectionTable.game
    val groups by CollectionGroupEntity referrersOn CollectionGroupTable.collection

    fun toCollection(userId : Int) : Collection {
        return Collection(
            id = this.id.value,
            userId = this.user.value,
            gameId = this.game.value,
            name = this.name,
            groups = this.groups.map {
                it.toGroup(userId)
            }
        )
    }
}

object CollectionGroupTable : IntIdTable("CollectionGroup") {
    val collection = reference("collection_id", CollectionTable)
    val name : Column<String> = varchar("name",  50)
}

class CollectionGroupEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CollectionGroupEntity>(CollectionGroupTable)
    var collection by CollectionEntity referencedOn CollectionGroupTable.collection
    var name by CollectionGroupTable.name

    fun toGroup(userId : Int) : Collection.Group {
        val groupId = this.id
        val groupPreferences = GroupPreferencesTable.select{ (GroupPreferencesTable.group eq groupId ) and (GroupPreferencesTable.user eq userId)}.firstOrNull()

        val itemRecipePreferenceMap = hashMapOf<Int, Int?>()
        GroupItemPreferencesTable.select{ (GroupItemPreferencesTable.group eq groupId ) and (GroupItemPreferencesTable.user eq userId)}
            .forEach {
                itemRecipePreferenceMap[it[GroupItemPreferencesTable.item].value] = it[GroupItemPreferencesTable.recipe]?.value
            }

        return Collection.Group(
            id = this.id.value,
            collectionId = this.collection.id.value,
            name = this.name,
            itemAmounts = CollectionItemAmountTable.select { CollectionItemAmountTable.group eq groupId.value }.map {
                Collection.ItemAmount(
                    itemId = it[CollectionItemAmountTable.item].value,
                    amount = it[CollectionItemAmountTable.amount]
                )
            },
            showBaseIngredients = groupPreferences?.get(GroupPreferencesTable.showBaseIngredients) ?: false,
            collapseIngredients = groupPreferences?.get(GroupPreferencesTable.collapseIngredients) ?: true,
            costReduction = groupPreferences?.get(GroupPreferencesTable.costReduction) ?: 1f,
            itemRecipePreferenceMap = itemRecipePreferenceMap
        )
    }
}

object CollectionItemAmountTable : Table("CollectionItemAmount") {
    val group = reference("group_id", CollectionGroupTable)
    val item = reference("item_id", ItemTable)
    val amount = integer("amount")
    override val primaryKey = PrimaryKey(arrayOf(group, item), "compositeKey")
}

object GroupPreferencesTable : Table("GroupPreferences") {
    val group = reference("group_id", CollectionGroupTable)
    val user = reference("user_id", UserTable)
    val showBaseIngredients = bool("showBaseIngredients")
    val collapseIngredients = bool("collapseIngredients")
    val costReduction = float("costReduction")
    override val primaryKey = PrimaryKey(arrayOf(group, user))
}

object GroupItemPreferencesTable : Table("GroupItemPreferences") {
    val group = reference("group_id", CollectionGroupTable)
    val user = reference("user_id", UserTable)
    val item = reference("item_id", ItemTable)
    val recipe = reference("recipe_id", RecipeTable).nullable()
    override val primaryKey = PrimaryKey(arrayOf(group, user, item))
}