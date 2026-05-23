package mineconomy.gui.exchange

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

data class TradeableItem(
    val key: String,
    val material: Material,
    val displayName: Component,
    val name: String,
)

private fun label(text: String, color: NamedTextColor): Component =
    Component.text(text, color).decoration(TextDecoration.ITALIC, false)

val TRADEABLE_ITEMS: List<TradeableItem> = listOf(
    // 블록
    TradeableItem("minecraft:stone",      Material.STONE,      label("돌",         NamedTextColor.GRAY),       "돌"),
    TradeableItem("minecraft:dirt",       Material.DIRT,       label("흙",         NamedTextColor.DARK_GREEN), "흙"),
    TradeableItem("minecraft:sand",       Material.SAND,       label("모래",       NamedTextColor.YELLOW),     "모래"),
    TradeableItem("minecraft:oak_log",    Material.OAK_LOG,    label("참나무 원목", NamedTextColor.DARK_GREEN), "참나무 원목"),
    // 광물
    TradeableItem("minecraft:iron_ingot", Material.IRON_INGOT, label("철 주괴",    NamedTextColor.WHITE),      "철 주괴"),
    TradeableItem("minecraft:gold_ingot", Material.GOLD_INGOT, label("금 주괴",    NamedTextColor.GOLD),       "금 주괴"),
    TradeableItem("minecraft:diamond",    Material.DIAMOND,    label("다이아몬드",  NamedTextColor.AQUA),       "다이아몬드"),
    TradeableItem("minecraft:emerald",    Material.EMERALD,    label("에메랄드",   NamedTextColor.GREEN),      "에메랄드"),
    // 농산물
    TradeableItem("minecraft:wheat",      Material.WHEAT,      label("밀",         NamedTextColor.YELLOW),     "밀"),
    TradeableItem("minecraft:potato",     Material.POTATO,     label("감자",       NamedTextColor.YELLOW),     "감자"),
    TradeableItem("minecraft:sugar_cane", Material.SUGAR_CANE, label("사탕수수",   NamedTextColor.GREEN),      "사탕수수"),
    // 몹 드롭
    TradeableItem("minecraft:leather",    Material.LEATHER,    label("가죽",       NamedTextColor.DARK_RED),   "가죽"),
    TradeableItem("minecraft:bone",       Material.BONE,       label("뼈",         NamedTextColor.WHITE),      "뼈"),
    TradeableItem("minecraft:gunpowder",  Material.GUNPOWDER,  label("화약",       NamedTextColor.DARK_GRAY),  "화약"),
)

val TRADEABLE_BY_KEY: Map<String, TradeableItem> = TRADEABLE_ITEMS.associateBy { it.key }
