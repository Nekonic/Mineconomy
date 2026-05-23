package mineconomy.core.amm

import mineconomy.core.economy.MineconomyApiImpl
import java.util.UUID

sealed class TradeResult {
    data class Success(val amount: Long) : TradeResult()
    data class Failure(val reason: String) : TradeResult()
}

class ExchangeService(
    private val poolRepo: PoolRepository,
    private val priceHistoryRepo: PriceHistoryRepository,
    private val api: MineconomyApiImpl,
) {
    suspend fun getPool(itemKey: String): LiquidityPool? = poolRepo.getPool(itemKey)
    suspend fun getAllPools(): List<LiquidityPool> = poolRepo.getAllPools()
    suspend fun getPriceHistory(itemKey: String, limit: Int = 4): List<Long> =
        priceHistoryRepo.getRecent(itemKey, limit)

    suspend fun buy(uuid: UUID, itemKey: String, currencyIn: Long): TradeResult {
        if (currencyIn <= 0) return TradeResult.Failure("금액이 0보다 커야 합니다.")
        if (api.getBalance(uuid) < currencyIn) return TradeResult.Failure("잔액이 부족합니다.")
        val pool = poolRepo.getPool(itemKey) ?: return TradeResult.Failure("거래 불가 아이템입니다.")
        val (newPool, itemOut) = AmmEngine.executeBuy(pool, currencyIn)
            ?: return TradeResult.Failure("거래를 실행할 수 없습니다. (유동성 부족)")
        api.withdraw(uuid, currencyIn)
        poolRepo.updatePool(newPool)
        priceHistoryRepo.record(itemKey, newPool.spotPrice)
        return TradeResult.Success(itemOut)
    }

    suspend fun sell(uuid: UUID, itemKey: String, itemIn: Long): TradeResult {
        if (itemIn <= 0) return TradeResult.Failure("수량이 0보다 커야 합니다.")
        val pool = poolRepo.getPool(itemKey) ?: return TradeResult.Failure("거래 불가 아이템입니다.")
        val (newPool, currencyOut) = AmmEngine.executeSell(pool, itemIn)
            ?: return TradeResult.Failure("거래를 실행할 수 없습니다. (유동성 부족)")
        api.deposit(uuid, currencyOut)
        poolRepo.updatePool(newPool)
        priceHistoryRepo.record(itemKey, newPool.spotPrice)
        return TradeResult.Success(currencyOut)
    }

    suspend fun seedPool(key: String, itemRes: Long, currRes: Long) {
        poolRepo.seedPool(key, itemRes, currRes)
        poolRepo.getPool(key)?.let { priceHistoryRepo.record(key, it.spotPrice) }
    }
}
