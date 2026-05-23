package mineconomy.gui.npc

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class NpcManager(private val plugin: JavaPlugin) {

    private val npcs = mutableListOf<NpcEntity>()

    fun loadFromConfig() {
        plugin.config.getMapList("npcs").forEach { entry ->
            val typeName = entry["type"] as? String ?: return@forEach
            val type = NpcType.entries.find { it.name == typeName } ?: return@forEach
            val worldName = entry["world"] as? String ?: return@forEach
            val world = Bukkit.getWorld(worldName) ?: run {
                plugin.slF4JLogger.warn("NPC 로드 실패: 월드 '$worldName' 없음")
                return@forEach
            }
            val x = (entry["x"] as? Number)?.toDouble() ?: return@forEach
            val y = (entry["y"] as? Number)?.toDouble() ?: return@forEach
            val z = (entry["z"] as? Number)?.toDouble() ?: return@forEach
            val yaw = (entry["yaw"] as? Number)?.toFloat() ?: 0f
            spawnNpc(type, Location(world, x, y, z, yaw, 0f))
        }
        plugin.slF4JLogger.info("NPC ${npcs.size}개 로드 완료")
    }

    fun addAndSave(type: NpcType, location: Location) {
        spawnNpc(type, location)
        val list = plugin.config.getMapList("npcs").toMutableList()
        list.add(
            mapOf(
                "type" to type.name,
                "world" to (location.world.name),
                "x" to location.x,
                "y" to location.y,
                "z" to location.z,
                "yaw" to location.yaw,
            )
        )
        plugin.config.set("npcs", list)
        plugin.saveConfig()
    }

    fun removeAll() {
        npcs.forEach { it.remove() }
        npcs.clear()
    }

    fun findByInteraction(entityId: Int): NpcEntity? =
        npcs.find { it.interaction.entityId == entityId }

    private fun spawnNpc(type: NpcType, location: Location): NpcEntity {
        val world = location.world

        val label = world.spawn(location.clone().add(0.0, 2.3, 0.0), TextDisplay::class.java) { td ->
            td.text(type.label)
            td.billboard = Display.Billboard.CENTER
            td.isPersistent = false
        }

        val icon = world.spawn(location.clone().add(0.0, 1.7, 0.0), ItemDisplay::class.java) { id ->
            id.setItemStack(ItemStack(type.icon))
            id.billboard = Display.Billboard.CENTER
            id.isPersistent = false
        }

        val interaction = world.spawn(location, Interaction::class.java) { it ->
            it.interactionWidth = 0.6f
            it.interactionHeight = 1.8f
            it.isResponsive = false
            it.isPersistent = false
        }

        return NpcEntity(type, interaction, label, icon).also { npcs.add(it) }
    }
}
