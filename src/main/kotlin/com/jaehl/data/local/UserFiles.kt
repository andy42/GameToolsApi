package com.jaehl.data.local

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class LocalFiles() {
    companion object {
        fun getDir(userHomeDir : String) : Path{
            val directory = Paths.get(System.getProperty("user.home"), userHomeDir)
            if( !directory.exists()){
                directory.createDirectories()
            }
            return Paths.get(System.getProperty("user.home"), userHomeDir)
        }
        fun getFile(userHomeDir : String, fileName : String) : File{
            val directory = Paths.get(System.getProperty("user.home"), userHomeDir)
            if( !directory.exists()){
                directory.createDirectories()
            }
            return Paths.get(System.getProperty("user.home"), userHomeDir, fileName).toFile()
        }
    }
}