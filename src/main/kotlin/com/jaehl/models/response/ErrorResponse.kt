package com.jaehl.models.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code : Int,
    val message : String
)