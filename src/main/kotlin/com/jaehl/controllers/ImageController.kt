package com.jaehl.controllers

import com.jaehl.data.auth.TokenType
import com.jaehl.data.model.ImageMetaData
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.User
import com.jaehl.data.repositories.ImageRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.ImageData
import com.jaehl.models.ImageType
import com.jaehl.routing.Controller
import com.jaehl.statuspages.AuthorizationException

class ImageController(
    private val imageRepo : ImageRepo,
    private val userRepo: UserRepo
) : Controller {

    suspend fun addNew(tokenData : TokenData, imageType : ImageType, description : String, data : ByteArray) : ImageMetaData =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)){
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole imageRepo.addNew(imageType, description, data)
    }

    suspend fun getImageData(tokenData : TokenData, imageId : Int) : ImageData {
        if(tokenData.tokenType != TokenType.RefreshToken) throw AuthorizationException()
        return imageRepo.getImageFile(imageId)
    }

    suspend fun getImages(tokenData : TokenData) : List<ImageMetaData> = accessTokenCall(userRepo, tokenData) {
        return@accessTokenCall imageRepo.getImages()
    }

}