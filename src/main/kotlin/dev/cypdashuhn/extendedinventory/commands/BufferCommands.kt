package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.db.BufferManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.rooster.commands.*
import dev.rooster.commands.types.*

fun <T : CanSuggest> T.suggestBufferNames(): T =
    suggestStrings {
        val player = playerOrNull ?: return@suggestStrings emptyList()
        BufferManager.list(player).map { BufferManager.formatTimestamp(it.timestamp) }
    }

fun ChildrenScope.buffer() =
    literal("buffer") {
        literal("load") {
            string("name")
                .suggestBufferNames()
                .optional()
                .onMissing { loadBuffer(player, null) }
                .onExecute { loadBuffer(player, arg<String>("name")) }
        }
    }

private fun loadBuffer(player: org.bukkit.entity.Player, name: String?) {
    val success = HotbarManager.loadBuffer(player, name)
    if (success) {
        player.msg("${T.green}Buffer loaded.")
    } else {
        player.msg("${T.red}No buffer found.")
    }
}
