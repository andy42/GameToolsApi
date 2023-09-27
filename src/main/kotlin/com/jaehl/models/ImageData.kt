package com.jaehl.models

import java.io.File

data class ImageData(
    val id : Int,
    val file : File,
    val description : String,
    val imageType: ImageType
)
