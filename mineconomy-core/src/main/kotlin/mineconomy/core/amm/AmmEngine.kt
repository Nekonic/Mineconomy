package mineconomy.core.amm

object AmmEngine {

    private const val FEE_NUM = 25L   // 2.5% = 25/1000
    private const val FEE_DEN = 1000L

    /** Items received when paying [currencyIn]. Does not modify the pool. */
    fun calcBuy(pool: LiquidityPool, currencyIn: Long): Long {
        val fee   = currencyIn * FEE_NUM / FEE_DEN
        val netIn = currencyIn - fee
        return pool.itemReserve * netIn / (pool.currencyReserve + netIn)
    }

    /** Currency received when selling [itemIn] items. Does not modify the pool. */
    fun calcSell(pool: LiquidityPool, itemIn: Long): Long {
        val gross = pool.currencyReserve * itemIn / (pool.itemReserve + itemIn)
        return gross - gross * FEE_NUM / FEE_DEN
    }

    /**
     * Minimum currency needed to receive at least [itemQty] items.
     * Returns Long.MAX_VALUE if the pool cannot supply that many items.
     */
    fun calcBuyCost(pool: LiquidityPool, itemQty: Long): Long {
        if (itemQty <= 0 || itemQty >= pool.itemReserve) return Long.MAX_VALUE
        // invert: itemOut = x * netIn / (y + netIn)  →  netIn = itemOut * y / (x - itemOut)
        val netIn = itemQty * pool.currencyReserve / (pool.itemReserve - itemQty)
        // ceiling division to get minimum currencyIn such that fee deduction still leaves enough
        return (netIn * FEE_DEN + (FEE_DEN - FEE_NUM) - 1) / (FEE_DEN - FEE_NUM)
    }

    /**
     * Execute a buy: pay [currencyIn] currency, receive items.
     * @return (updated pool, items given to user) or null if the trade is invalid.
     */
    fun executeBuy(pool: LiquidityPool, currencyIn: Long): Pair<LiquidityPool, Long>? {
        if (currencyIn <= 0) return null
        val fee     = currencyIn * FEE_NUM / FEE_DEN
        val netIn   = currencyIn - fee
        val itemOut = pool.itemReserve * netIn / (pool.currencyReserve + netIn)
        if (itemOut <= 0 || itemOut >= pool.itemReserve) return null
        return pool.copy(
            itemReserve     = pool.itemReserve - itemOut,
            currencyReserve = pool.currencyReserve + netIn,
            feeAccumulated  = pool.feeAccumulated + fee,
        ) to itemOut
    }

    /**
     * Execute a sell: give [itemIn] items, receive currency.
     * @return (updated pool, currency given to user) or null if the trade is invalid.
     */
    fun executeSell(pool: LiquidityPool, itemIn: Long): Pair<LiquidityPool, Long>? {
        if (itemIn <= 0) return null
        val gross   = pool.currencyReserve * itemIn / (pool.itemReserve + itemIn)
        val fee     = gross * FEE_NUM / FEE_DEN
        val currOut = gross - fee
        if (currOut <= 0) return null
        return pool.copy(
            itemReserve     = pool.itemReserve + itemIn,
            currencyReserve = pool.currencyReserve - gross,
            feeAccumulated  = pool.feeAccumulated + fee,
        ) to currOut
    }
}
