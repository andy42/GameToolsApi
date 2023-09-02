package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Item (
    val id : Int,
    val name : String,
    val categories : List<ItemCategory>,
    val image : Int,
    val game : Int
)