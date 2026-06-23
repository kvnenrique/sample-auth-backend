package com.aethink.data

import com.aethink.domain.User

interface UserRepositoryI {
    fun findUserByEmail(email: String): User?
    fun createUser(user: User)
}