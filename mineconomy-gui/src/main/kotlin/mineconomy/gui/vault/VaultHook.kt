package mineconomy.gui.vault

import mineconomy.api.MineconomyApi
import mineconomy.api.VaultEconomyAdapter
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

object VaultHook {

    fun register(plugin: JavaPlugin, api: MineconomyApi): Boolean {
        if (plugin.server.pluginManager.getPlugin("Vault") == null) {
            plugin.slF4JLogger.warn("Vault를 찾을 수 없습니다. Vault 연동을 건너뜁니다.")
            return false
        }
        plugin.server.servicesManager.register(
            Economy::class.java,
            VaultEconomyAdapter(api),
            plugin,
            ServicePriority.Normal,
        )
        plugin.slF4JLogger.info("Vault Economy 등록 완료")
        return true
    }

    fun unregister(plugin: JavaPlugin) {
        plugin.server.servicesManager.unregisterAll(plugin)
    }
}
