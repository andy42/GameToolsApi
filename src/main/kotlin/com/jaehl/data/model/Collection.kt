package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Collection(
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
