package com.wireguard.android.tunnel.failover

interface TunnelStateManager {
    suspend fun setState(tunnelId: String, state: TunnelState, reason: String?)
    suspend fun getState(tunnelId: String): TunnelState
}

interface TunnelRepository {
    suspend fun getTunnelById(tunnelId: String): Tunnel?
    suspend fun getBackupTunnels(primaryTunnelId: String): List<Tunnel>
    suspend fun getAllTunnels(): List<Tunnel>
}

interface NotificationManager {
    fun showNotification(title: String, message: String, priority: String = "normal", action: String? = null)
    fun dismissNotification(notificationId: String)
}

interface FailoverRepository {
    suspend fun saveTunnelFailoverPolicy(policy: TunnelFailoverPolicy)
    suspend fun getTunnelFailoverPolicy(tunnelId: String): TunnelFailoverPolicy?
    suspend fun deleteFailoverPolicy(tunnelId: String)
    suspend fun logEvent(tunnelId: String, eventType: String, message: String)
    suspend fun getTunnelFailoverLogs(tunnelId: String, limit: Int = 100): List<FailoverLog>
    suspend fun setActiveTunnel(tunnelId: String)
}