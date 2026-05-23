package mineconomy.gui.npc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class NpcListener(private val npcManager: NpcManager) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractAtEntityEvent) {
        val entity = event.rightClicked
        if (entity !is Interaction) return
        val npc = npcManager.findByInteraction(entity.entityId) ?: return
        event.isCancelled = true

        when (npc.type) {
            NpcType.EXCHANGE -> openExchangeGui(event.player)
            NpcType.BANK -> openBankGui(event.player)
        }
    }

    private fun openExchangeGui(player: Player) {
        // TODO: 거래소 GUI 열기
        player.sendMessage(Component.text("[거래소] 아직 구현되지 않았습니다.", NamedTextColor.RED))
    }

    private fun openBankGui(player: Player) {
        // TODO: 은행 GUI 열기
        player.sendMessage(Component.text("[은행] 아직 구현되지 않았습니다.", NamedTextColor.RED))
    }
}
