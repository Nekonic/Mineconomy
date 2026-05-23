package mineconomy.core.amm

import org.jetbrains.exposed.sql.Table

object PoolTable : Table("pools") {
    val itemKey          = varchar("item_key", 64)
    val itemReserve      = long("item_reserve")
    val currencyReserve  = long("currency_reserve")
    val feeAccumulated   = long("fee_accumulated").default(0L)
    override val primaryKey = PrimaryKey(itemKey)
}
