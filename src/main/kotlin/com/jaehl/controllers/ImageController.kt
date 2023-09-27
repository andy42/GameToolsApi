package com.jaehl.controllers

import com.jaehl.data.model.ImageMetaData
import com.jaehl.data.model.User
import com.jaehl.data.repositories.ImageRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.ImageData
import com.jaehl.models.ImageType
import com.jaehl.statuspages.AuthorizationException

class ImageController(
    private val imageRepo : ImageRepo,
    private val userRepo: UserRepo
) {

    suspend fun addNew(userId : Int, imageType : ImageType, description : String, data : ByteArray) : Int{
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return imageRepo.addNew(imageType, description, data)
    }

    suspend fun getImageData(imageId : Int) : ImageData {
        return imageRepo.getImageFile(imageId)
    }

    suspend fun getImages() : List<ImageMetaData> {
        return imageRepo.getImages()
    }

}