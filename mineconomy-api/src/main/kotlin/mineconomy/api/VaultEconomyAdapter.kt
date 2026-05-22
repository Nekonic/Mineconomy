package mineconomy.api

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * Vault Economy 인터페이스의 얇은 구현체.
 * AMM/주식 등 Mineconomy 고유 기능은 [MineconomyApi]를 직접 사용할 것.
 */
class VaultEconomyAdapter(private val api: MineconomyApi) : Economy {

    override fun isEnabled(): Boolean = true
    override fun getName(): String = "Mineconomy"
    override fun hasBankSupport(): Boolean = false
    override fun fractionalDigits(): Int = 0
    override fun format(amount: Double): String = "₩${amount.toLong()}"
    override fun currencyNamePlural(): String = "원"
    override fun currencyNameSingular(): String = "원"

    // ── OfflinePlayer 기반 ────────────────────────────────────────────────

    override fun hasAccount(player: OfflinePlayer): Boolean = true
    override fun hasAccount(player: OfflinePlayer, worldName: String): Boolean = true

    override fun getBalance(player: OfflinePlayer): Double =
        api.getBalance(player.uniqueId).toDouble()

    override fun getBalance(player: OfflinePlayer, world: String): Double =
        getBalance(player)

    override fun has(player: OfflinePlayer, amount: Double): Boolean =
        getBalance(player) >= amount

    override fun has(player: OfflinePlayer, worldName: String, amount: Double): Boolean =
        has(player, amount)

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val longAmount = amount.toLong()
        val balance = api.getBalance(player.uniqueId)
        return if (balance >= longAmount)
            EconomyResponse(amount, (balance - longAmount).toDouble(), EconomyResponse.ResponseType.SUCCESS, "")
        else
            EconomyResponse(0.0, balance.toDouble(), EconomyResponse.ResponseType.FAILURE, "잔액 부족")
    }

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse =
        withdrawPlayer(player, amount)

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val balance = api.getBalance(player.uniqueId)
        return EconomyResponse(amount, (balance + amount.toLong()).toDouble(), EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun depositPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse =
        depositPlayer(player, amount)

    override fun createPlayerAccount(player: OfflinePlayer): Boolean = true
    override fun createPlayerAccount(player: OfflinePlayer, worldName: String): Boolean = true

    // ── String 기반 (deprecated, Bukkit.getOfflinePlayer 위임) ───────────

    @Suppress("DEPRECATION")
    private fun offlineByName(name: String): OfflinePlayer = Bukkit.getOfflinePlayer(name)

    override fun hasAccount(playerName: String): Boolean = true
    override fun hasAccount(playerName: String, worldName: String): Boolean = true
    override fun getBalance(playerName: String): Double = getBalance(offlineByName(playerName))
    override fun getBalance(playerName: String, world: String): Double = getBalance(playerName)
    override fun has(playerName: String, amount: Double): Boolean = has(offlineByName(playerName), amount)
    override fun has(playerName: String, worldName: String, amount: Double): Boolean = has(playerName, amount)
    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse = withdrawPlayer(offlineByName(playerName), amount)
    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse = withdrawPlayer(playerName, amount)
    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse = depositPlayer(offlineByName(playerName), amount)
    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse = depositPlayer(playerName, amount)
    override fun createPlayerAccount(playerName: String): Boolean = true
    override fun createPlayerAccount(playerName: String, worldName: String): Boolean = true

    // ── 은행 기능 미지원 ─────────────────────────────────────────────────

    override fun createBank(name: String, player: OfflinePlayer): EconomyResponse = notImpl()
    override fun createBank(name: String, playerName: String): EconomyResponse = notImpl()
    override fun deleteBank(name: String): EconomyResponse = notImpl()
    override fun bankBalance(name: String): EconomyResponse = notImpl()
    override fun bankHas(name: String, amount: Double): EconomyResponse = notImpl()
    override fun bankWithdraw(name: String, amount: Double): EconomyResponse = notImpl()
    override fun bankDeposit(name: String, amount: Double): EconomyResponse = notImpl()
    override fun isBankOwner(name: String, player: OfflinePlayer): EconomyResponse = notImpl()
    override fun isBankOwner(name: String, playerName: String): EconomyResponse = notImpl()
    override fun isBankMember(name: String, player: OfflinePlayer): EconomyResponse = notImpl()
    override fun isBankMember(name: String, playerName: String): EconomyResponse = notImpl()
    override fun getBanks(): List<String> = emptyList()

    private fun notImpl(): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "미지원")
}
