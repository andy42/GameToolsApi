package com.jaehl.data.model

data class EnvironmentConfig (
    val jwtSecret : String,
    val jwtIssuer : String,
    val jwtAudience : String,
    val jwtRealm : String,

    val jdbcDriver : String,
    val jdbcDatabaseUrl : String,
    val databaseUsername : String,
    val databasePassword : String,
    val databaseName : String,

    val userHomeDirectory : String,
    val debug : Boolean,

    val adminUserName : String,
    val adminEmail : String,
    val adminPassword : String
) {
    fun getWorkingDirectory() : String {
        return "$userHomeDirectory/$databaseName"
    }
}
