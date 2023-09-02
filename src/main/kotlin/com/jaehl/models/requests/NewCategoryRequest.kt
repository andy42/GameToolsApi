package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewCategoryRequest(
    val name : String
)
