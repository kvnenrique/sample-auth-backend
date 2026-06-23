package com.aethink.data

import com.aethink.domain.RefreshToken
import kotlin.time.Clock

object RefreshTokenRepositoryInMemory: RefreshTokenRepositoryI {
    val refreshTokenTable = mutableMapOf<String, RefreshToken>()

    override fun findRefreshTokenByTokenHash(tokenHash: String): RefreshToken? {
        return refreshTokenTable.values.find { it.token_hash == tokenHash }
    }

    override fun findRefreshTokensByUserId(userId: String): List<RefreshToken> {
        return refreshTokenTable.values.filter { it.user_id == userId }
    }

    override fun saveRefreshToken(refreshToken: RefreshToken) {
        this.refreshTokenTable[refreshToken.id] = refreshToken
    }

    override fun updateRefreshToken(refreshToken: RefreshToken) {
        this.refreshTokenTable[refreshToken.id] = refreshToken
    }

    override fun revokeRefreshTokenById(id: String) {
        val refreshToken = refreshTokenTable[id] ?: return

        this.refreshTokenTable[id] = refreshToken.copy(
            revoked_at = Clock.System.now()
        )
    }
}
