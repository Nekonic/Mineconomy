package mineconomy.gui

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mineconomy.core.amm.ExchangeService
import mineconomy.core.amm.PoolTable
import mineconomy.core.amm.PriceHistoryTable
import mineconomy.core.db.AccountTable
import mineconomy.core.di.coreModule
import mineconomy.core.economy.MineconomyApiImpl
import mineconomy.gui.command.buildEconomyCommands
import mineconomy.gui.command.buildNpcCommand
import mineconomy.gui.exchange.ExchangeGui
import mineconomy.gui.exchange.TRADEABLE_ITEMS
import mineconomy.gui.listener.PlayerEconomyListener
import mineconomy.gui.npc.NpcListener
import mineconomy.gui.npc.NpcManager
import mineconomy.gui.vault.VaultHook
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class MineconomyGuiPlugin : JavaPlugin() {

    private lateinit var npcManager: NpcManager
    private lateinit var api: MineconomyApiImpl
    private val pluginScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onEnable() {
        saveDefaultConfig()

        if (!initDatabase()) return

        val koin = startKoin { modules(coreModule(pluginScope)) }.koin
        api = koin.get()

        val exchangeService = koin.get<ExchangeService>()
        val exchangeGui = ExchangeGui(this, pluginScope, exchangeService, api)

        npcManager = NpcManager(this)
        npcManager.loadFromConfig()

        server.pluginManager.registerEvents(exchangeGui, this)
        server.pluginManager.registerEvents(NpcListener(npcManager, exchangeGui), this)
        server.pluginManager.registerEvents(PlayerEconomyListener(api, pluginScope), this)

        VaultHook.register(this, api)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            registrar.register(buildNpcCommand(npcManager))
            buildEconomyCommands(api).forEach { registrar.register(it) }
        }

        seedDefaultPools(exchangeService)

        slF4JLogger.info("Mineconomy ${pluginMeta.version} 활성화")
    }

    override fun onDisable() {
        if (::api.isInitialized) VaultHook.unregister(this)
        if (::npcManager.isInitialized) npcManager.removeAll()
        pluginScope.cancel()
        runCatching { stopKoin() }
        slF4JLogger.info("Mineconomy 비활성화")
    }

    private fun initDatabase(): Boolean {
        val db = config.getConfigurationSection("database") ?: run {
            slF4JLogger.error("config.yml에 database 섹션이 없습니다.")
            server.pluginManager.disablePlugin(this)
            return false
        }
        val type = db.getString("type", "mariadb")!!.lowercase()
        val host = db.getString("host", "localhost")!!
        val port = db.getInt("port", 3306)
        val name = db.getString("name", "mineconomy")!!
        val user = db.getString("user", "mineconomy")!!
        val pass = db.getString("password", "1234")!!
        val protocol = if (type == "mysql") "mysql" else "mariadb"
        val sslParam = if (type == "mysql") "useSSL=false" else "sslMode=DISABLED"

        try {
            // 플러그인 classloader에서 드라이버 명시 로드 (DriverManager는 시스템 CL 사용)
            val driverClass = if (type == "mysql") "com.mysql.cj.jdbc.Driver" else "org.mariadb.jdbc.Driver"
            Class.forName(driverClass)

            // DB가 없으면 자동 생성
            java.sql.DriverManager.getConnection(
                "jdbc:$protocol://$host:$port/?$sslParam", user, pass
            ).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate(
                        "CREATE DATABASE IF NOT EXISTS `$name` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
                    )
                }
            }

            val hikariConfig = HikariConfig().apply {
                jdbcUrl         = "jdbc:$protocol://$host:$port/$name?$sslParam"
                username        = user
                password        = pass
                maximumPoolSize = db.getInt("pool-size", 10)
            }
            Database.connect(HikariDataSource(hikariConfig))
            @Suppress("DEPRECATION")
            transaction { SchemaUtils.createMissingTablesAndColumns(AccountTable, PoolTable, PriceHistoryTable) }
        } catch (e: Exception) {
            slF4JLogger.error("데이터베이스 연결 실패: ${e.message}")
            slF4JLogger.error("plugins/Mineconomy/config.yml 에서 database 설정을 확인하세요.")
            server.pluginManager.disablePlugin(this)
            return false
        }
        slF4JLogger.info("데이터베이스 연결 완료")
        return true
    }

    private fun seedDefaultPools(exchangeService: ExchangeService) {
        val poolSection = config.getConfigurationSection("exchange.pools")

        pluginScope.launch {
            TRADEABLE_ITEMS.forEach { item ->
                val sec = poolSection?.getConfigurationSection(item.key.replace("minecraft:", ""))
                val itemRes = sec?.getLong("item-reserve")  ?: defaultItemReserve(item.key)
                val currRes = sec?.getLong("currency-reserve") ?: defaultCurrencyReserve(item.key)
                exchangeService.seedPool(item.key, itemRes, currRes)
            }
        }
    }

    private fun defaultItemReserve(key: String) = when (key) {
        "minecraft:stone"      -> 10_000L
        "minecraft:dirt"       -> 20_000L
        "minecraft:sand"       -> 15_000L
        "minecraft:oak_log"    ->  5_000L
        "minecraft:iron_ingot" ->  2_000L
        "minecraft:gold_ingot" ->  1_000L
        "minecraft:diamond"    ->    500L
        "minecraft:emerald"    ->    800L
        "minecraft:wheat"      ->  5_000L
        "minecraft:potato"     ->  5_000L
        "minecraft:sugar_cane" ->  5_000L
        "minecraft:leather"    ->  2_000L
        "minecraft:bone"       ->  3_000L
        "minecraft:gunpowder"  ->  2_000L
        else                   ->  1_000L
    }

    private fun defaultCurrencyReserve(key: String) = when (key) {
        "minecraft:stone"      ->    10_000L
        "minecraft:dirt"       ->    10_000L
        "minecraft:sand"       ->    10_000L
        "minecraft:oak_log"    ->    25_000L
        "minecraft:iron_ingot" ->   200_000L
        "minecraft:gold_ingot" ->   500_000L
        "minecraft:diamond"    -> 2_500_000L
        "minecraft:emerald"    ->   800_000L
        "minecraft:wheat"      ->    15_000L
        "minecraft:potato"     ->    10_000L
        "minecraft:sugar_cane" ->    15_000L
        "minecraft:leather"    ->    20_000L
        "minecraft:bone"       ->    15_000L
        "minecraft:gunpowder"  ->    60_000L
        else                   ->    10_000L
    }
}
