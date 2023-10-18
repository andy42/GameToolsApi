package com.jaehl.models.requests

data class NewCollectionGroupRequest(
    val name : String,
    val itemAmounts : List<NewCollectionRequest.ItemAmount>
)
