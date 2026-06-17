package com.aethink

interface UserRepositoryI {
    fun findUserByEmail(email: String): User?
    fun createUser(user: User)
}