package com.jaehl.controllers

import com.jaehl.data.model.Collection
import com.jaehl.data.model.TokenData
import com.jaehl.data.repositories.CollectionRepo
import com.jaehl.models.requests.*
import com.jaehl.routing.Controller
import com.jaehl.statuspages.AuthorizationException

class CollectionController(
    private val collectionRepo: CollectionRepo
) : Controller {

    suspend fun addCollection(tokenData : TokenData, request: NewCollectionRequest) : Collection = accessTokenCall(tokenData) {
        return@accessTokenCall collectionRepo.addCollection(tokenData.userId, request)
    }

    suspend fun updateCollection(tokenData : TokenData, collectionId : Int, request: UpdateCollectionRequest) : Collection = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(collection.userId != tokenData.userId) throw AuthorizationException()
        return@accessTokenCall collectionRepo.updateCollection(collectionId, request)
    }

    suspend fun deleteCollection(tokenData : TokenData, collectionId : Int) = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(collection.userId != tokenData.userId) throw AuthorizationException()
        collectionRepo.deleteCollection( collectionId)
    }

    suspend fun getCollections(tokenData : TokenData, gameId : Int) : List<Collection> = accessTokenCall(tokenData) {
        return@accessTokenCall collectionRepo.getCollections(tokenData.userId, gameId)
    }

    suspend fun getCollection(tokenData : TokenData, collectionId : Int) : Collection = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(collection.userId != tokenData.userId) throw AuthorizationException()
        return@accessTokenCall collection
    }

    suspend fun addGroup(tokenData : TokenData, collectionId : Int, request : NewCollectionGroupRequest) : Collection.Group = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(tokenData.userId != collection.userId) throw AuthorizationException()

        return@accessTokenCall collectionRepo.addGroup(collectionId, request)
    }

    suspend fun updateGroup(tokenData : TokenData, collectionId : Int, groupId : Int, request : UpdateCollectionGroupRequest) : Collection.Group = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(tokenData.userId != collection.userId) throw AuthorizationException()
        return@accessTokenCall collectionRepo.updateGroup(groupId, request)
    }

    suspend fun deleteGroup(tokenData : TokenData, collectionId : Int, groupId : Int) = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(tokenData.userId != collection.userId) throw AuthorizationException()
        collectionRepo.deleteGroup(groupId)
    }

    suspend fun updateItemAmount(tokenData : TokenData, collectionId : Int, groupId : Int, itemId : Int, request : UpdateCollectionItemAmountRequest) : Collection.Group = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(tokenData.userId != collection.userId) throw AuthorizationException()
        return@accessTokenCall collectionRepo.updateItemAmount(groupId, itemId, request)
    }

    suspend fun deleteItemAmount(tokenData : TokenData, collectionId : Int, groupId : Int, itemId : Int) : Collection.Group = accessTokenCall(tokenData) {
        val collection = collectionRepo.getCollection(collectionId)
        if(tokenData.userId != collection.userId) throw AuthorizationException()
        return@accessTokenCall collectionRepo.deleteItemAmount(groupId, itemId)
    }
}