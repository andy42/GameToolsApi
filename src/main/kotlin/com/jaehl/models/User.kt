package com.jaehl.models

data class User(
    val id : String,
    val userName : String,
    val passwordHash : String,
    val role : Role
){
    enum class Role (val value : String){
        Admin("Admin"),
        User("User");

        companion object {
            fun createByName(value: String): Role? {
                return values().firstOrNull {it.value == value}
            }
        }
    }
}