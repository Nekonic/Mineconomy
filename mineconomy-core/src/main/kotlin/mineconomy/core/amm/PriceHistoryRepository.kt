package mineconomy.core.amm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PriceHistoryRepository {

    suspend fun record(itemKey: String, price: Long): Unit = withContext(Dispatchers.IO) {
        transaction {
            PriceHistoryTable.insert {
                it[PriceHistoryTable.itemKey]    = itemKey
                it[PriceHistoryTable.recordedAt] = System.currentTimeMillis()
                it[PriceHistoryTable.spotPrice]  = price
            }
        }
    }

    suspend fun getRecent(itemKey: String, limit: Int = 4): List<Long> = withContext(Dispatchers.IO) {
        transaction {
            PriceHistoryTable
                .selectAll()
                .where { PriceHistoryTable.itemKey eq itemKey }
                .orderBy(PriceHistoryTable.recordedAt, SortOrder.DESC)
                .limit(limit)
                .map { it[PriceHistoryTable.spotPrice] }
                .reversed()
        }
    }
}
