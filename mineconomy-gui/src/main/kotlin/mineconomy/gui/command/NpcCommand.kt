package mineconomy.gui.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import mineconomy.gui.npc.NpcManager
import mineconomy.gui.npc.NpcType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

fun buildNpcCommand(npcManager: NpcManager): LiteralCommandNode<CommandSourceStack> =
    Commands.literal("meco")
        .requires { it.sender.hasPermission("mineconomy.admin") }
        .then(
            Commands.literal("npc")
                .then(
                    Commands.literal("place")
                        .then(
                            Commands.argument("type", StringArgumentType.word())
                                .suggests { _, builder ->
                                    NpcType.entries.forEach { builder.suggest(it.name.lowercase()) }
                                    builder.buildFuture()
                                }
                                .executes { ctx ->
                                    val player = ctx.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                                    val typeName = StringArgumentType.getString(ctx, "type").uppercase()
                                    val type = NpcType.entries.find { it.name == typeName }
                                    if (type == null) {
                                        player.sendMessage(
                                            Component.text("알 수 없는 타입: $typeName — 사용 가능: ${NpcType.entries.joinToString { it.name.lowercase() }}", NamedTextColor.RED)
                                        )
                                        return@executes Command.SINGLE_SUCCESS
                                    }
                                    npcManager.addAndSave(type, player.location)
                                    player.sendMessage(Component.text("${type.name} NPC를 생성했습니다.", NamedTextColor.GREEN))
                                    Command.SINGLE_SUCCESS
                                }
                        )
                )
        )
        .build()
