package com.wireguard.android.tunnel.failover

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FailoverPolicyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: TunnelFailoverPolicy)

    @Query("SELECT * FROM tunnel_failover_policies WHERE tunnelId = :tunnelId")
    suspend fun getPolicyByTunnelId(tunnelId: String): TunnelFailoverPolicy?

    @Query("SELECT * FROM tunnel_failover_policies")
    fun getAllPolicies(): Flow<List<TunnelFailoverPolicy>>

    @Delete
    suspend fun deletePolicy(policy: TunnelFailoverPolicy)

    @Query("DELETE FROM tunnel_failover_policies WHERE tunnelId = :tunnelId")
    suspend fun deletePolicyByTunnelId(tunnelId: String)
}

@Dao
interface FailoverLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FailoverLog)

    @Query("SELECT * FROM failover_logs WHERE tunnelId = :tunnelId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLogsByTunnelId(tunnelId: String, limit: Int = 100): List<FailoverLog>

    @Query("SELECT * FROM failover_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 100): List<FailoverLog>

    @Query("DELETE FROM failover_logs WHERE timestamp < :beforeTime")
    suspend fun deleteOldLogs(beforeTime: Long)

    @Query("DELETE FROM failover_logs WHERE tunnelId = :tunnelId")
    suspend fun deleteLogsByTunnelId(tunnelId: String)
}

class FailoverRepositoryImpl(
    private val failoverPolicyDao: FailoverPolicyDao,
    private val failoverLogDao: FailoverLogDao
) : FailoverRepository {

    override suspend fun saveTunnelFailoverPolicy(policy: TunnelFailoverPolicy) {
        failoverPolicyDao.insertPolicy(policy)
        logEvent(policy.tunnelId, "policy_created", "故障转移策略已创建")
    }

    override suspend fun getTunnelFailoverPolicy(tunnelId: String): TunnelFailoverPolicy? {
        return failoverPolicyDao.getPolicyByTunnelId(tunnelId)
    }

    override suspend fun deleteFailoverPolicy(tunnelId: String) {
        failoverPolicyDao.deletePolicyByTunnelId(tunnelId)
        logEvent(tunnelId, "policy_deleted", "故障转移策略已删除")
    }

    override suspend fun logEvent(tunnelId: String, eventType: String, message: String) {
        val log = FailoverLog(
            tunnelId = tunnelId,
            eventType = eventType,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        failoverLogDao.insertLog(log)
    }

    override suspend fun getTunnelFailoverLogs(tunnelId: String, limit: Int): List<FailoverLog> {
        return failoverLogDao.getLogsByTunnelId(tunnelId, limit)
    }

    override suspend fun setActiveTunnel(tunnelId: String) {
        logEvent(tunnelId, "tunnel_activated", "隧道已激活")
    }

    suspend fun cleanupOldLogs(daysOld: Int = 30) {
        val beforeTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        failoverLogDao.deleteOldLogs(beforeTime)
    }

    suspend fun clearTunnelLogs(tunnelId: String) {
        failoverLogDao.deleteLogsByTunnelId(tunnelId)
    }

    fun getAllPolicies(): Flow<List<TunnelFailoverPolicy>> {
        return failoverPolicyDao.getAllPolicies()
    }
}