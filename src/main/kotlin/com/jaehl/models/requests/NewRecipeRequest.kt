package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewRecipeRequest(
    val gameId : Int,
    val craftedAt : List<Int>,
    val input : List<RecipeAmountRequest>,
    val output : List<RecipeAmountRequest>
)
