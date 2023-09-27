package com.jaehl.models

enum class ImageType(val value : Int, val fileExtension : String){
    NotSupported(value = 0, fileExtension = ""),
    Png(value = 1, fileExtension = "png"),
    Webp(value = 2, fileExtension = "webp"),
    Jpeg(value = 3, fileExtension = "jpg");


    companion object {
        fun from(value: Int): ImageType {
            return values().find { value == it.value } ?: NotSupported
        }
    }
}