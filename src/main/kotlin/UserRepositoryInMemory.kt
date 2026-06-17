package com.aethink

class UserRepositoryInMemory: UserRepositoryI {
    val userTable = mutableMapOf<String, User>()

    override fun findUserByEmail(email: String): User? {
        return userTable[email]
    }

    override fun createUser(user: User) {
        this.userTable[user.email] = user
    }
}