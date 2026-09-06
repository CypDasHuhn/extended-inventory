package dev.cypdashuhn.extendedinventory.hotbar

import dev.cypdashuhn.extendedinventory.ExtendedInventoryPlugin
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

object HotbarListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val state = HotbarManager.getState(event.player)
        if (state.profileId == null) {
            HotbarManager.ensureProfile(event.player)
        }
        ExtendedInventoryPlugin.plugin.server.scheduler.runTaskLater(
            ExtendedInventoryPlugin.plugin,
            Runnable { HotbarManager.mirrorToHotbar(event.player) },
            1L
        )
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        HotbarManager.saveState(event.player)
    }

    @EventHandler
    fun onHotbarClick(event: InventoryClickEvent) {
        if (event.slot !in 0..8) return
        if (event.slot < 0) return
        if (event.clickedInventory != event.whoClicked.inventory) return

        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        val state = HotbarManager.getState(player)
        if (state.mode == HotbarMode.LOCKED) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onAnchorUse(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val item = event.player.inventory.itemInMainHand
        if (!HotbarManager.isAnchorItem(item)) return

        event.isCancelled = true

        val coords = HotbarManager.resolveAnchorJump(item) ?: return
        HotbarManager.jumpTo(event.player, coords.first, coords.second)
    }
}
