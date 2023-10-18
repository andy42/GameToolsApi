package com.jaehl.extensions

import com.jaehl.data.model.ItemCategory
import com.jaehl.data.repositories.CategoryEntity

fun CategoryEntity.toItemCategory() : ItemCategory {
    return ItemCategory(
        id = this.id.value,
        name = this.name
    )
}