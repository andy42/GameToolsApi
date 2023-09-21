package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import com.jaehl.data.model.Collection
import com.jaehl.models.requests.*
import com.jaehl.statuspages.NotFound
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface CollectionRepo {
    suspend fun addCollection(userId : Int, request : NewCollectionRequest) : Collection
    suspend fun updateCollection(collectionId : Int, request : UpdateCollectionRequest) : Collection
    suspend fun deleteCollection(collectionId : Int)
    suspend fun getCollections(userId : Int, gameId : Int) : List<Collection>
    suspend fun getCollection(collectionId: Int) : Collection
    suspend fun addGroup(collectionId : Int, request : NewCollectionGroupRequest) : Collection.Group
    suspend fun updateGroup(groupId : Int, request : UpdateCollectionGroupRequest) : Collection.Group
    suspend fun deleteGroup(groupId: Int)
    suspend fun getGroup(groupId : Int) : Collection.Group
    //suspend fun addItemAmount(groupId : Int, itemId : Int, request : NewCollectionItemAmountRequest) : Collection.Group
    suspend fun updateItemAmount(groupId : Int, itemId : Int, request : UpdateCollectionItemAmountRequest) : Collection.Group
    suspend fun deleteItemAmount(groupId : Int, itemId : Int) : Collection.Group

}

class CollectionRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope,
) : CollectionRepo {

    init {
        coroutineScope.launch {
            database.dbQuery {
                SchemaUtils.create(CollectionTable)
                SchemaUtils.create(CollectionGroupTable)
                SchemaUtils.create(CollectionItemAmountTable)
            }
        }
    }

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
        return@dbQuery collectionEntity.toCollection()
    }

    override suspend fun updateCollection(collectionId : Int, request: UpdateCollectionRequest) : Collection = database.dbQuery {

        val collectionEntity = CollectionEntity.findById(collectionId) ?: throw NotFound("collection not found : $collectionId ")
        collectionEntity.name = request.name

        if(request.groups != null) {
            val groupSet = request.groups.map { it.id }.toSet()
            collectionEntity.groups.forEach { groupEntity ->

                //TODO replace this to only delete were items have been removed, then insert/update  were needed
                CollectionItemAmountTable.deleteWhere { (group eq groupEntity.id) }

                if (!groupSet.contains(groupEntity.id.value)) {
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

        return@dbQuery collectionEntity.toCollection()
    }

    override suspend fun deleteCollection(collectionId: Int) = database.dbQuery {
        val collectionEntity = CollectionEntity.findById(collectionId) ?: throw NotFound("collection not found : $collectionId ")
        collectionEntity.groups.forEach { groupModel ->
            CollectionItemAmountTable.deleteWhere { group eq groupModel.id.value }
            groupModel.delete()
        }
        collectionEntity.delete()
    }

    override suspend fun getCollections(userId: Int, gameId: Int) : List<Collection> = database.dbQuery {
        return@dbQuery CollectionEntity.find { (CollectionTable.user eq userId) and (CollectionTable.game eq gameId) }.map { it.toCollection() }
    }

    override suspend fun getCollection(collectionId: Int): Collection = database.dbQuery {
        return@dbQuery CollectionEntity.findById(collectionId)?.toCollection() ?: throw NotFound("collection not found $collectionId")
    }

    override suspend fun addGroup(collectionId: Int, request: NewCollectionGroupRequest): Collection.Group = database.dbQuery {
        val collectionEntity = CollectionEntity.findById(collectionId) ?: throw NotFound("collection not found : $request.collectionId ")

        val groupEntity = CollectionGroupEntity.new {
            this.collection = collectionEntity
            this.name = request.name
        }

        return@dbQuery groupEntity.toGroup()
    }

    override suspend fun updateGroup(
        groupId: Int,
        request: UpdateCollectionGroupRequest
    ): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        groupEntity.name = request.name
        return@dbQuery groupEntity.toGroup()
    }

    override suspend fun deleteGroup(groupId: Int) = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        CollectionItemAmountTable.deleteWhere { (group eq groupEntity.id) }
        groupEntity.delete()
    }

    override suspend fun getGroup(groupId: Int): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        return@dbQuery groupEntity.toGroup()
    }

//    override suspend fun addItemAmount(groupId : Int, itemId : Int, request : NewCollectionItemAmountRequest) : Collection.Group = database.dbQuery {
//
//        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $request.groupId ")
//        val itemEntity = ItemRow.findById(itemId) ?: throw NotFound("item id not found : $request.itemId")
//        CollectionItemAmountTable.insert {
//            it[group] = groupEntity.id
//            it[item] = itemEntity.id
//            it[CollectionItemAmountTable.amount] = request.amount
//        }
//
//        return@dbQuery groupEntity.toGroup()
//    }

    override suspend fun updateItemAmount(
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

        return@dbQuery groupEntity.toGroup()
    }

    override suspend fun deleteItemAmount(groupId: Int, itemId: Int): Collection.Group = database.dbQuery {
        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
        CollectionItemAmountTable.deleteWhere { (CollectionItemAmountTable.group eq groupId) and (CollectionItemAmountTable.item eq itemId) }
        return@dbQuery groupEntity.toGroup()
    }

//    override suspend fun addGroup(request : NewCollectionGroupRequest): Collection.Group = database.dbQuery {
//        val collectionEntity = CollectionEntity.findById(request.collectionId) ?: throw NotFound("collection not found : $request.collectionId ")
//
//        val groupEntity = CollectionGroupEntity.new {
//            this.collection = collectionEntity
//            this.name = request.name
//        }
//
//        return@dbQuery groupEntity.toGroup()
//    }

//    override suspend fun updateGroup(groupId: Int, request : UpdateCollectionGroupRequest): Collection.Group = database.dbQuery {
//        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
//        groupEntity.name = request.name
//        return@dbQuery groupEntity.toGroup()
//    }
//
//    override suspend fun deleteGroup(groupId: Int) = database.dbQuery {
//        val groupEntity = CollectionGroupEntity.findById(groupId) ?: throw NotFound("groupId not found : $groupId ")
//        CollectionItemAmountTable.deleteWhere { (group eq groupEntity.id) }
//        groupEntity.delete()
//    }





//    override suspend fun updateItemAmount(request : UpdateCollectionItemAmountRequest) : Collection.Group = database.dbQuery {
//        val groupEntity = CollectionGroupEntity.findById(request.groupId) ?: throw NotFound("groupId not found : $request.groupId ")
//        val itemEntity = ItemRow.findById(request.itemId) ?: throw NotFound("item id not found : $request.itemId")
//        CollectionItemAmountTable.update( {(CollectionItemAmountTable.group eq groupEntity.id) and (CollectionItemAmountTable.item eq itemEntity.id) }) {
//            it[CollectionItemAmountTable.amount] = amount
//        }
//        return@dbQuery groupEntity.toGroup()
//    }

//    override suspend fun deleteItemAmount(request : DeleteCollectionItemAmountRequest) : Collection.Group = database.dbQuery {
//        val groupEntity = CollectionGroupEntity.findById(request.groupId) ?: throw NotFound("groupId not found : $request.groupId ")
//        CollectionItemAmountTable.deleteWhere { (CollectionItemAmountTable.group eq request.groupId) and (CollectionItemAmountTable.item eq request.itemId) }
//        return@dbQuery groupEntity.toGroup()
//    }
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

    fun toCollection() : Collection {
        return Collection(
            id = this.id.value,
            userId = this.user.value,
            gameId = this.game.value,
            name = this.name,
            groups = this.groups.map {
                it.toGroup()
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
//    val items by CollectionItemAmountEntity referrersOn CollectionItemAmountTable.group

    fun toGroup() : Collection.Group {
        val groupId = this.id
        return Collection.Group(
            id = this.id.value,
            collectionId = this.collection.id.value,
            name = this.name,
            itemAmounts = CollectionItemAmountTable.select { CollectionItemAmountTable.group eq groupId.value }.map {
                Collection.ItemAmount(
                    itemId = it[CollectionItemAmountTable.item].value,
                    amount = it[CollectionItemAmountTable.amount]
                )
            }
        )
    }

}

object CollectionItemAmountTable : Table("CollectionItemAmount") {
    val group = reference("group_id", CollectionGroupTable)
    val item = reference("item_id", ItemTable)
    val amount = integer("amount")
    override val primaryKey = PrimaryKey(arrayOf(group, item), "compositeKey")
}