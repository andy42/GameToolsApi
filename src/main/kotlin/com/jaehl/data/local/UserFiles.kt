package com.jaehl.data.local

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

class LocalFiles() {
    companion object {
        fun getFile(userHomeDir : String, fileName : String) : File{
            val directory = Paths.get(System.getProperty("user.home"), userHomeDir)
            if( !directory.exists()){
                directory.createDirectory()
            }
            return Paths.get(System.getProperty("user.home"), userHomeDir, fileName).toFile()
        }
    }
}