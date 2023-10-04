package com.jaehl.data.model

import kotlinx.serialization.Serializable


data class User(
    val id : Int,
    val userName : String,
    val email : String,
    val passwordHash : String,
    val role : Role
){
    @Serializable
    enum class Role (val value : String){
        Admin("Admin"),
        Contributor("Contributor"),
        User("User");

        companion object {
            fun createByName(value: String): Role? {
                return values().firstOrNull {it.value == value}
            }
        }
    }
}