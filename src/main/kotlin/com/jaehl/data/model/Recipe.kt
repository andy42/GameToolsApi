package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id : Int,
    val gameId : Int,
    val craftedAt : List<Int>,
    val input : List<RecipeAmount>,
    val output : List<RecipeAmount>
)

@Serializable
data class RecipeAmount(
    val id : Int,
    val itemId : Int,
    val amount : Int
)