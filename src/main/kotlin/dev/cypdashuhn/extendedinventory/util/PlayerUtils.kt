package dev.cypdashuhn.extendedinventory.util

import dev.cypdashuhn.extendedinventory.ExtendedInventoryPlugin
import org.bukkit.entity.Player

fun playerId(player: Player): Int {
    val pm = ExtendedInventoryPlugin.playerManager
    return pm.playerByUUID(player.uniqueId.toString())?.id?.value
        ?: throw IllegalStateException("Player ${player.name} not registered in PlayerManager")
}
