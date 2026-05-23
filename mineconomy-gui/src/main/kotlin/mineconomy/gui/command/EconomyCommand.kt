package mineconomy.gui.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import mineconomy.core.economy.MineconomyApiImpl
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

fun buildEconomyCommands(api: MineconomyApiImpl): List<LiteralCommandNode<CommandSourceStack>> =
    listOf(buildBalanceCommand(api), buildPayCommand(api))

private fun buildBalanceCommand(api: MineconomyApiImpl): LiteralCommandNode<CommandSourceStack> =
    Commands.literal("balance")
        .executes { ctx ->
            val player = ctx.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
            val balance = api.getBalance(player.uniqueId)
            player.sendMessage(
                Component.text("잔액: ", NamedTextColor.GRAY)
                    .append(Component.text("₩${"%,d".format(balance)}", NamedTextColor.GOLD))
            )
            Command.SINGLE_SUCCESS
        }
        .build()

private fun buildPayCommand(api: MineconomyApiImpl): LiteralCommandNode<CommandSourceStack> =
    Commands.literal("pay")
        .then(
            Commands.argument("target", ArgumentTypes.player())
                .then(
                    Commands.argument("amount", LongArgumentType.longArg(1L))
                        .executes { ctx ->
                            val sender = ctx.source.sender as? Player
                                ?: return@executes Command.SINGLE_SUCCESS
                            val target = ctx
                                .getArgument("target", PlayerSelectorArgumentResolver::class.java)
                                .resolve(ctx.source)
                                .firstOrNull()
                                ?: run {
                                    sender.sendMessage(Component.text("플레이어를 찾을 수 없습니다.", NamedTextColor.RED))
                                    return@executes Command.SINGLE_SUCCESS
                                }
                            if (target == sender) {
                                sender.sendMessage(Component.text("자신에게 송금할 수 없습니다.", NamedTextColor.RED))
                                return@executes Command.SINGLE_SUCCESS
                            }
                            val amount = LongArgumentType.getLong(ctx, "amount")
                            if (!api.transfer(sender.uniqueId, target.uniqueId, amount)) {
                                sender.sendMessage(Component.text("잔액이 부족합니다.", NamedTextColor.RED))
                                return@executes Command.SINGLE_SUCCESS
                            }
                            val fmt = "₩${"%,d".format(amount)}"
                            sender.sendMessage(
                                Component.text("${target.name}에게 ", NamedTextColor.GRAY)
                                    .append(Component.text(fmt, NamedTextColor.GOLD))
                                    .append(Component.text(" 송금 완료.", NamedTextColor.GRAY))
                            )
                            target.sendMessage(
                                Component.text("${sender.name}으로부터 ", NamedTextColor.GRAY)
                                    .append(Component.text(fmt, NamedTextColor.GREEN))
                                    .append(Component.text(" 수령.", NamedTextColor.GRAY))
                            )
                            Command.SINGLE_SUCCESS
                        }
                )
        )
        .build()
