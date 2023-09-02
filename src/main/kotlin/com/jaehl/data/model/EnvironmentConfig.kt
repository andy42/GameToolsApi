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

    val userHomeDirectory : String
)