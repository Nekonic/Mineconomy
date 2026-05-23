package mineconomy.gui.npc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

enum class NpcType(val label: Component, val icon: Material) {
    EXCHANGE(Component.text("거래소", NamedTextColor.GOLD), Material.GOLD_INGOT),
    BANK(Component.text("은행", NamedTextColor.GREEN), Material.EMERALD),
}
