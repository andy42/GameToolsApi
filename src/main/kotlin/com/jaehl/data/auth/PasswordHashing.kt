package com.jaehl.data.auth

import at.favre.lib.crypto.bcrypt.BCrypt

interface PasswordHashing {
    fun hashPassword(password : String) : String
    fun verifyPassword(password : String, storedHashedPassword : String) : Boolean
}

class PasswordHashingImp : PasswordHashing {

    override fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    override fun verifyPassword(password: String, storedHashedPassword: String) : Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), storedHashedPassword)
        return result.verified
    }
}