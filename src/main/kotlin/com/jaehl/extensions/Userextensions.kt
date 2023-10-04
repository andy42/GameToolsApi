package com.jaehl.extensions

import com.jaehl.data.model.User
import com.jaehl.models.response.UserSanitized

fun User.toUserSanitized() : UserSanitized {
    return UserSanitized(
        id = this.id,
        userName = this.userName,
        email = this.userName,
        role = this.role
    )
}