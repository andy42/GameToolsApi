package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.local.LocalFiles
import com.jaehl.data.model.EnvironmentConfig
import com.jaehl.data.model.ImageMetaData
import com.jaehl.models.ImageData
import com.jaehl.models.ImageType
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
import java.io.FileOutputStream

interface ImageRepo {
    suspend fun dropTables()
    suspend fun createTables()
    suspend fun addNew(imageType: ImageType, description : String, data : ByteArray) : ImageMetaData
    suspend fun getImagePath(imageId : Int) : String?
    suspend fun getImageFile(imageId : Int) : ImageData
    suspend fun updateImage(imageId : Int, data : ByteArray)
    suspend fun deleteImage(imageId : Int)
    suspend fun getImages() : List<ImageMetaData>
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

    override suspend fun dropTables() = database.dbQuery {
        SchemaUtils.drop(ImageTable)
    }

    override suspend fun createTables() = database.dbQuery {
        SchemaUtils.create(ImageTable)
    }

    override suspend fun addNew(imageType: ImageType, description : String, data: ByteArray): ImageMetaData = database.dbQuery {

        val imageId = ImageTable.insertAndGetId {
            it[ImageTable.description] = description
            it[path] = "blank.${imageType.fileExtension}"
            it[ImageTable.imageType] = imageType.value
        }

        val imageFileName = "${imageId}.${imageType.fileExtension}"
        val imageFile = LocalFiles.getFile("${environmentConfig.getWorkingDirectory()}", imageFileName)
        val outputStream = FileOutputStream(imageFile)
        outputStream.write(data)
        outputStream.close()

        val imageEntity = ImageEntity.findById(imageId) ?: throw  ImageIdNotfound(imageId.value)
        imageEntity.path = imageFileName

        return@dbQuery ImageMetaData(
            id = imageEntity.id.value,
            description = imageEntity.description,
            imageType = imageEntity.imageType
        )
    }

    override suspend fun getImagePath(imageId: Int): String? = database.dbQuery {
        val imageEntity = ImageEntity.findById(imageId) ?: throw ImageIdNotfound(imageId)
        return@dbQuery imageEntity.path
    }

    override suspend fun getImageFile(imageId: Int): ImageData = database.dbQuery {
        val imageEntity = ImageEntity.findById(imageId) ?: throw ImageIdNotfound(imageId)
        return@dbQuery ImageData(
            id = imageEntity.id.value,
            file = LocalFiles.getFile("${environmentConfig.getWorkingDirectory()}", imageEntity.path),
            description = imageEntity.description,
            imageType = ImageType.from(imageEntity.imageType)
        )
    }

    override suspend fun getImages(): List<ImageMetaData> = database.dbQuery {
        return@dbQuery ImageEntity.all().map { imageEntity ->
            ImageMetaData(
                id = imageEntity.id.value,
                description = imageEntity.description,
                imageType = imageEntity.imageType
            )
        }
    }

    override suspend fun updateImage(imageId : Int, data: ByteArray) = database.dbQuery {

    }

    override suspend fun deleteImage(imageId: Int) = database.dbQuery {
        val imageEntity = ImageEntity.findById(imageId) ?: throw  ImageIdNotfound(imageId)
        val imageFile = LocalFiles.getFile("${environmentConfig.getWorkingDirectory()}", imageEntity.path)
        if(imageFile.exists()){
            imageFile.delete()
        }
        imageEntity.delete()
    }
}

object ImageTable : IntIdTable("Images") {
    val description : Column<String> = varchar("description",  50)
    val path : Column<String> = varchar("path",  50)
    var imageType : Column<Int> = integer("imageType")

}

class ImageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageEntity>(ImageTable)
    var description by ImageTable.description
    var path by ImageTable.path
    var imageType by ImageTable.imageType
}