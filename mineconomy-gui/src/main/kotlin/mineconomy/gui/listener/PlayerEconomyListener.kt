package mineconomy.gui.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mineconomy.core.economy.MineconomyApiImpl
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEconomyListener(
    private val api: MineconomyApiImpl,
    private val scope: CoroutineScope,
) : Listener {

    // 로그인 전 비동기 스레드에서 DB 조회 → 캐시 적재
    @EventHandler
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        scope.launch { api.loadPlayer(event.uniqueId) }
    }

    // 종료 시 캐시 제거 + 비동기 DB 저장
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        api.unloadPlayer(event.player.uniqueId)
    }

    // 사망 패널티: 잔액 3% 소실
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        api.applyDeathPenalty(event.player.uniqueId)
    }
}
