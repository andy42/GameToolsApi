package com.jaehl.data.model

import com.jaehl.data.auth.TokenType

data class TokenData(
    val userId : Int,
    val userName : String,
    val tokenType : TokenType
)
