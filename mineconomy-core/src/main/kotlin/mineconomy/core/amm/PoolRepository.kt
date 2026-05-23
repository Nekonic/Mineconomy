package mineconomy.core.amm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PoolRepository {

    suspend fun getPool(itemKey: String): LiquidityPool? = withContext(Dispatchers.IO) {
        transaction {
            PoolTable.selectAll()
                .where { PoolTable.itemKey eq itemKey }
                .singleOrNull()?.toPool()
        }
    }

    suspend fun getAllPools(): List<LiquidityPool> = withContext(Dispatchers.IO) {
        transaction { PoolTable.selectAll().map { it.toPool() } }
    }

    suspend fun updatePool(pool: LiquidityPool): Unit = withContext(Dispatchers.IO) {
        transaction {
            PoolTable.upsert {
                it[itemKey]         = pool.itemKey
                it[itemReserve]     = pool.itemReserve
                it[currencyReserve] = pool.currencyReserve
                it[feeAccumulated]  = pool.feeAccumulated
            }
        }
    }

    /** Insert pool only if it does not exist yet (idempotent on restart). */
    suspend fun seedPool(key: String, itemRes: Long, currRes: Long): Unit = withContext(Dispatchers.IO) {
        transaction {
            val missing = PoolTable.selectAll()
                .where { PoolTable.itemKey eq key }
                .singleOrNull() == null
            if (missing) {
                PoolTable.insert {
                    it[itemKey]         = key
                    it[itemReserve]     = itemRes
                    it[currencyReserve] = currRes
                    it[feeAccumulated]  = 0L
                }
            }
        }
    }

    private fun ResultRow.toPool() = LiquidityPool(
        itemKey         = this[PoolTable.itemKey],
        itemReserve     = this[PoolTable.itemReserve],
        currencyReserve = this[PoolTable.currencyReserve],
        feeAccumulated  = this[PoolTable.feeAccumulated],
    )
}
