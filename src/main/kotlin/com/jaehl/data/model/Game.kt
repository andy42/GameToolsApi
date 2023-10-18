package com.jaehl.data.model

import com.jaehl.data.repositories.GameEntity
import com.jaehl.extensions.toItemCategory
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id : Int,
    val name : String,
    val itemCategories : List<ItemCategory>,
    val icon : Int,
    val banner : Int
) {
    companion object {
        fun create(gameEntity : GameEntity) : Game {
            return Game(
                id = gameEntity.id.value,
                name = gameEntity.name,
                itemCategories = gameEntity.itemCategories.map { it.toItemCategory() },
                icon = gameEntity.icon.value,
                banner = gameEntity.banner.value
            )
        }
    }
}