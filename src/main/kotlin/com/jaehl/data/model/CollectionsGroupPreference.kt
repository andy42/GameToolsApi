package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionsGroupPreference(
    val userId : Int,
    val collectionId : Int,
    val groupId : Int,
    val showBaseIngredients : Boolean,
    val collapseIngredients : Boolean,
    val costReduction : Float,
    val groupItemPreferences : Map<Int, Int?>
)
