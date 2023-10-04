package com.jaehl.models.response

import kotlinx.serialization.Serializable

@Serializable
data class DataResponse<T> (
    val data : T
)