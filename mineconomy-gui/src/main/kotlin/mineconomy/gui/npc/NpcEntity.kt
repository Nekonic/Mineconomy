package mineconomy.gui.npc

import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay

class NpcEntity(
    val type: NpcType,
    val interaction: Interaction,
    val label: TextDisplay,
    val icon: ItemDisplay,
) {
    fun remove() {
        interaction.remove()
        label.remove()
        icon.remove()
    }
}
