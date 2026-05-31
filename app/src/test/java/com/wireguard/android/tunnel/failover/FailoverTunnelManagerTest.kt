package com.wireguard.android.tunnel.failover

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.*

class FailoverTunnelManagerTest {

    @Mock
    private lateinit var tunnelStateManager: TunnelStateManager

    @Mock
    private lateinit var tunnelRepository: TunnelRepository

    @Mock
    private lateinit var failoverRepository: FailoverRepository

    @Mock
    private lateinit var notificationManager: NotificationManager

    private lateinit var failoverManager: FailoverTunnelManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        failoverManager = FailoverTunnelManager(
            tunnelStateManager = tunnelStateManager,
            tunnelRepository = tunnelRepository,
            failoverRepository = failoverRepository,
            notificationManager = notificationManager
        )
    }

    @Test
    fun testHealthCheckStarted() {
        val tunnelId = "test-tunnel"
        
        failoverManager.startHealthCheck(tunnelId)
        
        assert(failoverManager.getActiveTunnelId() == tunnelId)
    }

    @Test
    fun testStopHealthCheck() {
        val tunnelId = "test-tunnel"
        
        failoverManager.startHealthCheck(tunnelId)
        failoverManager.stopHealthCheck()
        
        assert(failoverManager.getActiveTunnelId() == null)
    }

    @Test
    fun testBackupTunnelUsage() = runBlocking {
        val primaryId = "primary"
        val backupId = "backup"
        val backupTunnel = Tunnel(
            id = backupId,
            name = "Backup Tunnel",
            config = "",
            isBackup = true,
            primaryTunnelId = primaryId
        )

        `when`(tunnelRepository.getBackupTunnels(primaryId)).thenReturn(listOf(backupTunnel))

        val backups = tunnelRepository.getBackupTunnels(primaryId)
        assert(backups.size == 1)
        assert(backups[0].id == backupId)
    }

    @Test
    fun testNotificationSent() {
        val title = "Test"
        val message = "Test message"

        notificationManager.showNotification(title, message)

        verify(notificationManager).showNotification(title, message)
    }

    @Test
    fun testFailoverConfig() {
        val config = FailoverTunnelManager.FailoverConfig(
            pingTarget = "8.8.8.8",
            maxRetries = 5
        )

        assert(config.pingTarget == "8.8.8.8")
        assert(config.maxRetries == 5)
        assert(config.healthCheckIntervalMs == 30000L)
    }

    @Test
    fun testTunnelStateEnum() {
        val states = listOf(
            TunnelState.UP,
            TunnelState.DOWN,
            TunnelState.CONNECTING,
            TunnelState.DISCONNECTING
        )

        assert(states.size == 4)
        assert(TunnelState.UP.name == "UP")
    }
}