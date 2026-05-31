package com.wireguard.android.tunnel.failover

import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress

class FailoverTunnelManager(
    private val tunnelStateManager: TunnelStateManager,
    private val tunnelRepository: TunnelRepository,
    private val failoverRepository: FailoverRepository,
    private val notificationManager: NotificationManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + Job())
) {
    private var healthCheckJob: Job? = null
    private val TAG = "FailoverTunnelManager"

    data class FailoverConfig(
        val pingTarget: String = "1.1.1.1",
        val pingTimeoutMs: Long = 5000,
        val healthCheckIntervalMs: Long = 30000,
        val maxRetries: Int = 3
    )

    private var currentConfig = FailoverConfig()
    private var activeTunnelId: String? = null
    private var retryCount = 0
    private var isEnabled = false

    fun startHealthCheck(tunnelId: String, config: FailoverConfig = FailoverConfig(), backupTunnelIds: List<String> = emptyList()) {
        currentConfig = config
        activeTunnelId = tunnelId
        retryCount = 0
        isEnabled = true

        healthCheckJob = scope.launch {
            while (isActive && isEnabled) {
                try {
                    delay(currentConfig.healthCheckIntervalMs)
                    performHealthCheck(tunnelId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun performHealthCheck(tunnelId: String) {
        if (pingHost(currentConfig.pingTarget)) {
            retryCount = 0
            return
        }
        handlePingFailure(tunnelId)
    }

    private suspend fun handlePingFailure(tunnelId: String) {
        retryCount++
        when {
            retryCount <= currentConfig.maxRetries -> {
                if (restartTunnel(tunnelId)) {
                    delay(3000)
                    if (pingHost(currentConfig.pingTarget)) {
                        retryCount = 0
                        return
                    }
                }
                switchToBackupTunnel(tunnelId)
            }
            else -> {
                closeTunnel(tunnelId)
                notifyFailure(tunnelId)
            }
        }
    }

    private suspend fun switchToBackupTunnel(primaryTunnelId: String) {
        val backupTunnels = tunnelRepository.getBackupTunnels(primaryTunnelId)
        for (backupTunnel in backupTunnels) {
            try {
                tunnelStateManager.setState(primaryTunnelId, TunnelState.DOWN, null)
                delay(500)
                tunnelStateManager.setState(backupTunnel.id, TunnelState.UP, null)
                delay(3000)
                if (pingHost(currentConfig.pingTarget)) {
                    activeTunnelId = backupTunnel.id
                    retryCount = 0
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
        closeTunnels(primaryTunnelId, backupTunnels.map { it.id })
        notifyFailure(primaryTunnelId)
    }

    private suspend fun pingHost(host: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            InetAddress.getByName(host).isReachable(currentConfig.pingTimeoutMs.toInt())
        } catch (e: IOException) {
            false
        }
    }

    private suspend fun restartTunnel(tunnelId: String): Boolean {
        return try {
            tunnelStateManager.setState(tunnelId, TunnelState.DOWN, null)
            delay(500)
            tunnelStateManager.setState(tunnelId, TunnelState.UP, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun closeTunnel(tunnelId: String) {
        try {
            tunnelStateManager.setState(tunnelId, TunnelState.DOWN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
        }
    }

    private suspend fun closeTunnels(primaryId: String, backupIds: List<String>) {
        listOf(primaryId).plus(backupIds).forEach { closeTunnel(it) }
    }

    private fun notifyFailure(tunnelId: String) {
        notificationManager.showNotification(
            title = "隧道连接失败",
            message = "所有恢复尝试都已失败，隧道已关闭",
            priority = "critical"
        )
    }

    fun stopHealthCheck() {
        isEnabled = false
        healthCheckJob?.cancel()
        activeTunnelId = null
        retryCount = 0
    }

    fun getActiveTunnelId(): String? = activeTunnelId

    fun cleanup() {
        stopHealthCheck()
        scope.cancel()
    }
}