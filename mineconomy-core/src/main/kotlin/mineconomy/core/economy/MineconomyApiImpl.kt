package mineconomy.core.economy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mineconomy.api.MineconomyApi
import mineconomy.core.db.AccountRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MineconomyApiImpl(
    private val repo: AccountRepository,
    private val scope: CoroutineScope,
) : MineconomyApi {

    private val cache = ConcurrentHashMap<UUID, Long>()

    // ── MineconomyApi ────────────────────────────────────────────────────────

    override fun getBalance(uuid: UUID): Long = cache.getOrDefault(uuid, 0L)

    override fun transfer(from: UUID, to: UUID, amount: Long): Boolean {
        val fromBalance = cache.getOrDefault(from, 0L)
        if (fromBalance < amount) return false
        val newFrom = fromBalance - amount
        val newTo   = cache.getOrDefault(to, 0L) + amount
        cache[from] = newFrom
        cache[to]   = newTo
        scope.launch(Dispatchers.IO) {
            repo.setBalance(from, newFrom)
            repo.setBalance(to,   newTo)
        }
        return true
    }

    // ── 내부 전용 ────────────────────────────────────────────────────────────

    fun deposit(uuid: UUID, amount: Long) {
        val new = cache.merge(uuid, amount, Long::plus)!!
        scope.launch(Dispatchers.IO) { repo.setBalance(uuid, new) }
    }

    fun withdraw(uuid: UUID, amount: Long): Boolean {
        val balance = cache.getOrDefault(uuid, 0L)
        if (balance < amount) return false
        val new = balance - amount
        cache[uuid] = new
        scope.launch(Dispatchers.IO) { repo.setBalance(uuid, new) }
        return true
    }

    // ── 플레이어 생명주기 ──────────────────────────────────────────────────

    suspend fun loadPlayer(uuid: UUID) {
        cache[uuid] = repo.getBalance(uuid)
    }

    fun unloadPlayer(uuid: UUID) {
        val balance = cache.remove(uuid) ?: return
        scope.launch(Dispatchers.IO) { repo.setBalance(uuid, balance) }
    }

    // 사망 패널티: 잔액 3% 소실 (주식/포지션 제외)
    fun applyDeathPenalty(uuid: UUID) {
        val balance = cache.getOrDefault(uuid, 0L)
        val penalty = balance * 3 / 100
        if (penalty > 0) withdraw(uuid, penalty)
    }
}
