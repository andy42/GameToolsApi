package com.jaehl.data.model

import com.jaehl.data.repositories.GameEntity
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id : Int,
    val name : String
) {
    companion object {
        fun create(game : GameEntity) : Game {
            return Game(
                id = game.id.value,
                name = game.name
            )
        }
    }
}