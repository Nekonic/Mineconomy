package mineconomy.core.amm

import org.jetbrains.exposed.sql.Table

object PriceHistoryTable : Table("price_history") {
    val itemKey    = varchar("item_key", 64).index()
    val recordedAt = long("recorded_at")
    val spotPrice  = long("spot_price")
}
