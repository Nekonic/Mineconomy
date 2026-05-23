package mineconomy.gui

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import mineconomy.gui.command.buildNpcCommand
import mineconomy.gui.npc.NpcListener
import mineconomy.gui.npc.NpcManager
import org.bukkit.plugin.java.JavaPlugin

class MineconomyGuiPlugin : JavaPlugin() {

    private lateinit var npcManager: NpcManager

    override fun onEnable() {
        saveDefaultConfig()

        npcManager = NpcManager(this)
        npcManager.loadFromConfig()

        server.pluginManager.registerEvents(NpcListener(npcManager), this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(buildNpcCommand(npcManager))
        }

        slF4JLogger.info("Mineconomy ${pluginMeta.version} 활성화")
    }

    override fun onDisable() {
        npcManager.removeAll()
        slF4JLogger.info("Mineconomy 비활성화")
    }
}
