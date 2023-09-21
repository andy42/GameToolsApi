package com.jaehl.controllers

import com.jaehl.data.model.Item
import com.jaehl.data.model.ItemCategory
import com.jaehl.data.model.User
import com.jaehl.models.requests.NewCategoryRequest
import com.jaehl.models.requests.NewItemRequest
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.requests.UpdateItemRequest
import com.jaehl.statuspages.AuthorizationException

class ItemController (
    private val gameRepo: GameRepo,
    private val itemRepo: ItemRepo,
    private val userRepo: UserRepo
) {
    suspend fun addCategory(userId : Int, newCategoryRequest : NewCategoryRequest) : ItemCategory {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return itemRepo.addCategory(newCategoryRequest.name)
    }

    suspend fun getCategories() : List<ItemCategory>{
        return itemRepo.getCategories()
    }

    suspend fun addItem(userId : Int, newItemRequest : NewItemRequest) : Item{
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return itemRepo.addNewItem(
            name = newItemRequest.name,
            categories = newItemRequest.categories,
            imageId = newItemRequest.image,
            gameId = newItemRequest.game
        )
    }

    suspend fun updateItem(userId: Int, itemId: Int, updateItemRequest: UpdateItemRequest) : Item{
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return itemRepo.updateItem(
            itemId = itemId,
            request = updateItemRequest
        )
    }

    suspend fun getItems(gameId : Int?) : List<Item> {
        if(gameId == null){
            return itemRepo.getItems()
        } else {
            return itemRepo.getItems().filter { it.game == gameId}
        }
    }

    suspend fun getItem(itemId : Int) : Item {
        return itemRepo.getItem(itemId)
    }

    suspend fun deleteItem(userId : Int, itemId : Int) {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        itemRepo.deleteItem(
            itemId = itemId
        )
    }
}