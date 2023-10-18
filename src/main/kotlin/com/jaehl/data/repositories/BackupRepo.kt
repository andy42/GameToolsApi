package com.jaehl.data.repositories

import com.jaehl.data.local.LocalFiles
import com.jaehl.data.model.*
import com.jaehl.data.model.Collection
import com.jaehl.models.ImageType
import com.jaehl.statuspages.ServerError
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.streams.toList

interface BackupRepo {
    fun createBackup(
        users : List<User>,
        images : List<ImageMetaData>,
        games : List<Game>,
        itemCategories : List<ItemCategory>,
        items : List<Item>,
        recipes: List<Recipe>,
        collections : List<Collection>,
        collectionsGroupPreference : List<CollectionsGroupPreference>
    ) : Backup
    fun getBackups() : List<Backup>
    fun getUsers(backupId : String) : List<User>
    fun getImages(backupId : String) : List<ImageMetaData>
    fun getGames(backupId : String) : List<Game>
    fun getImageFile(backupId : String, imageMetaData : ImageMetaData) : File
    fun getItemCategories(backupId : String) : List<ItemCategory>
    fun getItems(backupId : String) : List<Item>
    fun getRecipes(backupId : String) : List<Recipe>
    fun getCollections(backupId : String) : List<CollectionBackup>
    fun getCollectionsGroupPreference(backupId : String) : List<CollectionsGroupPreference>
}

class BackupRepoImp(
    private val environmentConfig : EnvironmentConfig
) : BackupRepo {

    private fun getBackupPath() : String {
        return "${environmentConfig.getWorkingDirectory()}/$backupDirectory"
    }

    private fun getBackupPath(folderName : String) : String {
        return "${getBackupPath()}/$folderName"
    }

    inline fun <reified T>saveList(list :T, dirPath : String, fileName : String){
        val prettyJson = Json { // this returns the JsonBuilder
            prettyPrint = true
            prettyPrintIndent = " "
        }
        LocalFiles.getFile(dirPath, fileName).writeBytes(
            prettyJson.encodeToString(list).toByteArray()
        )
    }

    inline fun <reified T>loadData(path : Path) : T {
        try {
            val prettyJson = Json { // this returns the JsonBuilder
                prettyPrint = true
                prettyPrintIndent = " "
            }

            val data = path.toFile().readText()
            return prettyJson.decodeFromString<T>(data)
        }
        catch (t : Throwable){
            throw ServerError(t.message)
        }
    }

    override fun createBackup(
        users : List<User>,
        images : List<ImageMetaData>,
        games : List<Game>,
        itemCategories : List<ItemCategory>,
        items : List<Item>,
        recipes: List<Recipe>,
        collections : List<Collection>,
        collectionsGroupPreference : List<CollectionsGroupPreference>
    ) : Backup {

        val date = LocalDateTime.now()
        val folderName = fileFormatter.format(date)
        val metaData = Backup(
            id = folderName,
            date = dateFormatter.format(date),
            version = 1
        )
        saveList(metaData, getBackupPath(folderName), metaDataFile)
        saveList(users, getBackupPath(folderName), usersFile)
        saveList(images, getBackupPath(folderName), imagesFile)
        saveList(games,getBackupPath(folderName), gamesFile )
        images.forEach { imageMetaData ->
            val imageType = ImageType.from(imageMetaData.imageType)
            val imageFileName = "${imageMetaData.id}.${imageType.fileExtension}"
            val imageFile = LocalFiles.getFile("${environmentConfig.getWorkingDirectory()}", imageFileName)


            LocalFiles.getFile("${getBackupPath()}/$folderName/$imagesDir", imageFileName)
                .writeBytes(imageFile.readBytes())
        }
        saveList(itemCategories, getBackupPath(folderName), itemCategoriesFile)
        saveList(items, getBackupPath(folderName), itemsFile)
        saveList(recipes, getBackupPath(folderName), recipesFile)
        saveList(collections.map { it.toCollectionBackup() }, getBackupPath(folderName), collectionsFile)
        saveList(collectionsGroupPreference, getBackupPath(folderName), collectionsGroupPreferenceFile)

        return metaData
    }



    override fun getBackups(): List<Backup> {
        return Files.walk(LocalFiles.getDir(getBackupPath()), 1)
            .filter { it.isDirectory() }
            .map {
                Paths.get(it.toFile().absolutePath, metaDataFile)
            }
            .filter {
                it.exists()
            }
            .toList()
            .map { path ->
                loadData<Backup>(path)
            }
    }

    override fun getUsers(backupId : String) : List<User> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, usersFile)
        return loadData<List<User>>(path)
    }

    override fun getImages(backupId : String) : List<ImageMetaData> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, imagesFile)
        return loadData<List<ImageMetaData>>(path)
    }

    override fun getImageFile(backupId: String, imageMetaData: ImageMetaData): File {
        val imageType = ImageType.from(imageMetaData.imageType)
        val imageFileName = "${imageMetaData.id}.${imageType.fileExtension}"
        return LocalFiles.getFile("${getBackupPath()}/$backupId/$imagesDir", imageFileName)
    }

    override fun getGames(backupId : String) : List<Game> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, gamesFile)
        return loadData<List<Game>>(path)
    }

    override fun getItemCategories(backupId : String) : List<ItemCategory> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, itemCategoriesFile)
        return loadData<List<ItemCategory>>(path)
    }

    override fun getItems(backupId : String) : List<Item> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, itemsFile)
        return loadData<List<Item>>(path)
    }

    override fun getRecipes(backupId : String) : List<Recipe> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, recipesFile)
        return loadData<List<Recipe>>(path)
    }

    override fun getCollections(backupId : String) : List<CollectionBackup> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, collectionsFile)
        return loadData<List<CollectionBackup>>(path)
    }

    override fun getCollectionsGroupPreference(backupId: String): List<CollectionsGroupPreference> {
        val path = Paths.get(LocalFiles.getDir(getBackupPath()).absolutePathString(), backupId, collectionsGroupPreferenceFile)
        return loadData<List<CollectionsGroupPreference>>(path)
    }

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        private val fileFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS")
        val backupDirectory = "backups"

        private val metaDataFile = "metaData.json"
        private val usersFile = "users.json"
        private val imagesFile = "images.json"
        private val gamesFile = "games.json"
        private val imagesDir = "images"
        private val itemCategoriesFile = "itemCategories.json"
        private val itemsFile = "items.json"
        private val recipesFile = "recipes.json"
        private val collectionsFile = "collections.json"
        private val collectionsGroupPreferenceFile = "collectionsGroupPreference.json"
    }
}