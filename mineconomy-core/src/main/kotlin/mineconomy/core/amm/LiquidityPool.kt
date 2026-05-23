package mineconomy.core.amm

data class LiquidityPool(
    val itemKey: String,
    val itemReserve: Long,
    val currencyReserve: Long,
    val feeAccumulated: Long = 0L,
) {
    val spotPrice: Long
        get() = if (itemReserve == 0L) 0L else currencyReserve / itemReserve
}
