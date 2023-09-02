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
                SchemaUtils.create(Images)
            }
        }
    }

    override suspend fun addNew(description : String, data: ByteArray): Int = database.dbQuery {

        val imageId = Images.insertAndGetId {
            it[Images.description] = description
            it[path] = "blank.png"
        }

        val imageFileName = "${imageId}.png"
        val imageFile = LocalFiles.getFile(environmentConfig.userHomeDirectory, imageFileName)
        val outputStream = FileOutputStream(imageFile)
        outputStream.write(data)
        outputStream.close()

        val imageRow = ImageRow.findById(imageId) ?: throw  ImageIdNotfound(imageId.value)
        imageRow.path = imageFileName

        return@dbQuery imageId.value
    }

    override suspend fun getImagePath(imageId: Int): String? = database.dbQuery {
        val imageRow = ImageRow.findById(imageId) ?: throw ImageIdNotfound(imageId)
        return@dbQuery imageRow.path
    }

    override suspend fun getImageFile(imageId: Int): File = database.dbQuery {
        val imageRow = ImageRow.findById(imageId) ?: throw ImageIdNotfound(imageId)
        return@dbQuery LocalFiles.getFile(environmentConfig.userHomeDirectory, imageRow.path)
    }

    override suspend fun updateImage(imageId : Int, data: ByteArray) = database.dbQuery {

    }

    override suspend fun deleteImage(imageId: Int) = database.dbQuery {
        val imageRow = ImageRow.findById(imageId) ?: throw  ImageIdNotfound(imageId)
        val imageFile = LocalFiles.getFile(environmentConfig.userHomeDirectory, imageRow.path)
        if(imageFile.exists()){
            imageFile.delete()
        }
        imageRow.delete()
    }
}

object Images : IntIdTable() {
    val description : Column<String> = varchar("description",  50)
    val path : Column<String> = varchar("path",  50)

}

class ImageRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageRow>(Images)
    var description by Images.description
    var path by Images.path
}