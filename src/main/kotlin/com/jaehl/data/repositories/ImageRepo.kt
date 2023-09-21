package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.local.LocalFiles
import com.jaehl.data.model.EnvironmentConfig
import com.jaehl.statuspages.ImageIdNotfound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import java.io.File
import java.io.FileOutputStream

interface ImageRepo {
    suspend fun addNew(description : String, data : ByteArray) : Int
    suspend fun getImagePath(imageId : Int) : String?
    suspend fun getImageFile(imageId : Int) : File
    suspend fun updateImage(imageId : Int, data : ByteArray)
    suspend fun deleteImage(imageId : Int)
}

class ImageRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope,
    private val environmentConfig : EnvironmentConfig
) : ImageRepo {

    init {
        coroutineScope.launch {
            database.dbQuery {
                SchemaUtils.create(ImageTable)
            }
        }
    }

    override suspend fun addNew(description : String, data: ByteArray): Int = database.dbQuery {

        val imageId = ImageTable.insertAndGetId {
            it[ImageTable.description] = description
            it[path] = "blank.png"
        }

        val imageFileName = "${imageId}.png"
        val imageFile = LocalFiles.getFile(environmentConfig.userHomeDirectory, imageFileName)
        val outputStream = FileOutputStream(imageFile)
        outputStream.write(data)
        outputStream.close()

        val imageEntity = ImageEntity.findById(imageId) ?: throw  ImageIdNotfound(imageId.value)
        imageEntity.path = imageFileName

        return@dbQuery imageId.value
    }

    override suspend fun getImagePath(imageId: Int): String? = database.dbQuery {
        val imageEntity = ImageEntity.findById(imageId) ?: throw ImageIdNotfound(imageId)
        return@dbQuery imageEntity.path
    }

    override suspend fun getImageFile(imageId: Int): File = database.dbQuery {
        val imageEntity = ImageEntity.findById(imageId) ?: throw ImageIdNotfound(imageId)
        return@dbQuery LocalFiles.getFile(environmentConfig.userHomeDirectory, imageEntity.path)
    }

    override suspend fun updateImage(imageId : Int, data: ByteArray) = database.dbQuery {

    }

    override suspend fun deleteImage(imageId: Int) = database.dbQuery {
        val imageEntity = ImageEntity.findById(imageId) ?: throw  ImageIdNotfound(imageId)
        val imageFile = LocalFiles.getFile(environmentConfig.userHomeDirectory, imageEntity.path)
        if(imageFile.exists()){
            imageFile.delete()
        }
        imageEntity.delete()
    }
}

object ImageTable : IntIdTable("Images") {
    val description : Column<String> = varchar("description",  50)
    val path : Column<String> = varchar("path",  50)

}

class ImageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageEntity>(ImageTable)
    var description by ImageTable.description
    var path by ImageTable.path
}