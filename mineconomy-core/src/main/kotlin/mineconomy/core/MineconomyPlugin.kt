package mineconomy.core

import org.bukkit.plugin.java.JavaPlugin

class MineconomyPlugin : JavaPlugin() {

    override fun onEnable() {
        slF4JLogger.info("Mineconomy ${pluginMeta.version} 활성화")
    }

    override fun onDisable() {
        slF4JLogger.info("Mineconomy 비활성화")
    }
}