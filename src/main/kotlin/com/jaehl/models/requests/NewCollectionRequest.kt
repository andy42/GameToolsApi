package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewCollectionRequest(
    val gameId : Int,
    val name : String,
    val groups : List<Group>
) {
    @Serializable
    data class Group(
        val name : String,
        val itemAmounts : List<ItemAmount>
    )

    @Serializable
    data class ItemAmount(
        val itemId : Int,
        val amount : Int
    )
}