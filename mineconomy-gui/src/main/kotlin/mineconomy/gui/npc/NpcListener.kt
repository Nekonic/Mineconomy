package mineconomy.gui.npc

import mineconomy.gui.exchange.ExchangeGui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Interaction
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class NpcListener(
    private val npcManager: NpcManager,
    private val exchangeGui: ExchangeGui,
) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractAtEntityEvent) {
        val entity = event.rightClicked
        if (entity !is Interaction) return
        val npc = npcManager.findByInteraction(entity.entityId) ?: return
        event.isCancelled = true

        when (npc.type) {
            NpcType.EXCHANGE -> exchangeGui.open(event.player)
            NpcType.BANK     -> event.player.sendMessage(
                Component.text("[은행] 아직 구현되지 않았습니다.", NamedTextColor.RED))
        }
    }
}
