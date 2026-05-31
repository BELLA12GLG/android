package com.wireguard.android.tunnel.failover

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tunnel_failover_policies")
data class TunnelFailoverPolicy(
    @PrimaryKey val tunnelId: String,
    val backupTunnelIds: List<String> = emptyList(),
    val enableHealthCheck: Boolean = true,
    val healthCheckInterval: Long = 30000,
    val maxRetries: Int = 3,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Tunnel(
    val id: String,
    val name: String,
    val config: String,
    val isBackup: Boolean = false,
    val primaryTunnelId: String? = null,
    val isActive: Boolean = false,
    val state: TunnelState = TunnelState.DOWN
)

enum class TunnelState {
    UP, DOWN, CONNECTING, DISCONNECTING
}

@Entity(tableName = "failover_logs")
data class FailoverLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tunnelId: String,
    val eventType: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)