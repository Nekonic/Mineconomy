package mineconomy.gui.exchange

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mineconomy.core.amm.AmmEngine
import mineconomy.core.amm.ExchangeService
import mineconomy.core.amm.LiquidityPool
import mineconomy.core.amm.TradeResult
import mineconomy.core.economy.MineconomyApiImpl
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.components.CustomModelDataComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// ── 상수 ─────────────────────────────────────────────────────────────────────

private val LIST_ITEM_SLOTS: List<Int> = buildList {
    for (row in 1..4) for (col in 1..7) add(row * 9 + col)
} // 10-16, 19-25, 28-34, 37-43

private val QTY_OPTIONS = longArrayOf(1, 8, 16, 32, 64)
private val QTY_SLOTS   = intArrayOf(10, 11, 12, 13, 14)

// ── 세션 상태 ─────────────────────────────────────────────────────────────────

private sealed interface GuiScreen {
    val inv: Inventory
}
private data class ListScreen(override val inv: Inventory, val page: Int) : GuiScreen
private data class TradeScreen(
    override val inv: Inventory,
    val item: TradeableItem,
    val pool: LiquidityPool,
    val qty: Long,
    val listPage: Int,
) : GuiScreen
private data class ChartScreen(
    override val inv: Inventory,
    val item: TradeableItem,
    val listPage: Int,
) : GuiScreen

// ── GUI 클래스 ────────────────────────────────────────────────────────────────

class ExchangeGui(
    private val plugin: JavaPlugin,
    private val scope: CoroutineScope,
    private val exchangeService: ExchangeService,
    private val api: MineconomyApiImpl,
) : Listener {

    private val sessions = ConcurrentHashMap<UUID, GuiScreen>()

    fun open(player: Player) {
        scope.launch {
            val pools = exchangeService.getAllPools().associateBy { it.itemKey }
            runOnMain { showList(player, pools, 0) }
        }
    }

    // ── 리스트 화면 ───────────────────────────────────────────────────────────

    private fun showList(player: Player, pools: Map<String, LiquidityPool>, page: Int) {
        if (!player.isOnline) return
        val inv = Bukkit.createInventory(null, 54,
            tx("거래소", NamedTextColor.GOLD))
        fillBorder(inv)

        val items = TRADEABLE_ITEMS
        items.drop(page * 28).take(28).forEachIndexed { i, entry ->
            inv.setItem(LIST_ITEM_SLOTS[i], buildListItem(entry, pools[entry.key]))
        }

        if (page > 0)
            inv.setItem(45, navItem(Material.ARROW, "◀ 이전"))
        if ((page + 1) * 28 < items.size)
            inv.setItem(53, navItem(Material.ARROW, "다음 ▶"))
        inv.setItem(49, navItem(Material.BARRIER, "닫기"))

        sessions[player.uniqueId] = ListScreen(inv, page)
        player.openInventory(inv)
    }

    private fun buildListItem(entry: TradeableItem, pool: LiquidityPool?): ItemStack {
        val stack = ItemStack(entry.material)
        val meta  = stack.itemMeta
        meta.displayName(entry.displayName)
        val price = pool?.spotPrice ?: 0L
        meta.lore(listOf(
            tx("현재 가격: ₩${price.fmt()}/개", NamedTextColor.YELLOW),
            Component.empty(),
            tx("클릭: 거래", NamedTextColor.GRAY),
        ))
        stack.itemMeta = meta
        return stack
    }

    // ── 거래 화면 ─────────────────────────────────────────────────────────────

    private fun showTrade(player: Player, entry: TradeableItem, pool: LiquidityPool, qty: Long, listPage: Int) {
        if (!player.isOnline) return
        val inv = Bukkit.createInventory(null, 27,
            Component.text("거래 — ", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .append(entry.displayName))
        fillBorder(inv)

        inv.setItem(4, buildItemInfo(entry, pool, qty, player))

        QTY_OPTIONS.forEachIndexed { i, q ->
            inv.setItem(QTY_SLOTS[i], buildQtyBtn(q, q == qty))
        }

        val buyCost = AmmEngine.calcBuyCost(pool, qty)
        val sellRev = AmmEngine.calcSell(pool, qty)
        inv.setItem(18, buildActionBtn(Material.LIME_TERRACOTTA, "구매", NamedTextColor.GREEN, listOf(
            tx("${qty}개 구매", NamedTextColor.WHITE),
            tx("예상 비용: ₩${if (buyCost == Long.MAX_VALUE) "∞" else buyCost.fmt()}", NamedTextColor.YELLOW),
        )))
        inv.setItem(22, buildActionBtn(Material.RED_TERRACOTTA, "판매", NamedTextColor.RED, listOf(
            tx("${qty}개 판매", NamedTextColor.WHITE),
            tx("예상 수익: ₩${sellRev.fmt()}", NamedTextColor.YELLOW),
        )))
        inv.setItem(6, navItem(Material.CLOCK, "가격 차트 ▶"))
        inv.setItem(26, navItem(Material.ARROW, "◀ 돌아가기"))

        sessions[player.uniqueId] = TradeScreen(inv, entry, pool, qty, listPage)
        player.openInventory(inv)
    }

    private fun buildItemInfo(entry: TradeableItem, pool: LiquidityPool, qty: Long, player: Player): ItemStack {
        val stack = ItemStack(entry.material)
        val meta  = stack.itemMeta
        meta.displayName(entry.displayName)
        meta.lore(listOf(
            tx("현재 가격: ₩${pool.spotPrice.fmt()}/개", NamedTextColor.YELLOW),
            tx("선택 수량: ${qty}개", NamedTextColor.WHITE),
            Component.empty(),
            tx("보유 잔액: ₩${api.getBalance(player.uniqueId).fmt()}", NamedTextColor.AQUA),
            tx("보유 수량: ${countItems(player, entry.material)}개", NamedTextColor.AQUA),
        ))
        stack.itemMeta = meta
        return stack
    }

    private fun buildQtyBtn(qty: Long, selected: Boolean): ItemStack {
        val mat  = if (selected) Material.LIME_STAINED_GLASS_PANE else Material.GRAY_STAINED_GLASS_PANE
        val name = Component.text("×${qty}", if (selected) NamedTextColor.GREEN else NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .let { if (selected) it.decorate(TextDecoration.BOLD) else it }
        val stack = ItemStack(mat)
        val meta  = stack.itemMeta
        meta.displayName(name)
        stack.itemMeta = meta
        return stack
    }

    private fun buildActionBtn(mat: Material, label: String, color: NamedTextColor, lore: List<Component>): ItemStack {
        val stack = ItemStack(mat)
        val meta  = stack.itemMeta
        meta.displayName(tx(label, color).decorate(TextDecoration.BOLD))
        meta.lore(lore)
        stack.itemMeta = meta
        return stack
    }

    // ── 이벤트 핸들러 ─────────────────────────────────────────────────────────

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val screen = sessions[player.uniqueId] ?: return
        if (event.inventory != screen.inv) return
        event.isCancelled = true
        if (event.clickedInventory != screen.inv) return
        when (screen) {
            is ListScreen  -> handleListClick(player, screen, event.slot)
            is TradeScreen -> handleTradeClick(player, screen, event.slot)
            is ChartScreen -> handleChartClick(player, screen, event.slot)
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val screen = sessions[player.uniqueId] ?: return
        if (event.inventory == screen.inv) sessions.remove(player.uniqueId)
    }

    // ── 클릭 처리 ─────────────────────────────────────────────────────────────

    private fun handleListClick(player: Player, screen: ListScreen, slot: Int) {
        val idx = LIST_ITEM_SLOTS.indexOf(slot)
        if (idx >= 0) {
            val entry = TRADEABLE_ITEMS.getOrNull(screen.page * 28 + idx) ?: return
            scope.launch {
                val pool = exchangeService.getPool(entry.key) ?: return@launch
                runOnMain { showTrade(player, entry, pool, QTY_OPTIONS[0], screen.page) }
            }
            return
        }
        when (slot) {
            45   -> reloadList(player, screen.page - 1)
            49   -> player.closeInventory()
            53   -> reloadList(player, screen.page + 1)
        }
    }

    private fun handleTradeClick(player: Player, screen: TradeScreen, slot: Int) {
        val qtyIdx = QTY_SLOTS.indexOf(slot)
        if (qtyIdx >= 0) {
            showTrade(player, screen.item, screen.pool, QTY_OPTIONS[qtyIdx], screen.listPage)
            return
        }
        when (slot) {
            6    -> openChart(player, screen.item, screen.listPage)
            18   -> executeBuy(player, screen)
            22   -> executeSell(player, screen)
            26   -> reloadList(player, screen.listPage)
        }
    }

    // ── 거래 실행 ─────────────────────────────────────────────────────────────

    private fun executeBuy(player: Player, screen: TradeScreen) {
        val cost = AmmEngine.calcBuyCost(screen.pool, screen.qty)
        if (cost == Long.MAX_VALUE) {
            player.sendMessage(tx("[거래소] 유동성이 부족합니다.", NamedTextColor.RED))
            return
        }
        if (api.getBalance(player.uniqueId) < cost) {
            player.sendMessage(tx("[거래소] 잔액이 부족합니다.", NamedTextColor.RED))
            return
        }
        scope.launch {
            when (val result = exchangeService.buy(player.uniqueId, screen.item.key, cost)) {
                is TradeResult.Success -> {
                    val itemOut = result.amount.toInt().coerceAtLeast(1)
                    runOnMain {
                        val leftover = player.inventory.addItem(ItemStack(screen.item.material, itemOut))
                        leftover.values.forEach { player.world.dropItem(player.location, it) }
                        player.sendMessage(
                            Component.text("[거래소] ", NamedTextColor.GOLD)
                                .append(tx("${screen.item.name} ${result.amount}개 구매 완료 " +
                                    "(₩${cost.fmt()} 지불)", NamedTextColor.GREEN))
                        )
                        refreshTrade(player, screen)
                    }
                }
                is TradeResult.Failure ->
                    runOnMain { player.sendMessage(tx("[거래소] ${result.reason}", NamedTextColor.RED)) }
            }
        }
    }

    private fun executeSell(player: Player, screen: TradeScreen) {
        val available = countItems(player, screen.item.material)
        val sellQty   = screen.qty.coerceAtMost(available)
        if (sellQty <= 0) {
            player.sendMessage(tx("[거래소] 보유한 ${screen.item.name}이(가) 없습니다.", NamedTextColor.RED))
            return
        }
        removeItems(player, screen.item.material, sellQty.toInt())
        scope.launch {
            when (val result = exchangeService.sell(player.uniqueId, screen.item.key, sellQty)) {
                is TradeResult.Success ->
                    runOnMain {
                        player.sendMessage(
                            Component.text("[거래소] ", NamedTextColor.GOLD)
                                .append(tx("${screen.item.name} ${sellQty}개 판매 완료 " +
                                    "(₩${result.amount.fmt()} 수령)", NamedTextColor.GREEN))
                        )
                        refreshTrade(player, screen)
                    }
                is TradeResult.Failure ->
                    runOnMain {
                        val refund = ItemStack(screen.item.material, sellQty.toInt())
                        player.inventory.addItem(refund).values
                            .forEach { player.world.dropItem(player.location, it) }
                        player.sendMessage(tx("[거래소] ${result.reason}", NamedTextColor.RED))
                    }
            }
        }
    }

    private fun refreshTrade(player: Player, screen: TradeScreen) {
        scope.launch {
            val pool = exchangeService.getPool(screen.item.key) ?: return@launch
            runOnMain { showTrade(player, screen.item, pool, screen.qty, screen.listPage) }
        }
    }

    private fun reloadList(player: Player, page: Int) {
        scope.launch {
            val pools = exchangeService.getAllPools().associateBy { it.itemKey }
            runOnMain { showList(player, pools, page) }
        }
    }

    // ── 차트 화면 ─────────────────────────────────────────────────────────────

    private fun openChart(player: Player, item: TradeableItem, listPage: Int) {
        scope.launch {
            val history = exchangeService.getPriceHistory(item.key, 4)
            runOnMain { showChart(player, item, history, listPage) }
        }
    }

    private fun showChart(player: Player, item: TradeableItem, history: List<Long>, listPage: Int) {
        if (!player.isOnline) return
        // "샮" 문자 → default.json의 custom_graph3.png 비트맵 폰트 렌더링 (커스텀 GUI 배경)
        val inv = Bukkit.createInventory(null, 54,
            Component.text("궯샮", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false))

        inv.setItem(45, navItem(Material.ARROW, "◀ 돌아가기"))

        if (history.isEmpty()) {
            inv.setItem(22, navItem(Material.BARRIER, "거래 데이터 없음"))
        } else {
            val minPrice   = history.min()
            val maxPrice   = history.max()
            val range      = maxPrice - minPrice
            val dataPoints = history.size.coerceAtMost(4)
            for (i in 0 until dataPoints) {
                val price  = history[history.size - dataPoints + i]
                val prev   = if (i > 0) history[history.size - dataPoints + i - 1] else null
                val level  = if (range == 0L) 4
                             else ((price - minPrice) * 7 / range + 1).toInt().coerceIn(1, 8)
                val isLast = i == dataPoints - 1
                drawBar(inv, i, level, barColor(price, prev), price, prev,
                        if (isLast) maxPrice else null, if (isLast) minPrice else null)
            }
            inv.setItem(49, buildPriceInfo(item, history.last()))
        }

        sessions[player.uniqueId] = ChartScreen(inv, item, listPage)
        player.openInventory(inv)
    }

    private fun drawBar(
        inv: Inventory, dataPointIdx: Int, level: Int, color: Color,
        price: Long, prev: Long?, highPrice: Long?, lowPrice: Long?,
    ) {
        if (level == 0) return
        val base     = dataPointIdx * 2
        val fullRows = level / 2
        val hasHalf  = level % 2 == 1

        val nameColor = when {
            prev == null || price == prev -> NamedTextColor.GRAY
            price > prev                 -> NamedTextColor.RED
            else                         -> NamedTextColor.BLUE
        }
        val name = tx("₩${price.fmt()}", nameColor)
        val lore = buildList<Component> {
            if (prev != null) {
                val delta    = price - prev
                val absDelta = if (delta < 0) -delta else delta
                val pct      = if (prev > 0) delta * 100.0 / prev else 0.0
                val arrow    = if (delta > 0) "▲" else if (delta < 0) "▼" else "━"
                val loreColor = when {
                    delta > 0 -> NamedTextColor.RED
                    delta < 0 -> NamedTextColor.BLUE
                    else      -> NamedTextColor.GRAY
                }
                add(tx("$arrow ${absDelta.fmt()} (${"%.2f".format(pct)}%)", loreColor))
            }
            if (highPrice != null && lowPrice != null) {
                add(Component.empty())
                add(tx("고가  ₩${highPrice.fmt()}", NamedTextColor.RED))
                add(tx("저가  ₩${lowPrice.fmt()}", NamedTextColor.BLUE))
            }
        }

        for (r in 0 until fullRows) {
            val row = 3 - r
            inv.setItem(row * 9 + base,     chartPiece(1, color, name, lore))
            inv.setItem(row * 9 + base + 1, chartPiece(1, color, name, lore))
        }
        if (hasHalf) {
            val row = 3 - fullRows
            if (row >= 0) {
                inv.setItem(row * 9 + base,     chartPiece(2, color, name, lore))
                inv.setItem(row * 9 + base + 1, chartPiece(2, color, name, lore))
            }
        }
    }

    // 한국식: 상승=빨강, 하락=파랑, 기준(첫 번째)=회색
    private fun barColor(price: Long, prev: Long?): Color = when {
        prev == null || price == prev -> Color.fromRGB(150, 150, 150)
        price > prev                 -> Color.fromRGB(220,  50,  50)
        else                         -> Color.fromRGB( 50, 100, 220)
    }

    private fun handleChartClick(player: Player, screen: ChartScreen, slot: Int) {
        if (slot == 45) {
            scope.launch {
                val pool = exchangeService.getPool(screen.item.key) ?: return@launch
                runOnMain { showTrade(player, screen.item, pool, QTY_OPTIONS[0], screen.listPage) }
            }
        }
    }

    private fun buildPriceInfo(item: TradeableItem, price: Long): ItemStack {
        val stack = ItemStack(item.material)
        val meta  = stack.itemMeta
        meta.displayName(item.displayName)
        meta.lore(listOf(tx("현재 가격: ₩${price.fmt()}/개", NamedTextColor.YELLOW)))
        stack.itemMeta = meta
        return stack
    }

    private fun chartPiece(
        cmd: Int, color: Color,
        name: Component? = null, lore: List<Component> = emptyList(),
    ): ItemStack {
        val stack = ItemStack(Material.POTION)
        val meta  = stack.itemMeta as PotionMeta
        meta.color = color
        val cmdData = meta.customModelDataComponent
        cmdData.floats = listOf(cmd.toFloat())
        meta.setCustomModelDataComponent(cmdData)
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        if (name != null) {
            meta.displayName(name)
            if (lore.isNotEmpty()) meta.lore(lore)
        } else {
            meta.setHideTooltip(true)
        }
        stack.itemMeta = meta
        return stack
    }

    // ── 인벤토리 유틸 ─────────────────────────────────────────────────────────

    private fun fillBorder(inv: Inventory) {
        for (i in 0 until inv.size) {
            val row = i / 9
            val col = i % 9
            val isBorder = row == 0 || row == inv.size / 9 - 1 || col == 0 || col == 8
            if (isBorder) inv.setItem(i, borderPane())
        }
    }

    private fun borderPane(): ItemStack {
        val stack = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta  = stack.itemMeta
        meta.setHideTooltip(true)
        stack.itemMeta = meta
        return stack
    }

    private fun navItem(mat: Material, label: String): ItemStack {
        val stack = ItemStack(mat)
        val meta  = stack.itemMeta
        meta.displayName(tx(label, NamedTextColor.WHITE))
        stack.itemMeta = meta
        return stack
    }

    private fun countItems(player: Player, mat: Material): Long =
        player.inventory.contents
            .filterNotNull()
            .filter { it.type == mat }
            .sumOf { it.amount.toLong() }

    private fun removeItems(player: Player, mat: Material, amount: Int) {
        var remaining = amount
        for (slot in 0 until player.inventory.size) {
            if (remaining <= 0) break
            val item = player.inventory.getItem(slot) ?: continue
            if (item.type != mat) continue
            if (item.amount <= remaining) {
                remaining -= item.amount
                player.inventory.setItem(slot, null)
            } else {
                item.amount -= remaining
                remaining = 0
            }
        }
    }

    private fun runOnMain(block: () -> Unit) =
        Bukkit.getScheduler().runTask(plugin, Runnable(block))
}

// ── 파일 레벨 유틸 ────────────────────────────────────────────────────────────

private fun tx(text: String, color: NamedTextColor): Component =
    Component.text(text, color).decoration(TextDecoration.ITALIC, false)

private fun Long.fmt(): String = "%,d".format(this)
