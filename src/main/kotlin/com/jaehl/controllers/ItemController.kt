package com.jaehl.controllers

import com.jaehl.data.model.Item
import com.jaehl.data.model.ItemCategory
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.User
import com.jaehl.models.requests.NewCategoryRequest
import com.jaehl.models.requests.NewItemRequest
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.requests.UpdateItemRequest
import com.jaehl.routing.Controller
import com.jaehl.statuspages.AuthorizationException

class ItemController (
    private val gameRepo: GameRepo,
    private val itemRepo: ItemRepo,
    private val userRepo: UserRepo
) : Controller {
    suspend fun addCategory(tokenData : TokenData, newCategoryRequest : NewCategoryRequest) : ItemCategory =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole itemRepo.addCategory(newCategoryRequest.name)
    }

    suspend fun getCategories(tokenData : TokenData) : List<ItemCategory> = accessTokenCall(userRepo, tokenData) {
        return@accessTokenCall itemRepo.getCategories()
    }

    suspend fun addItem(tokenData : TokenData, newItemRequest : NewItemRequest) : Item =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole itemRepo.addNewItem(
                name = newItemRequest.name,
                categories = newItemRequest.categories,
                imageId = newItemRequest.image,
                gameId = newItemRequest.game
            )
    }

    suspend fun updateItem(tokenData : TokenData, itemId: Int, updateItemRequest: UpdateItemRequest) : Item =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole itemRepo.updateItem(
                itemId = itemId,
                request = updateItemRequest
            )
    }

    suspend fun getItems(tokenData : TokenData, gameId : Int?) : List<Item> = accessTokenCall(userRepo, tokenData) {
        if(gameId == null){
            return@accessTokenCall itemRepo.getItems()
        } else {
            return@accessTokenCall itemRepo.getItems().filter { it.game == gameId}
        }
    }

    suspend fun getItem(tokenData : TokenData, itemId : Int) : Item = accessTokenCall(userRepo, tokenData) {
        return@accessTokenCall itemRepo.getItem(itemId)
    }

    suspend fun deleteItem(tokenData : TokenData, itemId : Int) = accessTokenCall(userRepo, tokenData) {
        if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
        itemRepo.deleteItem(
            itemId = itemId
        )
    }
}