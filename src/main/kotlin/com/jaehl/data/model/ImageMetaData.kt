package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageMetaData(
    val id : Int,
    val description : String,
    val imageType: Int
)
