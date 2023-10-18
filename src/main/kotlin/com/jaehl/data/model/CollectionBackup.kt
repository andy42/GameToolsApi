package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionBackup(
    val id : Int,
    val userId : Int,
    val gameId : Int,
    val name : String,
    val groups : List<Group>
) {
    @Serializable
    data class Group(
        val id : Int,
        val collectionId : Int,
        val name : String,
        val itemAmounts : List<ItemAmount>
    )

    @Serializable
    data class ItemAmount(
        val itemId : Int,
        val amount : Int
    )
}

fun Collection.toCollectionBackup() : CollectionBackup {
    return CollectionBackup(
        id = this.id,
        userId = this.userId,
        gameId = this.gameId,
        name = this.name,
        groups = this.groups.map { group ->
            CollectionBackup.Group(
                id = group.id,
                collectionId = group.collectionId,
                name = group.name,
                itemAmounts = group.itemAmounts.map { itemAmount ->
                    CollectionBackup.ItemAmount(
                        itemId = itemAmount.itemId,
                        amount = itemAmount.amount
                    )
                }
            )
        }
    )
}
