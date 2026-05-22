package mineconomy.gui

import org.bukkit.plugin.java.JavaPlugin

/**
 * 배포 진입점 플러그인.
 * core 로직을 초기화한 뒤 GUI 레이어를 올린다.
 */
class MineconomyGuiPlugin : JavaPlugin() {

    override fun onEnable() {
        // TODO: core 초기화, GUI 레지스트리 등록
        slF4JLogger.info("Mineconomy GUI ${pluginMeta.version} 활성화")
    }

    override fun onDisable() {
        slF4JLogger.info("Mineconomy GUI 비활성화")
    }
}