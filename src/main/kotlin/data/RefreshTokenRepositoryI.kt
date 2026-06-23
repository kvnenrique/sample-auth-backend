package com.aethink.data

import com.aethink.domain.RefreshToken

interface RefreshTokenRepositoryI {
    fun findRefreshTokenByTokenHash(tokenHash: String): RefreshToken?
    fun findRefreshTokensByUserId(userId: String): List<RefreshToken> // real auth systems require multiple sessions
    fun saveRefreshToken(refreshToken: RefreshToken)
    fun updateRefreshToken(refreshToken: RefreshToken)
    fun revokeRefreshTokenById(id: String)
}
