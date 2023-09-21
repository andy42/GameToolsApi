package com.jaehl.controllers

import com.jaehl.data.model.User
import com.jaehl.data.repositories.ImageRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.statuspages.AuthorizationException

class ImageController(
    private val imageRepo : ImageRepo,
    private val userRepo: UserRepo
) {

    suspend fun addNew(userId : Int, description : String, data : ByteArray) : Int{
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return imageRepo.addNew(description, data)
    }

    suspend fun getImageData(imageId : Int) : ByteArray {
        val imageFile = imageRepo.getImageFile(imageId)
        return imageFile.readBytes()
    }

}