package com.jaehl.controllers

import com.jaehl.data.model.Collection
import com.jaehl.data.repositories.CollectionRepo
import com.jaehl.models.requests.*
import com.jaehl.statuspages.AuthorizationException

class CollectionController(
    private val collectionRepo: CollectionRepo
) {
    suspend fun addCollection(userId : Int, request: NewCollectionRequest) : Collection{
        return collectionRepo.addCollection(userId, request)
    }

    suspend fun updateCollection(userId : Int, collectionId : Int, request: UpdateCollectionRequest) : Collection{
        val collection = collectionRepo.getCollection(collectionId)
        if(collection.userId != userId) throw AuthorizationException()
        return collectionRepo.updateCollection(collectionId, request)
    }

    suspend fun deleteCollection(userId : Int, collectionId : Int) {
        val collection = collectionRepo.getCollection(collectionId)
        if(collection.userId != userId) throw AuthorizationException()
        collectionRepo.deleteCollection( collectionId)
    }

    suspend fun getCollections(userId : Int, gameId : Int) : List<Collection>{
        return collectionRepo.getCollections(userId, gameId)
    }

    suspend fun getCollection(userId : Int, collectionId : Int) : Collection {
        val collection = collectionRepo.getCollection(collectionId)
        if(collection.userId != userId) throw AuthorizationException()
        return collection
    }

    suspend fun addGroup(collectionId : Int, userId : Int, request : NewCollectionGroupRequest) : Collection.Group {
        val collection = collectionRepo.getCollection(collectionId)
        if(userId != collection.userId) throw AuthorizationException()

        return collectionRepo.addGroup(collectionId, request)
    }

    suspend fun updateGroup(userId : Int, collectionId : Int, groupId : Int, request : UpdateCollectionGroupRequest) : Collection.Group {
        val collection = collectionRepo.getCollection(collectionId)
        if(userId != collection.userId) throw AuthorizationException()
        return collectionRepo.updateGroup(groupId, request)
    }

    suspend fun deleteGroup(userId : Int, collectionId : Int, groupId : Int) {
        val collection = collectionRepo.getCollection(collectionId)
        if(userId != collection.userId) throw AuthorizationException()
        collectionRepo.deleteGroup(groupId)
    }

//    suspend fun addItemAmount(userId : Int, collectionId : Int, groupId : Int, itemId : Int, request : NewCollectionItemAmountRequest) : Collection.Group {
//        val collection = collectionRepo.getCollection(collectionId)
//        if(userId != collection.userId) throw AuthorizationException()
//        return collectionRepo.addItemAmount(groupId, itemId, request)
//    }

    suspend fun updateItemAmount(userId : Int, collectionId : Int, groupId : Int, itemId : Int, request : UpdateCollectionItemAmountRequest) : Collection.Group {
        val collection = collectionRepo.getCollection(collectionId)
        if(userId != collection.userId) throw AuthorizationException()
        return collectionRepo.updateItemAmount(groupId, itemId, request)
    }

    suspend fun deleteItemAmount(userId : Int, collectionId : Int, groupId : Int, itemId : Int) : Collection.Group {
        val collection = collectionRepo.getCollection(collectionId)
        if(userId != collection.userId) throw AuthorizationException()
        return collectionRepo.deleteItemAmount(groupId, itemId)
    }
}