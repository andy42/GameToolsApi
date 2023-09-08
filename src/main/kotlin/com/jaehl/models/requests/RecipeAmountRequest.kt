package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class RecipeAmountRequest(
    val itemId : Int,
    val amount : Int
)
