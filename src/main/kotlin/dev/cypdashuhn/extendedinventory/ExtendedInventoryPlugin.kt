package dev.cypdashuhn.extendedinventory

import dev.cypdashuhn.extendedinventory.commands.registerCommands
import dev.cypdashuhn.extendedinventory.db.initDb
import dev.cypdashuhn.extendedinventory.hotbar.HotbarListener
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.ChatInputManager
import dev.cypdashuhn.extendedinventory.ui.initUi
import dev.rooster.commands.commandapi.commands
import dev.rooster.core.RoosterServices
import dev.rooster.core.initRooster
import dev.rooster.core.initRoosterDisable
import dev.rooster.core.initRoosterLoad
import dev.rooster.db.utility_tables.PlayerManager
import dev.rooster.localization.provider.YmlLocaleProvider
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale

class ExtendedInventoryPlugin : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
        val services = RoosterServices()
        val playerManager by services.setDelegate(PlayerManager())
    }

    override fun onLoad() = initRoosterLoad()

    override fun onEnable() {
        plugin = this

        initRooster(plugin, services) {
            services.set(YmlLocaleProvider(
                mapOf("en_US" to Locale.ENGLISH),
                "en_US"
            ))
            initDb()
            initUi()
            commands {
                registerCommands()
            }
        }

        Bukkit.getPluginManager().registerEvents(ChatInputManager, this)
        Bukkit.getPluginManager().registerEvents(HotbarListener, this)
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun onJoin(event: PlayerJoinEvent) = playerManager.playerLogin(event.player)
        }, this)

        Bukkit.getOnlinePlayers().forEach {
            playerManager.playerLogin(it)
            HotbarManager.ensureProfile(it)
        }
    }

    override fun onDisable() {
        HotbarManager.saveAll()
        initRoosterDisable()
    }
}
